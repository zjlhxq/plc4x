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

import org.apache.commons.lang3.StringUtils;
import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.ads.api.generic.types.AmsNetId;
import org.apache.plc4x.java.ads.api.generic.types.AmsPort;
import org.apache.plc4x.java.ads.connection.AdsAbstractPlcConnection;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.s7.connection.S7PlcConnection;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;

/**
 * This is Class encapsulates all relevant informations to a Connection and provides methods to create and get them.
 */

public class ConnectionX {

    private static final Pattern ADS_ADDRESS_PATTERN = Pattern.compile("(?<targetAmsNetId>" + AmsNetId.AMS_NET_ID_PATTERN + "):(?<targetAmsPort>" + AmsPort.AMS_PORT_PATTERN + ")" + "(/" + "(?<sourceAmsNetId>" + AmsNetId.AMS_NET_ID_PATTERN + "):(?<sourceAmsPort>" + AmsPort.AMS_PORT_PATTERN + ")" + ")?");
    private static final Pattern INET_ADDRESS_PATTERN = Pattern.compile("tcp://(?<host>[\\w.]+)(:(?<port>\\d*))?");
    private static final Pattern SERIAL_PATTERN = Pattern.compile("serial://(?<serialDefinition>((?!/\\d).)*)");
    private static final Pattern ADS_URI_PATTERN = Pattern.compile("^ads:(" + INET_ADDRESS_PATTERN + "|" + SERIAL_PATTERN + ")/" + ADS_ADDRESS_PATTERN + "(\\?.*)?");
    private static final Pattern S7_URI_PATTERN = Pattern.compile("^s7://(?<host>.*)/(?<rack>\\d{1,4})/(?<slot>\\d{1,4})(?<params>\\?.*)?");
    public static ConcurrentHashMap<String, ConnectionX> Connections = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Integer> ConnectionCount = new ConcurrentHashMap<>();
    private static UaNodeContext Context;
    private ArrayList<UaNode> NodesToAdd = new ArrayList<>();
    private ArrayList<UaNode> SpecialNodesToAdd = new ArrayList<>();
    private String ConnectionString;
    private PlcConnection Connection;
    private UaFolderNode ConnectionFolder;
    private UaFolderNode VariablesFolder;
    private UaVariableNode ConnectionStringNode;
    private UaFolderNode ReadableTreeVariablesFolder;
    private String ReadableTreeType;

    protected ConnectionX(String ConnectionString, String MaschineName, UaNodeContext NodeContext, PlcConnection Connection) {
        this.Context = NodeContext;
        this.Connection = Connection;
        this.ConnectionString = ConnectionString;
        createFolders(MaschineName);
        createReadableTree(Connection, ConnectionString, MaschineName);
    }

    /**
     * checks if there is already a Connection to the server and creates one if none exists
     *
     * @param ConnectionString the Adress of a Server (like: opcua:tcp://192.168.178.42:8443/milo)
     * @param MaschineName     a String representing the Server, can be a usergiven Name or the ConnectionString
     * @param NodeContext      the Context of the Server
     * @return returns the ConnectionX Object which is created
     * @throws AlreadyExistsException if the Connection already exsits
     */
    public static ConnectionX addConnectionX(String ConnectionString, String MaschineName, UaNodeContext NodeContext) throws AlreadyExistsException, PlcConnectionException {
        if (!Connections.containsKey(ConnectionString)) {
            ConnectionCount.put(ConnectionString, 1);
        } else {
            Integer count = ConnectionCount.get(ConnectionString);
            ConnectionCount.put(ConnectionString, count + 1);
            throw new AlreadyExistsException("The Connection: " + ConnectionString + " already exists.");
        }

        PlcConnection Connection = new PlcDriverManager().getConnection(ConnectionString);
        ConnectionX connectionX = new ConnectionX(ConnectionString, MaschineName, NodeContext, Connection);
        Connections.put(ConnectionString, connectionX);
        return connectionX;
    }

    /**
     * returns the ConnectionX Object belonging to the ConnectionString
     *
     * @param ConnectionString the Adress of a Server
     * @return returns the ConnectionX Object
     */
    public static ConnectionX getConnectionX(String ConnectionString) {
        return Connections.get(ConnectionString);
    }

