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

import org.apache.plc4x.java.ads.model.DirectAdsField;
import org.apache.plc4x.java.ads.model.SymbolicAdsField;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.s7.model.S7Field;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.server.api.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.*;
import org.eclipse.milo.opcua.sdk.server.nodes.delegates.AttributeDelegate;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;

/**
 * This is Class encapsulates all relevant informations to a Connection and provides methods to create and get them.
 */

public class VariableX {
    private static final Pattern SYMBOLIC_ADDRESS_PATTERN = Pattern.compile("^(?<symbolicAddress>.+):(?<adsDataType>\\w+)(\\[(?<numberOfElements>\\d)])?");
    private static final Pattern RESOURCE_ADDRESS_PATTERN = Pattern.compile("^((0[xX](?<indexGroupHex>[0-9a-fA-F]+))|(?<indexGroup>\\d+))/((0[xX](?<indexOffsetHex>[0-9a-fA-F]+))|(?<indexOffset>\\d+)):(?<adsDataType>\\w+)(\\[(?<numberOfElements>\\d)])?");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^%(?<memoryArea>.)(?<transferSizeCode>[XBWD]?)(?<byteOffset>\\d{1,7})(.(?<bitOffset>[0-7]))?:(?<dataType>[a-zA-Z_]+)(\\[(?<numElements>\\d+)])?");
    private static final Pattern DATA_BLOCK_ADDRESS_PATTERN = Pattern.compile("^%DB(?<blockNumber>\\d{1,5}).DB(?<transferSizeCode>[XBWD]?)(?<byteOffset>\\d{1,7})(.(?<bitOffset>[0-7]))?:(?<dataType>[a-zA-Z_]+)(\\[(?<numElements>\\d+)])?");

    private ArrayList<UaNode> NodesToAdd = new ArrayList<>();
    private List<WeakReference<AttributeObserver>> observers;

    private NodeId id;
    private String FieldAddress;
    private String VariableName;
    private NodeId DataType;
    private UaVariableNode FieldAddressNode;
    private UaVariableNode ValueNode;
    private UaFolderNode VariableFolder;
    private UaNodeContext Context;

    private VariableX(NodeId id, String ConnectionString, String VariableName, UaNodeContext NodeContext, String FieldAddress, NodeId DataType, PlcConnection Connection, UaFolderNode VariablesFolder) {
        this.id = id;
        this.FieldAddress = FieldAddress;
        this.DataType = DataType;
        this.Context = NodeContext;
        this.VariableName = VariableName;
        createNodes(VariableName, NodeContext, Connection, VariablesFolder);
        createReadableTree(FieldAddress, ConnectionString);
    }

    /**
     * checks if the Variable already exists and creates the VariableX and DataItemX if none existent
     *
     * @param id               the NodeId the Variable should get
     * @param ConnectionString the Adress of a Server
     * @param FieldAddress     the Adress of the Variable on the Server
     * @param DataType         the DataType of the Variable
     * @param MaschineName     a String representing the Server, can be a usergiven Name or the ConnectionString
     * @param VariableName     a String representing the Variable, can be a usergiven Name or the FieldAddress
     * @param Context          the Context of the Server
     * @param Connection       the Connection this Variable belongs to
     * @param VariablesFolder  the Folder of the Conenction all Variables are stored in
     * @return returns the VariableX Object
     * @throws AlreadyExistsException if the Variable already exsits
     */
    public static VariableX addVariableX(NodeId id, String ConnectionString, String FieldAddress, NodeId DataType, String MaschineName,
                                         String VariableName, UaNodeContext Context, PlcConnection Connection, UaFolderNode VariablesFolder) throws AlreadyExistsException {
        //checks if Variable already exists then creates the Variable and the DataItemX belonging to it
        if (!DataItemX.getIdByParams.containsKey(DataItemX.stringPair(ConnectionString, FieldAddress))) {
            VariableX variableX = new VariableX(id, ConnectionString, VariableName, Context, FieldAddress, DataType, Connection, VariablesFolder);
            DataItemX dataItemX = new DataItemX(id, ConnectionString, FieldAddress, DataType, MaschineName, VariableName);
            dataItemX.setVariableX(variableX);
            dataItemX.setConnectionX(ConnectionX.getConnectionX(ConnectionString));
            return variableX;
        } else {
            throw new AlreadyExistsException("The FieldAddress:" + FieldAddress + "already exists");
        }
    }

