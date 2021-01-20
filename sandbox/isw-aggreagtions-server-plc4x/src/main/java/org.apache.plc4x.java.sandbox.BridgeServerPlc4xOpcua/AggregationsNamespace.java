/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.plc4x.java.sandbox.BridgeServerPlc4xOpcua;

import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespace;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.*;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * This is a custom Namespace desinged to work as Datamodell of the Aggregationsserver. It contains the basic Structure
 * of the Aggregationsserver with Protocols and Methods.
 */
public class AggregationsNamespace extends ManagedNamespace implements AggregationsServerInterface {

    private static final String NAMESPACE_URI = "AggregationsServerPlc4x";
    private static UaFolderNode GenericProtocolFolder;
    private static AggregationsNamespace namespace;
    private final AggregationsSubscriptionModel subscriptionModel;
    protected UaFolderNode rootNode;
    protected UaFolderNode ProtocolsFolder;
    protected UaFolderNode TestFolder;

    private AggregationsNamespace(OpcUaServer server) {
        super(server, NAMESPACE_URI);

        subscriptionModel = new AggregationsSubscriptionModel(server, this);
    }

    public static AggregationsNamespace get() {
        return namespace;
    }

    protected static AggregationsNamespace create(OpcUaServer server) {
        return namespace = new AggregationsNamespace(server);
    }

    public static NodeId getDataType(String TypeString) {
        switch (TypeString) {
            case "Boolean":
                return Identifiers.Boolean;
            case "SByte":
                return Identifiers.SByte;
            case "Byte":
                return Identifiers.Byte;
            case "Int16":
                return Identifiers.Int16;
            case "UInt16":
                return Identifiers.UInt16;
            case "Int32":
                return Identifiers.Int32;
            case "UInt32":
                return Identifiers.UInt32;
            case "Int64":
                return Identifiers.Int64;
            case "UInt64":
                return Identifiers.UInt64;
            case "Float":
                return Identifiers.Float;
            case "Double":
                return Identifiers.Double;
            case "String":
                return Identifiers.String;
            case "DateTime":
                return Identifiers.DateTime;
            case "Guid":
                return Identifiers.Guid;
            case "ByteString":
                return Identifiers.ByteString;
            case "XmlElement":
                return Identifiers.XmlElement;
            case "NodeId":
                return Identifiers.NodeId;
            case "ExpandedNodeId":
                return Identifiers.ExpandedNodeId;
            case "StatusCode":
                return Identifiers.StatusCode;
            case "QualifiedName":
                return Identifiers.QualifiedName;
            case "LocalizedText":
                return Identifiers.LocalizedText;
            case "Structure":
                return Identifiers.Structure;
            case "DataValue":
                return Identifiers.DataValue;
            case "BaseDataType":
                return Identifiers.BaseDataType;
            case "DiagnosticInfo":
                return Identifiers.DiagnosticInfo;
            default:
                System.out.println("DatenTyp not found. Permitted are: Boolean, SByte, Byte, Int16, UInt64, Float, Double, String, DateTime, Guid, ByteString, XmlElement, NodeId, ExpandedNodeId, StatusCode, QualifiedName, LocalizedText, Structure, DataValue, BaseDataType, DiagnosticInfo.");
        }
        return null;
    }

    @Override
    protected void onStartup() {
        super.onStartup();

        NodeId rootNodeId = newNodeId("RootNode");

        rootNode = new UaFolderNode(
            getNodeContext(),
            rootNodeId,
            newQualifiedName("RootNode"),
            LocalizedText.english("RootNode")
        );

        getNodeManager().addNode(rootNode);

        rootNode.addReference(new Reference(
            rootNode.getNodeId(),
            Identifiers.Organizes,
            Identifiers.ObjectsFolder.expanded(),
            false
        ));

        addMethods();
        addProtocols();
    }