    /**
     * creates the Folders and Nodes representing a Connection
     *
     * @param MaschineName a String representing the Server, can be a usergiven Name or the ConnectionString
     */
    private void createFolders(String MaschineName) {
        ConnectionFolder = createConnectionFolder(MaschineName);
        ConnectionStringNode = createConnectionStringNode();
        VariablesFolder = createVariablesFolder(MaschineName);
        ConnectionFolder.addOrganizes(ConnectionStringNode);
        ConnectionFolder.addOrganizes(VariablesFolder);
        NodesToAdd.add(ConnectionFolder);
        NodesToAdd.add(ConnectionStringNode);
        NodesToAdd.add(VariablesFolder);
    }

    /**
     * returns the Connection of this ConnectionX Object
     *
     * @return returns the Connection
     */
    public PlcConnection getConnection() {
        return Connection;
    }

    /**
     * adds Variabels to this Connection
     *
     * @param id           the NodeId the Variable should get
     * @param FieldAdress  the Adress of the Variable on the Server
     * @param DataType     the DataType of the Variable
     * @param MaschineName a String representing the Server, can be a usergiven Name or the ConnectionString
     * @param VariableName a String representing the Variable, can be a usergiven Name or the FieldAdress
     * @return returns the created VariableX Object
     */
    public VariableX addVariable(NodeId id, String FieldAdress, NodeId DataType, String MaschineName, String VariableName) throws AlreadyExistsException {
        return VariableX.addVariableX(id, ConnectionString, FieldAdress, DataType, MaschineName, VariableName, Context, Connection, VariablesFolder);
    }

    /**
     * creates a Folder for this Connection
     *
     * @param MaschineName a String representing the Server, can be a usergiven Name or the ConnectionString
     * @return returns the ConnectionFolder
     */
    private UaFolderNode createConnectionFolder(String MaschineName) {
        String newConnectionString = ConnectionString.replaceAll(":", "..");
        newConnectionString = newConnectionString.replaceAll(";", ".%");
        return VariableX.createFolderNode(VariableX.newNodeId(newConnectionString), MaschineName, Context);
    }