    /**
     * a mehtod to create VariableNodes
     *
     * @param id           the NodeId the VariableNode should get
     * @param Context      the Context of the Server
     * @param name         the Name the VariableNode should get
     * @param NodeDataType the DataType of the Variable Value
     * @return returns the ValueNode
     */
    public static UaVariableNode createVariableNodes(NodeId id, UaNodeContext Context, String name, NodeId NodeDataType) {
        return new UaVariableNode.UaVariableNodeBuilder(Context)
            .setNodeId(id)
            .setAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE))
            .setUserAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE))
            .setBrowseName(newQualifiedName(name))
            .setDisplayName(LocalizedText.english(name))
            .setDataType(NodeDataType)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .build();
    }

    /**
     * a mehtod to create FolderNodes
     *
     * @param id      the NodeId the FolderNode should get
     * @param name    the Name the FolderNode should get
     * @param Context the Context of the Server
     * @return returns the FolderNode
     */
    public static UaFolderNode createFolderNode(NodeId id, String name, UaNodeContext Context) {
        return new UaFolderNode(
            Context,
            id,
            newQualifiedName(name),
            LocalizedText.english(name)
        );
    }

    /**
     * a mehtod to create QualifiedNames
     *
     * @param name the Name in the QualifiedName
     * @return returns the QualifiedName
     */
    public static QualifiedName newQualifiedName(String name) {
        return new QualifiedName(2, name);
    }

    /**
     * a mehtod to create NodeIds
     *
     * @param name the Name the Idetifier in the NodeId should get
     * @return returns the NodeId
     */
    public static NodeId newNodeId(String name) {
        return new NodeId(2, name);
    }

    /**
     * creates the Variable and all the Folders and Nodes belonging to it
     *
     * @param VariableName    a String representing the Variable, can be a usergiven Name or the FieldAddress
     * @param Context         the Context of the Server
     * @param Connection      the Connection this Variable belongs to
     * @param VariablesFolder the Folder of the Conenction all Variables are stored in
     */
    private void createNodes(String VariableName, UaNodeContext Context, PlcConnection Connection, UaFolderNode VariablesFolder) {
        VariableFolder = createVariableFolder(VariableName, Context);
        VariablesFolder.addOrganizes(VariableFolder);

        FieldAddressNode = createFieldAddressNode(Context);
        VariableFolder.addOrganizes(FieldAddressNode);

        ValueNode = createValueNode(Context, Connection, VariableName);
        VariableFolder.addOrganizes(ValueNode);

        NodesToAdd.add(VariableFolder);
        NodesToAdd.add(FieldAddressNode);
        NodesToAdd.add(ValueNode);
    }

    /**
     * creates the Folder all Nodes of the Variable are stored in
     *
     * @param VariableName a String representing the Variable, can be a usergiven Name or the FieldAddress
     * @param Context      the Context of the Server
     * @return returns the VariableFolder
     */
    private UaFolderNode createVariableFolder(String VariableName, UaNodeContext Context) {
        return createFolderNode(newNodeId((String) id.getIdentifier() + "/VariableFolder"), "Variable " + VariableName, Context);
    }

    /**
     * creates the VariableNode with the FieldAddress of the Variable as Value
     *
     * @param Context the Context of the Server
     * @return returns the FieldAddressNode
     */
    private UaVariableNode createFieldAddressNode(UaNodeContext Context) {
        UaVariableNode node = createVariableNodes(newNodeId((String) id.getIdentifier() + "/FieldAddress"), Context, "FieldAddress", Identifiers.String);
        node.setValue(new DataValue(new Variant(FieldAddress)));
        return node;
    }

    /**
     * creates the actual VariableNode and stores a Delegate as Value
     *
     * @param Context      the Context of the Server
     * @param Connection   the Connection this Variable belongs to
     * @param VariableName a String representing the Variable, can be a usergiven Name or the FieldAddress
     * @return returns the ValueNode
     */
    private UaVariableNode createValueNode(UaNodeContext Context, PlcConnection Connection, String VariableName) {
        UaVariableNode node;
        if (VariableName == FieldAddress) {
            node = createVariableNodes(id, Context, "Variable", DataType);
        } else {
            node = createVariableNodes(id, Context, VariableName, DataType);
        }
        node.setAttributeDelegate(buildDelegate(Connection));
        return node;
    }

    /**
     * bulids the Delegates that are stored in the Value of the ValueNode, when a read on this Value is executed
     * the Code in the Delegate is executed
     *
     * @param Connection the Connection this Variable belongs to
     * @return returns the Delegate
     */
    private AttributeDelegate buildDelegate(PlcConnection Connection) {

        //builds the delegates, they are stored in the Node instead of a Value and forward reads and writes to Plc4x
        AttributeDelegate delegate = new AttributeDelegate() {

            //read
            @Override
            public DataValue getValue(AttributeContext context, VariableNode node) {
                try {
                    return new DataValue(plc4xRead(Connection, FieldAddress));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (PlcRuntimeException e) {
                    return new DataValue(new Variant(null), StatusCode.BAD);
                }
                return null;
            }

            //write
            @Override
            public void setValue(AttributeContext context, VariableNode node, DataValue value) {
                try {
                    plc4xWrite(Connection, FieldAddress, value, DataType);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (PlcRuntimeException e) {
                    e.printStackTrace();
                }
            }

        };
        return delegate;
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
     * creates a Variant in which the code for a PLC4X-read call is stored
     *
     * @param connection   the Connection this Variable belongs to
     * @param FieldAddress the Adress of the Variable on the Server
     * @return returns the Variant with the code
     */
    private Variant plc4xRead(PlcConnection connection, String FieldAddress) throws ExecutionException, InterruptedException, PlcRuntimeException {
        PlcReadResponse response = connection.readRequestBuilder().addItem(FieldAddress, FieldAddress).build().execute().get();
        return new Variant(response.getObject(response.getFieldNames().iterator().next()));
    }

    /**
     * creates a Variant in which the code for a PLC4X-write call is stored
     *
     * @param connection   the Connection this Variable belongs to
     * @param FieldAddress the Adress of the Variable on the Server
     * @param Value        the Value which should be wrote on the PLC4X Server
     * @param DataType     the DataType of the Value
     * @return returns the Variant with the code
     */
    private void plc4xWrite(PlcConnection connection, String FieldAddress, DataValue Value, NodeId DataType) throws ExecutionException, InterruptedException, PlcRuntimeException {
        Object RightTypeValue = Value.getValue().getValue();
        BuiltinDataType.getBackingClass(DataType).cast(RightTypeValue);
        connection.writeRequestBuilder().addItem(FieldAddress, FieldAddress, RightTypeValue).build().execute().get();
    }

    /**
     * checks if the Connection the Varaible is belonging to has a readable tree and if
     * it gets the readableTreeVariablesFolder and creates a readable branch for this Variable
     *
     * @param FieldAddress     the Adress of the Variable on the Server
     * @param ConnectionString the Adress of a Server
     */
    private void createReadableTree(String FieldAddress, String ConnectionString) {
        if (ConnectionX.getConnectionX(ConnectionString).getReadableTreeType() != null) {
            if (ConnectionX.getConnectionX(ConnectionString).getReadableTreeType().equals("Ads")) {
                UaFolderNode VariablesFolder = ConnectionX.getConnectionX(ConnectionString).getReadableTreeVariablesFolder();
                createAdsVariableTree(FieldAddress, VariablesFolder);
            } else if (ConnectionX.getConnectionX(ConnectionString).getReadableTreeType().equals("S7")) {
                UaFolderNode VariablesFolder = ConnectionX.getConnectionX(ConnectionString).getReadableTreeVariablesFolder();
                createS7VariableTree(FieldAddress, parseS7FieldAddress(FieldAddress), VariablesFolder);
            }
        }
    }

    /**
     * checks if FieldAddress is a Symbolic or Direct Adress and calls the corresponding method
     *
     * @param FieldAddress    the Adress of the Variable on the Server
     * @param VariablesFolder the Folder this Variable should be added to
     */
    private void createAdsVariableTree(String FieldAddress, UaFolderNode VariablesFolder) {
        if (SymbolicAdsField.matches(FieldAddress)) {
            ConcurrentHashMap content = parseSymbolicAdsFieldAddress(FieldAddress);
            createSymbolicAdsVariableTree(content, VariablesFolder);
        } else if (DirectAdsField.matches(FieldAddress)) {
            ConcurrentHashMap content = parseDirectAdsFieldAddress(FieldAddress);
            createDirectAdsVariableTree(content, VariablesFolder);
        } else System.out.println("No ADS Variable found at: " + FieldAddress);

    }

    /**
     * pareses the Inforamtions of the FieldAddress and stores them in a HashMap
     *
     * @param FieldAddress the Adress of the Variable on the Server
     * @return returns a HashMap with the Inforamtions of the FieldAddress
     */
    private ConcurrentHashMap parseSymbolicAdsFieldAddress(String FieldAddress) {
        Matcher matcher = SYMBOLIC_ADDRESS_PATTERN.matcher(FieldAddress);
        matcher.matches();

        ConcurrentHashMap<String, String> content = new ConcurrentHashMap<String, String>();

        content.put("symbolicAddress", matcher.group("symbolicAddress"));
        content.put("adsDataType", matcher.group("adsDataType"));

        return content;
    }

    /**
     * pareses the Inforamtions of the FieldAddress and stores them in a HashMap
     *
     * @param FieldAddress the Adress of the Variable on the Server
     * @return returns a HashMap with the Inforamtions of the FieldAddress
     */
    private ConcurrentHashMap parseDirectAdsFieldAddress(String FieldAddress) {
        Matcher matcher = RESOURCE_ADDRESS_PATTERN.matcher(FieldAddress);
        matcher.matches();

        String indexGroupStringHex = matcher.group("indexGroupHex");
        String indexGroupString = matcher.group("indexGroup");
        String indexOffsetStringHex = matcher.group("indexOffsetHex");
        String indexOffsetString = matcher.group("indexOffset");
        if (indexGroupStringHex != null) {
            long indexGroup = Long.parseLong(indexGroupStringHex, 16);
            indexGroupString = Long.toString(indexGroup);
        }
        if (indexOffsetStringHex != null) {
            long indexOffset = Long.parseLong(indexOffsetStringHex, 16);
            indexOffsetString = Long.toString(indexOffset);
        }

        ConcurrentHashMap<String, String> content = new ConcurrentHashMap<String, String>();

        content.put("indexGroup", indexGroupString);
        content.put("indexOffset", indexOffsetString);
        content.put("adsDataType", matcher.group("adsDataType"));

        return content;
    }

    /**
     * this method takes the Informations of the HashMap and creates a readable Tree representation in OPC UA
     *
     * @param content         a Hashmap with the Inforamtions of the FieldAddress
     * @param VariablesFolder the Folder this Variable should be added to
     */
    private void createDirectAdsVariableTree(ConcurrentHashMap content, UaFolderNode VariablesFolder) {
        UaFolderNode AdsVariable = createFolderNode(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/AdsVariableFolder"), "Variable " + VariableName, Context);
        VariablesFolder.addOrganizes(AdsVariable);

        UaVariableNode AdsIndexGroupNode = createVariableNodes(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/AdsIndexGroup"), Context, "IndexGroup", Identifiers.String);
        AdsIndexGroupNode.setValue(new DataValue(new Variant(content.get("indexGroup"))));
        AdsVariable.addOrganizes(AdsIndexGroupNode);

        UaVariableNode AdsIndexOffsetNode = createVariableNodes(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/AdsIndexOffset"), Context, "IndexOffset", Identifiers.String);
        AdsIndexOffsetNode.setValue(new DataValue(new Variant(content.get("indexOffset"))));
        AdsVariable.addOrganizes(AdsIndexOffsetNode);

        UaVariableNode AdsDataTypeNode = createVariableNodes(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/AdsDataType"), Context, "DataType", Identifiers.String);
        AdsDataTypeNode.setValue(new DataValue(new Variant(content.get("adsDataType"))));
        AdsVariable.addOrganizes(AdsDataTypeNode);

        AdsVariable.addOrganizes(ValueNode);

        NodesToAdd.add(AdsVariable);
        NodesToAdd.add(AdsIndexGroupNode);
        NodesToAdd.add(AdsIndexOffsetNode);
        NodesToAdd.add(AdsDataTypeNode);
    }

    /**
     * this method takes the Informations of the HashMap and creates a readable Tree representation in OPC UA
     *
     * @param content         a Hashmap with the Inforamtions of the FieldAddress
     * @param VariablesFolder the Folder this Variable should be added to
     */
    private void createSymbolicAdsVariableTree(ConcurrentHashMap content, UaFolderNode VariablesFolder) {
        UaFolderNode AdsVariable = createFolderNode(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/AdsVariableFolder"), "Variable " + VariableName, Context);
        VariablesFolder.addOrganizes(AdsVariable);

        UaVariableNode AdsSymbolicAddressNode = createVariableNodes(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/AdsSymbolicAddress"), Context, "SymbolicAddress", Identifiers.String);
        AdsSymbolicAddressNode.setValue(new DataValue(new Variant(content.get("symbolicAddress"))));
        AdsVariable.addOrganizes(AdsSymbolicAddressNode);

        UaVariableNode AdsDataTypeNode = createVariableNodes(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/AdsDataType"), Context, "DataType", Identifiers.String);
        AdsDataTypeNode.setValue(new DataValue(new Variant(content.get("adsDataType"))));
        AdsVariable.addOrganizes(AdsDataTypeNode);

        AdsVariable.addOrganizes(ValueNode);

        NodesToAdd.add(AdsVariable);
        NodesToAdd.add(AdsSymbolicAddressNode);
        NodesToAdd.add(AdsDataTypeNode);
    }

    /**
     * pareses the Inforamtions of the FieldAddress and stores them in a HashMap
     *
     * @param FieldAddress the Adress of the Variable on the Server
     * @return returns a HashMap with the Inforamtions of the FieldAddress
     */
    private ConcurrentHashMap parseS7FieldAddress(String FieldAddress) {
        S7Field s7Field = S7Field.of(FieldAddress);

        ConcurrentHashMap<String, String> content = new ConcurrentHashMap<String, String>();

        content.put("BitOffset", Short.toString(s7Field.getBitOffset()));
        content.put("ByteOffset", Integer.toString(s7Field.getByteOffset()));
        content.put("BlockNumber", Integer.toString(s7Field.getBlockNumber()));

        if (DATA_BLOCK_ADDRESS_PATTERN.matcher(FieldAddress).matches()) {
            Matcher matcher = DATA_BLOCK_ADDRESS_PATTERN.matcher(FieldAddress);
            matcher.matches();

            content.put("MemoryArea", "DB");
            content.put("DataType", matcher.group("dataType"));
        } else {
            Matcher matcher = ADDRESS_PATTERN.matcher(FieldAddress);
            matcher.matches();

            content.put("MemoryArea", matcher.group("memoryArea"));
            content.put("DataType", matcher.group("dataType"));
        }

        return content;
    }

    /**
     * this method takes the Informations of the HashMap and creates a readable Tree representation in OPC UA
     *
     * @param content         a Hashmap with the Inforamtions of the FieldAddress
     * @param VariablesFolder the Folder this Variable should be added to
     */
    private void createS7VariableTree(String FieldAddress, ConcurrentHashMap content, UaFolderNode VariablesFolder) {
        UaFolderNode S7Variable = createFolderNode(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/S7VariableFolder"), "Variable " + VariableName, Context);
        VariablesFolder.addOrganizes(S7Variable);

        UaVariableNode S7BitOffsetNode = createVariableNodes(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/S7BitOffset"), Context, "BitOffset", Identifiers.String);
        S7BitOffsetNode.setValue(new DataValue(new Variant(content.get("BitOffset"))));
        S7Variable.addOrganizes(S7BitOffsetNode);

        UaVariableNode S7ByteOffsetNode = createVariableNodes(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/S7ByteOffset"), Context, "ByteOffset", Identifiers.String);
        S7ByteOffsetNode.setValue(new DataValue(new Variant(content.get("ByteOffset"))));
        S7Variable.addOrganizes(S7ByteOffsetNode);

        UaVariableNode S7BlockNumberNode = createVariableNodes(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/S7BlockNumber"), Context, "BlockNumber", Identifiers.String);
        S7BlockNumberNode.setValue(new DataValue(new Variant(content.get("BlockNumber"))));
        S7Variable.addOrganizes(S7BlockNumberNode);

        UaVariableNode S7MemoryAreaNode = createVariableNodes(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/S7MemoryArea"), Context, "MemoryArea", Identifiers.String);
        S7MemoryAreaNode.setValue(new DataValue(new Variant(content.get("MemoryArea"))));
        S7Variable.addOrganizes(S7MemoryAreaNode);

        UaVariableNode S7DataTypeNode = createVariableNodes(newNodeId((String) ValueNode.getNodeId().getIdentifier() + "/S7DataType"), Context, "DataType", Identifiers.String);
        S7DataTypeNode.setValue(new DataValue(new Variant(content.get("DataType"))));
        S7Variable.addOrganizes(S7DataTypeNode);

        S7Variable.addOrganizes(ValueNode);

        NodesToAdd.add(S7Variable);
        NodesToAdd.add(S7BitOffsetNode);
        NodesToAdd.add(S7ByteOffsetNode);
        NodesToAdd.add(S7BlockNumberNode);
        NodesToAdd.add(S7MemoryAreaNode);
        NodesToAdd.add(S7DataTypeNode);

    }


    //Just copied this from Eclipse Milo's UaNode Class
    public synchronized void addAttributeObserver(AttributeObserver observer) {
        if (observers == null) {
            observers = new LinkedList<>();
        }

        observers.add(new WeakReference<>(observer));
    }

    public synchronized void removeAttributeObserver(AttributeObserver observer) {
        if (observers == null) return;

        Iterator<WeakReference<AttributeObserver>> iterator = observers.iterator();

        while (iterator.hasNext()) {
            WeakReference<AttributeObserver> ref = iterator.next();
            if (ref.get() == null || ref.get() == observer) {
                iterator.remove();
            }
        }

        if (observers.isEmpty()) observers = null;
    }

    protected synchronized void fireAttributeChanged(AttributeId attributeId, Object attributeValue) {
        if (observers == null) return;

        Iterator<WeakReference<AttributeObserver>> iterator = observers.iterator();

        while (iterator.hasNext()) {
            WeakReference<AttributeObserver> ref = iterator.next();
            AttributeObserver observer = ref.get();
            if (observer != null) {
                observer.attributeChanged(ValueNode, attributeId, attributeValue);
            } else {
                iterator.remove();
            }
        }
    }

}