    /**
     * Deltes a VariableNode and all beloning Nodes by its id
     *
     * @param id the NodeId of the VariableNode to delete
     * @return an indicator if the attempt to delete was successful
     */
    public boolean deleteItem(NodeId id) {
        try {
            //removes the VariableNode and all the Nodes belonging to this VariableNode
            ArrayList<UaNode> VarNodesToDel = DataItemX.dataItems.get(id).getVariableX().nodesToAdd();
            for (int i = 0; i < VarNodesToDel.size(); i++) {
                getNode(VarNodesToDel.get(i).getNodeId()).get().delete();
            }
            String ConnectionString = DataItemX.dataItems.get(id).getConnectionString();
            DataItemX.dataItems.get(id).deleteItem();

            //delete Connection if necessary (if its the only Variable for this Connection)
            if (ConnectionX.ConnectionCount.get(ConnectionString) == 1) {
                ConnectionX ConnToDelete = ConnectionX.Connections.get(ConnectionString);
                ArrayList<UaNode> ConnNodesToDel = ConnToDelete.nodesToAdd();
                for (int i = 0; i < ConnNodesToDel.size(); i++) {
                    getNode(VarNodesToDel.get(i).getNodeId()).get().delete();
                }
                ConnectionX.ConnectionCount.remove(ConnectionString);
                ConnectionX.Connections.remove(ConnectionString);
            } else {
                Integer count = ConnectionX.ConnectionCount.get(ConnectionString);
                ConnectionX.ConnectionCount.put(ConnectionString, count - 1);
            }
        } catch (NullPointerException e) {
            System.out.println("An error occurred while deleting the Item: " + id.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Gets the NodeId of a Varible according to its ConnectionString and FieldAdress and calls {@link #deleteItem(NodeId) deleteItem}
     *
     * @param ConnectionString the ConnectionString of a Server
     * @param FieldAdress      the FiledAdress referring to a Variable
     * @return an indicator if the attempt to delete was successful
     */
    public boolean deleteItemByCsAndFa(String ConnectionString, String FieldAdress) {
        NodeId id = DataItemX.getId(ConnectionString, FieldAdress);
        return deleteItem(id);
    }

    /**
     * Gets the NodeId of a Varible according to its ConnectionString and VariableName and calls {@link #deleteItem(NodeId) deleteItem}
     *
     * @param ConnectionString the ConnectionString of a Server
     * @param VariableName     representing a Variable with a usergiven Name
     * @return an indicator if the attempt to delete was successful
     */
    public boolean deleteItemByCsAndVn(String ConnectionString, String VariableName) {
        NodeId id = DataItemX.getId(ConnectionString, VariableName);
        return deleteItem(id);
    }

    /**
     * Gets the NodeId of a Varible according to its MaschineName and FieldAdress and calls {@link #deleteItem(NodeId) deleteItem}
     *
     * @param MaschineName representing a Server with a usergiven Name
     * @param FieldAdress  the FiledAdress referring to a Variable
     * @return an indicator if the attempt to delete was successful
     */
    public boolean deleteItemByMnAndFa(String MaschineName, String FieldAdress) {
        NodeId id = DataItemX.getId(MaschineName, FieldAdress);
        return deleteItem(id);
    }

    /**
     * Gets the NodeId of a Varible according to its MaschineName and VariableName and calls {@link #deleteItem(NodeId) deleteItem}
     *
     * @param MaschineName representing a Server with a usergiven Name
     * @param VariableName representing a Variable with a usergiven Name
     * @return an indicator if the attempt to delete was successful
     */
    public boolean deleteItemByMnAndVn(String MaschineName, String VariableName) {
        NodeId id = DataItemX.getId(MaschineName, VariableName);
        return deleteItem(id);
    }

    /**
     * creates a Variable with the core inforamtions ConnectionString, FildAdress and DataType.
     * MaschineName and VariableName are just for better overview.
     * besides the Variable Node also all the beloning Nodes are created
     *
     * @param ConnectionString the ConnectionString of a Server
     * @param FieldAdress      the FiledAdress referring to Variable
     * @param DataType         the DataType of the Variable for example String, Byte, Boolean
     * @param MaschineName     representing a Server with a usergiven Name
     * @param VariableName     representing a Variable with a usergiven Name
     * @return an indicator if the attempt to create the Variable was successful
     */
    public boolean createItem(String ConnectionString, String FieldAdress, NodeId DataType, String MaschineName, String VariableName) {
        //generate NodeId as Combination of ConnectionString and FieldAdress
        NodeId id = generateNodeId(ConnectionString, FieldAdress);

        //createConnection
        try {
            ConnectionX connectionX = ConnectionX.addConnectionX(ConnectionString, MaschineName, getNodeContext());
            GenericProtocolFolder.addOrganizes(connectionX.getConnectionFolder());
            addNodes(connectionX.nodesToAdd());
            addNodes(connectionX.specialNodesToAdd());
        } catch (AlreadyExistsException e) {
            System.out.println(e.getMessage());
        } catch (PlcConnectionException e) {
            System.out.println("The Connection to: " + ConnectionString + " can't be established.");
            e.printStackTrace();
            return false;
        }

        //createVariable
        try {
            VariableX variableX = ConnectionX.getConnectionX(ConnectionString).addVariable(id, FieldAdress, DataType, MaschineName, VariableName);
            addNodes(variableX.nodesToAdd());
        } catch (AlreadyExistsException e) {
            System.out.println("The Variable: " + FieldAdress + " already exists.");
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * calls {@link #createItem(String, String, NodeId, String, String) createItem} with the additional Parameter VariableName
     *
     * @param ConnectionString the ConnectionString of a Server
     * @param FieldAdress      the FiledAdress referring to Variable
     * @param DataType         the DataType of the Variable for example String, Byte, Boolean
     * @param VariableName     representing a Variable with a usergiven Name
     * @return an indicator if the attempt to create the Variable was successful
     */
    public boolean createItemWithVariableName(String ConnectionString, String FieldAdress, NodeId DataType, String VariableName) {
        return createItem(ConnectionString, FieldAdress, DataType, ConnectionString, VariableName);
    }

    /**
     * calls {@link #createItem(String, String, NodeId, String, String) createItem} with the additional Parameter MaschineName
     *
     * @param ConnectionString the ConnectionString of a Server
     * @param FieldAdress      the FiledAdress referring to Variable
     * @param DataType         the DataType of the Variable for example String, Byte, Boolean
     * @param MaschineName     representing a Server with a usergiven Name
     * @return an indicator if the attempt to create the Variable was successful
     */
    public boolean createItemwWithMaschineName(String ConnectionString, String FieldAdress, NodeId DataType, String MaschineName) {
        return createItem(ConnectionString, FieldAdress, DataType, MaschineName, FieldAdress);
    }

    /**
     * calls {@link #createItem(String, String, NodeId, String, String) createItem} without additional Parameters
     *
     * @param ConnectionString the ConnectionString of a Server
     * @param FieldAdress      the FiledAdress referring to Variable
     * @param DataType         the DataType of the Variable for example String, Byte, Boolean
     * @return an indicator if the attempt to create the Variable was successful
     */
    public boolean createItem(String ConnectionString, String FieldAdress, NodeId DataType) {
        return createItem(ConnectionString, FieldAdress, DataType, ConnectionString, FieldAdress);
    }

    public boolean createAlias(NodeId orginalNodeId, NodeId aliasNodeId, Boolean takeRefereces) {
        try {
            UaNode aliasNode = new AliasNode(orginalNodeId, aliasNodeId, takeRefereces);
            getNodeManager().addNode(aliasNode);

        } catch (WrongInputException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean createAlias(NodeId orginalNodeId, NodeId aliasNodeId) {
        return createAlias(orginalNodeId, aliasNodeId, false);
    }

    public UaNodeContext getUaNodecontext() {
        return getNodeContext();
    }

    /**
     * works through the jsonFile and creates and deletes Items
     *
     * @param jsonFile represents a File with ConnectionStrings, FieldAdresses, etc.
     * @return an indicator if there were problems with/in the JSONFile
     * @throws WrongInputException if theres a Problem with/in the JSONFile
     */
    public boolean handleConfigurationFile(JSONObject jsonFile) {
        try {
            //create
            JSONArray create = jsonFile.getJSONArray("create");
            for (int i = 0; i < create.length(); i++) {
                JSONArray Connection = create.getJSONArray(i);

                //with MaschineName
                if (Connection.length() == 3) {
                    if (Connection.get(2) instanceof JSONArray) {
                        String ConnectionString = (String) Connection.get(0);
                        String MaschineName = (String) Connection.get(1);
                        JSONArray Variables = Connection.getJSONArray(2);
                        for (int k = 0; k < Variables.length(); k++) {
                            JSONArray Variable = Variables.getJSONArray(k);

                            //with VariableName
                            if (Variable.length() == 3) {
                                String FieldAdress = (String) Variable.get(0);
                                String VariableName = (String) Variable.get(2);
                                NodeId DataType = getDataType((String) Variable.get(1));
                                createItem(ConnectionString, FieldAdress, DataType, MaschineName, VariableName);
                            }
                            //without VariableName
                            else if (Variable.length() == 2) {
                                String FieldAdress = (String) Variable.get(0);
                                NodeId DataType = getDataType((String) Variable.get(1));
                                createItemwWithMaschineName(ConnectionString, FieldAdress, DataType, MaschineName);
                            } else {
                                throw new WrongInputException("Es liegt ein Fehler im JSON File vor (create/Conenction: " + ConnectionString + ")");
                            }
                        }
                    } else {
                        throw new WrongInputException("Es liegt ein Fehler im JSON File vor (create)");
                    }
                }

                //without MaschineName
                else if (Connection.length() == 2) {
                    if (Connection.get(1) instanceof JSONArray) {
                        String ConnectionString = (String) Connection.get(0);
                        JSONArray Variables = Connection.getJSONArray(1);
                        for (int j = 0; j < Variables.length(); j++) {
                            JSONArray Variable = Variables.getJSONArray(j);

                            //with VariableName
                            if (Variable.length() == 3) {
                                String FieldAdress = (String) Variable.get(0);
                                String VariableName = (String) Variable.get(2);
                                NodeId DataType = getDataType((String) Variable.get(1));
                                createItemWithVariableName(ConnectionString, FieldAdress, DataType, VariableName);
                            }
                            //without VariableName
                            else if (Variable.length() == 2) {
                                String FieldAdress = (String) Variable.get(0);
                                NodeId DataType = getDataType((String) Variable.get(1));
                                createItem(ConnectionString, FieldAdress, DataType);
                            } else {
                                throw new WrongInputException("Es liegt ein Fehler im JSON File vor (create/Conenction: " + ConnectionString + ")");
                            }
                        }
                    } else {
                        throw new WrongInputException("Es liegt ein Fehler im JSON File vor (create)");
                    }
                } else {
                    throw new WrongInputException("Es liegt ein Fehler im JSON File vor (create)");
                }
            }


            //delete
            JSONObject delete = jsonFile.getJSONObject("delete");

            JSONArray ByCsFa = delete.getJSONArray("ByConnectionStringAndFieldAdress");
            for (int s = 0; s < ByCsFa.length(); s++) {
                JSONArray Variable = (JSONArray) ByCsFa.get(s);
                if (Variable.length() == 2) {
                    String ConnectionString = (String) Variable.get(0);
                    String FieldAdress = (String) Variable.get(1);
                    deleteItemByCsAndFa(ConnectionString, FieldAdress);
                } else {
                    throw new WrongInputException("Es liegt ein Fehler im JSON File vor (delete/ByConnectionStringAndFieldAdress)");
                }
            }

            JSONArray ByCsVn = delete.getJSONArray("ByConnectionStringAndVariableName");
            for (int t = 0; t < ByCsVn.length(); t++) {
                JSONArray Variable = (JSONArray) ByCsVn.get(t);
                if (Variable.length() == 2) {
                    String ConnectionString = (String) Variable.get(0);
                    String VariableName = (String) Variable.get(1);
                    deleteItemByCsAndVn(ConnectionString, VariableName);
                } else {
                    throw new WrongInputException("Es liegt ein Fehler im JSON File vor (delete/ByConnectionStringAndVariableName)");
                }
            }

            JSONArray ByMnFa = delete.getJSONArray("ByMaschineNameAndFieldAdress");
            for (int h = 0; h < ByMnFa.length(); h++) {
                JSONArray Variable = (JSONArray) ByMnFa.get(h);
                if (Variable.length() == 2) {
                    String MaschineName = (String) Variable.get(0);
                    String FieldAdress = (String) Variable.get(1);
                    deleteItemByMnAndFa(MaschineName, FieldAdress);
                } else {
                    throw new WrongInputException("Es liegt ein Fehler im JSON File vor (delete/ByMaschineNameAndFieldAdress)");
                }
            }

            JSONArray ByMnVn = delete.getJSONArray("ByMaschineNameAndVariableName");
            for (int u = 0; u < ByMnVn.length(); u++) {
                JSONArray Variable = (JSONArray) ByMnVn.get(u);
                if (Variable.length() == 2) {
                    String MaschineName = (String) Variable.get(0);
                    String VariableName = (String) Variable.get(1);
                    deleteItemByMnAndVn(MaschineName, VariableName);
                } else {
                    throw new WrongInputException("Es liegt ein Fehler im JSON File vor (delete/ByMaschineNameAndVariableName)");
                }
            }
        } catch (WrongInputException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * adds the Foldernodes for the Genricprotocol, and the Protocols with extendet trees
     */
    private void addProtocols() {

        //ProtocosFolder
        ProtocolsFolder = new UaFolderNode(
            getNodeContext(),
            newNodeId("RootNode/Protocols"),
            newQualifiedName("Protocols"),
            LocalizedText.english("Protocols")
        );

        getNodeManager().addNode(ProtocolsFolder);
        rootNode.addOrganizes(ProtocolsFolder);

        //GenericProtocolFolder
        GenericProtocolFolder = new UaFolderNode(
            getNodeContext(),
            newNodeId("RootNode/Protocols/GenericProtocol"),
            newQualifiedName("GenericProtocol"),
            LocalizedText.english("GenericProtocol")
        );

        getNodeManager().addNode(GenericProtocolFolder);
        ProtocolsFolder.addOrganizes(GenericProtocolFolder);

    }

    /**
     * checks if the node with the id exists already
     *
     * @param id NodeId to check
     * @return a booleans that indicates if the Node to check exists
     */
    protected boolean nodeExists(NodeId id) {
        return getNodeManager().getNode(id).isPresent();
    }

    /**
     * returns the Node if it exists
     *
     * @param id NodeId of the searched Node
     * @return the Node
     */
    public Optional<UaNode> getNode(NodeId id) {
        return getNodeManager().getNode(id);
    }

    /**
     * returns the Server
     *
     * @return
     */
    protected OpcUaServer getUaServer() {
        return getServer();
    }

    /**
     * adds the Nodes to the Namespace
     *
     * @param NodesToAdd List of Nodes to add
     */
    public void addNodes(ArrayList<UaNode> NodesToAdd) {
        for (int i = 0; i < NodesToAdd.size(); i++) {
            getNodeManager().addNode(NodesToAdd.get(i));
        }
    }

    /**
     * creates a NodeId consisting of the ConnectionString and FieldAdress
     *
     * @param ConnectionString the ConnectionString of a Server
     * @param FieldAdress      the FiledAdress referring to Variable
     */
    public NodeId generateNodeId(String ConnectionString, String FieldAdress) {
        //:and ; as they appear in the ConnectionString are forbidden in NodeIds (at least with MiloClients, UaExpert works)
        // therefore : is replaced by .. and ; by .%.
        String newConnectionString = ConnectionString.replaceAll(":", "..");
        newConnectionString = newConnectionString.replaceAll(";", ".%");

        String newFieldAdress = FieldAdress.replaceAll(":", "..");
        newFieldAdress = newFieldAdress.replaceAll(";", ".%");

        return newNodeId(newConnectionString + "." + newFieldAdress);
    }

    /**
     * adds the Foldernode for the the Methods
     */
    private void addMethods() {

        UaFolderNode MethodsFolder = new UaFolderNode(
            getNodeContext(),
            newNodeId("RootNode/Methods"),
            newQualifiedName("Methods"),
            LocalizedText.english("Methods")
        );

        getNodeManager().addNode(MethodsFolder);
        rootNode.addOrganizes(MethodsFolder);

        addCreateItemMethod(MethodsFolder);
        addDeleteItemMethod(MethodsFolder);
        addHandleConfigurationFileMethod(MethodsFolder);

    }

    /**
     * adds the Method to create Items
     *
     * @param folderNode the Folder to which the MethodNode is bound
     */
    private void addCreateItemMethod(UaFolderNode folderNode) {

        UaMethodNode methodNode = UaMethodNode.builder(getNodeContext())
            .setNodeId(newNodeId("RootNode/Methods/CreateItemMethod"))
            .setBrowseName(newQualifiedName("CreateItemMethod"))
            .setDisplayName(new LocalizedText(null, "CreateItemMethod"))
            .setDescription(LocalizedText.english("Creates a new Item"))
            .build();


        CreateItem createItem = new CreateItem(methodNode);

        methodNode.setProperty(UaMethodNode.InputArguments, createItem.getInputArguments());
        methodNode.setProperty(UaMethodNode.OutputArguments, createItem.getOutputArguments());
        methodNode.setInvocationHandler(createItem);

        getNodeManager().addNode(methodNode);

        methodNode.addReference(new Reference(
            methodNode.getNodeId(),
            Identifiers.HasComponent,
            folderNode.getNodeId().expanded(),
            false
        ));
    }

    /**
     * adds the Method to delte Items
     *
     * @param folderNode the Folder to which the MethodNode is bound
     */
    private void addDeleteItemMethod(UaFolderNode folderNode) {

        UaMethodNode methodNode = UaMethodNode.builder(getNodeContext())
            .setNodeId(newNodeId("RootNode/Methods/DeleteItemMethod"))
            .setBrowseName(newQualifiedName("DeleteItemMethod"))
            .setDisplayName(new LocalizedText(null, "DeleteItemMethod"))
            .setDescription(LocalizedText.english("Deletes a Item"))
            .build();

        DeleteItem deleteItem = new DeleteItem(methodNode);
        methodNode.setProperty(UaMethodNode.InputArguments, deleteItem.getInputArguments());
        methodNode.setProperty(UaMethodNode.OutputArguments, deleteItem.getOutputArguments());
        methodNode.setInvocationHandler(deleteItem);

        getNodeManager().addNode(methodNode);

        methodNode.addReference(new Reference(
            methodNode.getNodeId(),
            Identifiers.HasComponent,
            folderNode.getNodeId().expanded(),
            false
        ));
    }

    /**
     * adds the Method which handels the JSON File
     *
     * @param folderNode the Folder to which the MethodNode is bound
     */
    private void addHandleConfigurationFileMethod(UaFolderNode folderNode) {

        UaMethodNode methodNode = UaMethodNode.builder(getNodeContext())
            .setNodeId(newNodeId("RootNode/Methods/HandleConfigurationFile"))
            .setBrowseName(newQualifiedName("HandleConfigurationFile"))
            .setDisplayName(new LocalizedText(null, "HandleConfigurationFile"))
            .setDescription(LocalizedText.english("Creates and deltes Items from a JSON Configurationfile."))
            .build();

        HandleConfigurationFile handleConfigurationFile = new HandleConfigurationFile(methodNode);
        methodNode.setProperty(UaMethodNode.InputArguments, handleConfigurationFile.getInputArguments());
        methodNode.setProperty(UaMethodNode.OutputArguments, handleConfigurationFile.getOutputArguments());
        methodNode.setInvocationHandler(handleConfigurationFile);

        getNodeManager().addNode(methodNode);

        methodNode.addReference(new Reference(
            methodNode.getNodeId(),
            Identifiers.HasComponent,
            folderNode.getNodeId().expanded(),
            false
        ));
    }

    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsCreated(dataItems);
    }

    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsModified(dataItems);
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsDeleted(dataItems);
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        subscriptionModel.onMonitoringModeChanged(monitoredItems);
    }

    //InterfaceImplementation
    @Override
    public void createAggregationsServerNode(String connectionString, String variableAdress, NodeId dataType,
                                             String maschineName, String variableName) {
        if (!(connectionString.isEmpty() || connectionString == null)
            || !(variableAdress.isEmpty() || variableAdress == null) || dataType != null) {
            if (maschineName == null && variableName == null) {
                createItem(connectionString, variableAdress, dataType);
            } else if (variableName == null) {
                createItemwWithMaschineName(connectionString, variableAdress, dataType, maschineName);
            } else if (maschineName == null) {
                createItemWithVariableName(connectionString, variableAdress, dataType, variableName);
            } else {
                createItem(connectionString, variableAdress, dataType, maschineName, variableName);
            }
        } else {
            try {
                throw new WrongInputException("ConnectionString, FieldAdress and DataType are needed!");
            } catch (WrongInputException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteAggregationsServerNode(String connectionString, String maschineName,
                                             String variableAdress, String variableName) {
        if (connectionString != null && variableAdress != null) {
            deleteItemByCsAndFa(connectionString, variableAdress);
        } else if (connectionString != null && variableName != null) {
            deleteItemByCsAndVn(connectionString, variableName);
        } else if (maschineName != null && variableAdress != null) {
            deleteItemByMnAndFa(maschineName, variableAdress);
        } else if (maschineName != null && variableName != null) {
            deleteItemByMnAndVn(maschineName, variableName);
        } else {
            try {
                throw new WrongInputException("ConnectionString or MaschineName and FieldAdress or VariableName are needed");
            } catch (WrongInputException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void createAggregationsServerAliasNode(NodeId orginalNodeId, NodeId aliasNodeId, Boolean takeRefereces) {
        createAlias(orginalNodeId, aliasNodeId, takeRefereces);
    }

    @Override
    public void createAggregationsServerFolderNode(NodeId nodeId, String name) {
        getNodeManager().addNode(VariableX.createFolderNode(nodeId, name, getUaNodecontext()));
    }

    @Override
    public void createAggregationsServerReference(NodeId sourceNodeId, NodeId targetNodeId, NodeId ReferenceType, Boolean forward) {
        getNode(sourceNodeId).get().addReference(new Reference(sourceNodeId, ReferenceType, targetNodeId.expanded(), forward));
    }

    @Override
    public void createAggregationsServerTransformationNode(NodeId nodeId, String browseName, String displayName,
                                                           HashMap<String, NodeId> inputNodes, String javaScriptTransformationCode) {
        getNodeManager().addNode(new TransformationNode(nodeId, newQualifiedName(browseName), new LocalizedText(displayName),
            inputNodes, javaScriptTransformationCode));
    }
}