    /**
     * creates a Folder for all the Variables that are linked to this Connection
     *
     * @param MaschineName a String representing the Server, can be a usergiven Name or the ConnectionString
     * @return returns the VariablesFolder
     */
    private UaFolderNode createVariablesFolder(String MaschineName) {
        return VariableX.createFolderNode(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/Variables"), "Variables to Connection: " + MaschineName, Context);
    }

    /**
     * creates a Variable Node with the ConnectionString of this Connection as Value
     *
     * @return returns the ConnectionStringNode
     */
    private UaVariableNode createConnectionStringNode() {
        UaVariableNode ConnectionStringNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/ConnectionString"), Context, "ConnectionString", Identifiers.String);
        ConnectionStringNode.setValue(new DataValue(new Variant(ConnectionString)));
        return ConnectionStringNode;
    }

    /**
     * returns the ConnectionFolder of this Connection
     *
     * @return returns the ConnectionFolder
     */
    public UaFolderNode getConnectionFolder() {
        return ConnectionFolder;
    }

    /**
     * returns the a List with all created Nodes, so they can be added to the NameSpace
     *
     * @return returns a List with created Nodes
     */
    public ArrayList<UaNode> nodesToAdd() {
        return NodesToAdd;
    }

    /**
     * returns the a List with all created Nodes, so they can be added to the NameSpace
     *
     * @return returns a List with created Nodes
     */
    public ArrayList<UaNode> specialNodesToAdd() {
        return SpecialNodesToAdd;
    }

    /**
     * checks if the Connection is a ADS or S7 Connection, so the additional readabel tree branches can be created
     *
     * @param Connection       the created Connection
     * @param ConnectionString the Adress of a Server
     * @param MaschineName     a String representing the Server, can be a usergiven Name or the ConnectionString
     */
    private void createReadableTree(PlcConnection Connection, String ConnectionString, String MaschineName) {

        if (Connection instanceof AdsAbstractPlcConnection) {
            if (!AggregationsNamespace.get().nodeExists(new NodeId(2, "RootNode/Protocols/ADS"))) {
                UaFolderNode AdsFolder = new UaFolderNode(
                    Context,
                    new NodeId(2, "RootNode/Protocols/ADS"),
                    new QualifiedName(2, "ADS"),
                    LocalizedText.english("ADS")
                );

                AggregationsNamespace.get().ProtocolsFolder.addOrganizes(AdsFolder);
                SpecialNodesToAdd.add(AdsFolder);
            }
            ReadableTreeVariablesFolder = createAdsConnectionTree(MaschineName, parseAdsConnectionString(ConnectionString));
            ReadableTreeType = "Ads";
        } else if (Connection instanceof S7PlcConnection) {

            if (!AggregationsNamespace.get().nodeExists(new NodeId(2, "RootNode/Protocols/S7"))) {
                UaFolderNode S7Folder = new UaFolderNode(
                    Context,
                    new NodeId(2, "RootNode/Protocols/S7"),
                    new QualifiedName(2, "S7"),
                    LocalizedText.english("S7")
                );

                AggregationsNamespace.get().ProtocolsFolder.addOrganizes(S7Folder);
                SpecialNodesToAdd.add(S7Folder);
            }
            ReadableTreeVariablesFolder = createS7ConnectionTree(MaschineName, parseS7ConnectionString(ConnectionString));
            ReadableTreeType = "S7";
        } else System.out.println("No Protocol with extended tree detected.");
    }

    /**
     * if the Connection is a ADS Connection this method gets the Informations of the ConnectionString and stores them in a HashMap
     *
     * @param ConnectionString the Adress of a Server
     * @return returns HashMap with the Informations of the ConnectionString
     */
    private ConcurrentHashMap parseAdsConnectionString(String ConnectionString) {

        Matcher matcher = ADS_URI_PATTERN.matcher(ConnectionString);
        matcher.matches();

        ConcurrentHashMap<String, String> content = new ConcurrentHashMap<String, String>();

        if (matcher.group("host") != null) content.put("host", matcher.group("host"));
        if (matcher.group("port") != null) content.put("port", matcher.group("port"));
        if (matcher.group("serialDefinition") != null)
            content.put("serialDefinition", matcher.group("serialDefinition"));
        if (matcher.group("targetAmsNetId") != null) content.put("targetAmsNetId", matcher.group("targetAmsNetId"));
        if (matcher.group("targetAmsPort") != null) content.put("targetAmsPort", matcher.group("targetAmsPort"));
        if (matcher.group("sourceAmsNetId") != null) content.put("sourceAmsNetId", matcher.group("sourceAmsNetId"));
        if (matcher.group("sourceAmsPort") != null) content.put("sourceAmsPort", matcher.group("sourceAmsPort"));

        return content;
    }

    /**
     * if the Connection is a S7 Connection this method gets the Informations of the ConnectionString and stores them in a HashMap
     *
     * @param ConnectionString the Adress of a Server
     * @return returns HashMap with the Informations of the ConnectionString
     */
    private ConcurrentHashMap parseS7ConnectionString(String ConnectionString) {
        Matcher matcher = S7_URI_PATTERN.matcher(ConnectionString);
        matcher.matches();

        String params = matcher.group("params");

        String curParamPduSize = "1024";
        String curParamMaxAmqCaller = "8";
        String curParamMaxAmqCallee = "8";
        String curParamControllerType = null;
        if (!StringUtils.isEmpty(params)) {
            for (String param : params.split("&")) {
                String[] paramElements = param.split("=");
                String paramName = paramElements[0];
                if (paramElements.length == 2) {
                    String paramValue = paramElements[1];
                    switch (paramName) {
                        case "pdu-size":
                            curParamPduSize = paramValue;
                            break;
                        case "max-amq-caller":
                            curParamMaxAmqCaller = paramValue;
                            break;
                        case "max-amq-callee":
                            curParamMaxAmqCallee = paramValue;
                            break;
                        case "controller-type":
                            curParamControllerType = paramValue;
                            break;
                        default:
                            System.out.println("Unknown parameter " + paramName + " with value " + paramValue);
                    }
                } else {
                    System.out.println("Unknown no-value parameter " + paramName);
                }
            }
        }

        ConcurrentHashMap<String, String> content = new ConcurrentHashMap<String, String>();
        content.put("host", matcher.group("host"));
        content.put("rack", matcher.group("rack"));
        content.put("slot", matcher.group("slot"));
        content.put("PduSize", curParamPduSize);
        content.put("MaxAmqCaller", curParamMaxAmqCaller);
        content.put("MaxAmqCallee", curParamMaxAmqCallee);
        content.put("ControllerType", curParamControllerType);

        return content;
    }

    /**
     * this method takes the Informations of the HashMap and creates a readable Tree representation in OPC UA
     *
     * @param MaschineName a String representing the Server, can be a usergiven Name or the ConnectionString
     * @param content      a list with the Informations of the ConnectionString
     * @return returns HashMap with the Informations of the ConnectionString
     */
    private UaFolderNode createAdsConnectionTree(String MaschineName, ConcurrentHashMap content) {

        UaFolderNode AdsConnectionNode = VariableX.createFolderNode(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/AdsConnection"), MaschineName, Context);
        AdsConnectionNode.addReference(new Reference(
            AdsConnectionNode.getNodeId(),
            Identifiers.Organizes,
            VariableX.newNodeId("RootNode/Protocols/ADS").expanded(),
            false
        ));

        UaFolderNode AdsTargetNode = VariableX.createFolderNode(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/AdsTarget"), "Host/IP", Context);
        AdsConnectionNode.addOrganizes(AdsTargetNode);

        UaFolderNode AdsTargetAmsNode = VariableX.createFolderNode(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/AdsTargetAms"), "AdsTargetAms", Context);
        AdsConnectionNode.addOrganizes(AdsTargetAmsNode);

        UaFolderNode AdsSourceAmsNode = VariableX.createFolderNode(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/AdsSourceAms"), "AdsSourceAms", Context);
        AdsConnectionNode.addOrganizes(AdsSourceAmsNode);

        UaFolderNode AdsVariablesNode = VariableX.createFolderNode(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/AdsVariables"), "AdsVariables", Context);
        AdsConnectionNode.addOrganizes(AdsVariablesNode);


        UaVariableNode AdsPortNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/AdsPort"), Context, "Port", Identifiers.String);
        AdsPortNode.setValue(new DataValue(new Variant(content.get("port"))));
        AdsTargetNode.addOrganizes(AdsPortNode);

        UaVariableNode AdsHostNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/AdsHost"), Context, "Host/IP", Identifiers.String);
        AdsHostNode.setValue(new DataValue(new Variant(content.get("host"))));
        AdsTargetNode.addOrganizes(AdsHostNode);

        UaVariableNode AdsSerialDefinitionNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/AdsSerialDefinition"), Context, "SerialDefinition", Identifiers.String);
        AdsSerialDefinitionNode.setValue(new DataValue(new Variant(content.get("serialDefinition"))));
        AdsConnectionNode.addOrganizes(AdsSerialDefinitionNode);

        UaVariableNode AdsTargetAmsNetIdNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/AdsTargetAmsNetId"), Context, "TargetAmsNetId", Identifiers.String);
        AdsTargetAmsNetIdNode.setValue(new DataValue(new Variant(content.get("targetAmsNetId"))));
        AdsTargetAmsNode.addOrganizes(AdsTargetAmsNetIdNode);

        UaVariableNode AdsTargetAmsPortNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/AdsTargetAmsPort"), Context, "TargetAmsPort", Identifiers.String);
        AdsTargetAmsPortNode.setValue(new DataValue(new Variant(content.get("targetAmsPort"))));
        AdsTargetAmsNode.addOrganizes(AdsTargetAmsPortNode);

        UaVariableNode AdsSourceAmsNetIdNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/SourceAmsNetId"), Context, "SourceAmsNetId", Identifiers.String);
        AdsSourceAmsNetIdNode.setValue(new DataValue(new Variant(content.get("sourceAmsNetId"))));
        AdsSourceAmsNode.addOrganizes(AdsSourceAmsNetIdNode);

        UaVariableNode AdsSourceAmsPortNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/SourceAmsPort"), Context, "SourceAmsPort", Identifiers.String);
        AdsSourceAmsPortNode.setValue(new DataValue(new Variant(content.get("sourceAmsPort"))));
        AdsSourceAmsNode.addOrganizes(AdsSourceAmsPortNode);

        AdsConnectionNode.addOrganizes(ConnectionStringNode);

        NodesToAdd.add(AdsConnectionNode);
        NodesToAdd.add(AdsTargetNode);
        NodesToAdd.add(AdsTargetAmsNode);
        NodesToAdd.add(AdsSourceAmsNode);
        NodesToAdd.add(AdsPortNode);
        NodesToAdd.add(AdsHostNode);
        NodesToAdd.add(AdsSerialDefinitionNode);
        NodesToAdd.add(AdsTargetAmsNetIdNode);
        NodesToAdd.add(AdsTargetAmsPortNode);
        NodesToAdd.add(AdsSourceAmsNetIdNode);
        NodesToAdd.add(AdsSourceAmsPortNode);
        NodesToAdd.add(AdsVariablesNode);
        NodesToAdd.add(ConnectionStringNode);

        return AdsVariablesNode;
    }

    /**
     * this method takes the Informations of the HashMap and creates a readable Tree representation in OPC UA
     *
     * @param MaschineName a String representing the Server, can be a usergiven Name or the ConnectionString
     * @param content      a list with the Informations of the ConnectionString
     * @return returns HashMap with the Informations of the ConnectionString
     */
    private UaFolderNode createS7ConnectionTree(String MaschineName, ConcurrentHashMap content) {
        UaFolderNode S7ConnectionNode = VariableX.createFolderNode(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/S7Connection"), MaschineName, Context);
        S7ConnectionNode.addReference(new Reference(
            S7ConnectionNode.getNodeId(),
            Identifiers.Organizes,
            VariableX.newNodeId("RootNode/Protocols/S7").expanded(),
            true
        ));

        UaFolderNode S7ParameterNode = VariableX.createFolderNode(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/S7Parameter"), "Parameter", Context);
        S7ConnectionNode.addOrganizes(S7ParameterNode);

        UaFolderNode S7VariablesNode = VariableX.createFolderNode(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/S7Variables"), "S7Varaibles", Context);
        S7ConnectionNode.addOrganizes(S7VariablesNode);


        UaVariableNode S7HostNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/S7Host"), Context, "Host/IP", Identifiers.String);
        S7HostNode.setValue(new DataValue(new Variant(content.get("host"))));
        S7ConnectionNode.addOrganizes(S7HostNode);

        UaVariableNode S7RackNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/S7Rack"), Context, "Rack", Identifiers.String);
        S7RackNode.setValue(new DataValue(new Variant(content.get("rack"))));
        S7ConnectionNode.addOrganizes(S7RackNode);

        UaVariableNode S7SlotNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/S7Slot"), Context, "Slot", Identifiers.String);
        S7SlotNode.setValue(new DataValue(new Variant(content.get("slot"))));
        S7ConnectionNode.addOrganizes(S7SlotNode);

        UaVariableNode S7SPduSizeNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/S7PduSize"), Context, "PduSize", Identifiers.String);
        S7SPduSizeNode.setValue(new DataValue(new Variant(content.get("PduSize"))));
        S7ParameterNode.addOrganizes(S7SPduSizeNode);

        UaVariableNode S7MaxAmqCallerNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/S7MaxAmqCaller"), Context, "MaxAmqCaller", Identifiers.String);
        S7MaxAmqCallerNode.setValue(new DataValue(new Variant(content.get("MaxAmqCaller"))));
        S7ParameterNode.addOrganizes(S7MaxAmqCallerNode);

        UaVariableNode S7MaxAmqCalleeNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/S7MaxAmqCallee"), Context, "MaxAmqCallee", Identifiers.String);
        S7MaxAmqCalleeNode.setValue(new DataValue(new Variant(content.get("MaxAmqCallee"))));
        S7ParameterNode.addOrganizes(S7MaxAmqCalleeNode);

        UaVariableNode S7ControllerTypeNode = VariableX.createVariableNodes(VariableX.newNodeId((String) ConnectionFolder.getNodeId().getIdentifier() + "/S7ControllerType"), Context, "ControllerType", Identifiers.String);
        S7ControllerTypeNode.setValue(new DataValue(new Variant(content.get("ControllerType"))));
        S7ParameterNode.addOrganizes(S7ControllerTypeNode);

        S7ConnectionNode.addOrganizes(ConnectionStringNode);

        NodesToAdd.add(S7ConnectionNode);
        NodesToAdd.add(S7ParameterNode);
        NodesToAdd.add(S7HostNode);
        NodesToAdd.add(S7RackNode);
        NodesToAdd.add(S7SlotNode);
        NodesToAdd.add(S7SPduSizeNode);
        NodesToAdd.add(S7MaxAmqCallerNode);
        NodesToAdd.add(S7MaxAmqCalleeNode);
        NodesToAdd.add(S7ControllerTypeNode);
        NodesToAdd.add(S7VariablesNode);
        NodesToAdd.add(ConnectionStringNode);

        return S7VariablesNode;
    }

    /**
     * a Method to get the ReadableTreeVariablesFolder
     *
     * @return returns the VariablesFolder for the readable Branch
     */
    public UaFolderNode getReadableTreeVariablesFolder() {
        return ReadableTreeVariablesFolder;
    }

    /**
     * informs if this Connection has a readable tree and if its ADS or S7
     *
     * @return returns a String with the Type of the Readabletree
     */
    public String getReadableTreeType() {
        return ReadableTreeType;
    }
}

