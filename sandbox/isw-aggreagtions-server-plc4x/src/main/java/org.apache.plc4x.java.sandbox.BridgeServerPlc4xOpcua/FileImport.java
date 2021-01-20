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

import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.api.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.*;
import org.eclipse.milo.opcua.sdk.server.nodes.delegates.AttributeDelegate;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.apache.plc4x.java.sandbox.BridgeServerPlc4xOpcua.VariableX.newQualifiedName;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;

public class FileImport {

    //TODO Work on this one is still in Progress and it's not in use in this Version. But can be played with.
    private GetIdentifiers getIdentifiers;
    private UaNodeContext context;
    private ArrayList<ReferenceInformation> referenceInformations;
    private ArrayList<UaNode> nodesToAdd;
    private PlcConnection connection;
    private String latestConnectionstring;

    public FileImport() {
        this.getIdentifiers = new GetIdentifiers();
        this.context = AggregationsNamespace.get().getUaNodecontext();
    }

    public static void main(String[] args) {

        String baba = "OrginalOpcUaServerNodeId:BabediBubediBangerang!ThisIsIt";
        if (baba.startsWith("OrginalOpcUaServerNodeId:")) {
            String bibi[] = baba.split("OrginalOpcUaServerNodeId:");
            System.out.println(bibi[1]);
        }

        //FileImport fileImport = new FileImport();
        //File file = new File("/Users/felix/Desktop/config.xml");
        //fileImport.handleConfigurationFile(file);
    }

    public void handleConfigurationFile(File file) {
        Node node = xmlToDocumentCore(file).getDocumentElement();

        ArrayList<Node> arrayList = getNodeChildren(node);
        for (Node node1 : arrayList) {
            buildDevice(node1);
        }
        //buildDevice(arrayList.get(1));
    }

    private void buildDevice(Node node) {
        referenceInformations = new ArrayList<>();
        nodesToAdd = new ArrayList<>();
        connection = null;

        String elementId = null;
        String deviceName = null;
        String protocol = null;
        Node connectionInfo = null;
        Node additionalDeviceInfo = null;
        Node deviceElements = null;

        ArrayList<Node> nodeList = getNodeChildren(node);
        for (Node deviceAttribute : nodeList) {
            switch (deviceAttribute.getNodeName()) {

                case "elementId":
                    elementId = deviceAttribute.getTextContent();
                    break;

                case "deviceName":
                    deviceName = deviceAttribute.getTextContent();
                    break;

                case "protocol":
                    protocol = deviceAttribute.getTextContent();
                    break;

                case "ConnectionInfo":
                    connectionInfo = deviceAttribute;
                    break;

                case "AdditionalDeviceInformation":
                    additionalDeviceInfo = deviceAttribute;
                    break;

                case "deviceElements":
                    deviceElements = deviceAttribute;
                    break;
            }
        }

        UaFolderNode connectionFolder = buildConnectionInfo(connectionInfo, protocol, deviceName);
        UaFolderNode elementsFolder = buildElementsFromList(deviceElements);
        ArrayList<UaNode> deviceNodes = buildDeviceNodes(protocol, additionalDeviceInfo);

        UaFolderNode deviceFolder = buildFolder(new NodeId(2, elementId), deviceName);
        nodesToAdd.add(deviceFolder);
        for (UaNode deviceNode : deviceNodes) {
            deviceFolder.addOrganizes(deviceNode);
            nodesToAdd.add(deviceNode);
        }
        deviceFolder.addOrganizes(connectionFolder);
        deviceFolder.addOrganizes(elementsFolder);

        AggregationsNamespace.get().addNodes(nodesToAdd);
        AggregationsNamespace.get().rootNode.addOrganizes(deviceFolder);
    }

    private UaFolderNode buildConnectionInfo(Node node, String protocol, String deviceName) {
        String transportProtocol = null;
        String host = null;
        String port = null;
        String ip = null;
        String uri = null;
        Node additionalConnectionInfo = null;

        ArrayList<Node> nodeList = getNodeChildren(node);
        for (Node connectionAttribute : nodeList) {
            switch (connectionAttribute.getNodeName()) {

                case "transportProtocol":
                    transportProtocol = connectionAttribute.getTextContent();
                    break;

                case "host":
                    host = connectionAttribute.getTextContent();
                    break;

                case "port":
                    port = connectionAttribute.getTextContent();
                    break;

                case "ip":
                    ip = connectionAttribute.getTextContent();
                    break;

                case "uri":
                    uri = connectionAttribute.getTextContent();
                    break;

                case "AdditionalConnectionInformation":
                    additionalConnectionInfo = connectionAttribute;
                    break;
            }
        }

        String ipOrHost = null;
        if (!ip.equals("not initialized")) ipOrHost = ip;
        if (!host.equals("not initialized")) ipOrHost = host;

        String connectionString = null;
        if ((uri == null || uri.isEmpty()) && (transportProtocol.matches("tcp") || transportProtocol.matches("not initialized"))) {
            connectionString = protocol.toLowerCase() + ":" + transportProtocol.toLowerCase() + "://" + ipOrHost + ":" + port;
        } else {
            if (uri.startsWith("opc.tcp:")) {
                connectionString = uri.replace("opc.tcp:", "opcua:tcp:");
            } else {
                connectionString = uri;
            }
        }

        connection = getConnection(connectionString);
        ArrayList<UaNode> connectionNodes = buildConnectionNodes(transportProtocol, host, port, ip, additionalConnectionInfo);
        if (!uri.matches("not initialized")) {
            connectionNodes.add(buildStringNode("Uri", connectionString));
        }
        latestConnectionstring = connectionString;
        UaFolderNode connectionFolder = buildFolder(new NodeId(2, deviceName + "-ConnectionFolder-" + UUID.randomUUID()), deviceName + "-ConnectionInformation");
        nodesToAdd.add(connectionFolder);
        for (UaNode connNode : connectionNodes) {
            connectionFolder.addOrganizes(connNode);
            nodesToAdd.add(connNode);
        }
        ConnectionX.Connections.put(connectionString, new ConnectionX(connectionString, deviceName, AggregationsNamespace.get().getUaNodecontext(), connection));

        return connectionFolder;
    }

    private UaFolderNode buildElementsFromList(Node node) {
        ArrayList<Node> nodeList = getNodeChildren(node);
        UaFolderNode elements = buildFolder(new NodeId(2, "Elements-" + UUID.randomUUID()), "Elements");
        nodesToAdd.add(elements);
        for (Node entry : nodeList) {
            ArrayList<Node> entryNodes = getNodeChildren(entry);
            for (Node entryNode : entryNodes) {
                if (entryNode.getNodeName().matches("value")) {
                    elements.addOrganizes(buildElement(entryNode));
                }
            }
        }
        return elements;
    }

    private UaFolderNode buildElement(Node node) {
        ArrayList<UaNode> elementNodes = new ArrayList<>();
        UaNode varNode = null;

        String elementId = null;
        String onDeviceId = null;
        String browseName = null;
        Integer writeMask = null;
        Integer userWriteMask = null;
        String symbolicName = null;
        String displayName = null;
        String description = null;
        String nodeVersion = null;
        String category = null;
        String documentation = null;

        //Special
        Node additionalElementInfo = null;
        Node references = null;

        //Variable
        String dataType = null;
        Integer valueRank = null;
        String arrayDimensions = null;
        Integer accessLevel = null;
        Integer userAccessLevel = null;
        Integer minimumSamplingInterval = null;
        Boolean historizing = null;
        Boolean allowNulls = null;
        String dataTypeVersion = null;
        ByteString dictionaryFragment = null;

        //DeviceObject
        Integer eventNotifier = null;
        //TODO
        ByteString icon = null;

        //DeviceMethod
        Boolean executable = null;
        Boolean userExecutable = null;

        //DeviceDataType
        Integer size = null;
        Boolean primitive = null;
        //String dataType = null;
        Boolean isAbstract = null;

        //DeviceVariableType
        //String arrayDimensions = null;
        //Integer valueRank = null;
        //String dataType = null;
        //Boolean isAbstract = null;

        //DeviceObjectType
        //ByteString icon = null;
        //Boolean isAbstract = null;

        //DeviceReferenceType
        String inverseName = null;
        //Boolean isAbstract = null;
        Boolean symmetric = null;


        //Switch
        ArrayList<Node> nodeList = getNodeChildren(node);
        for (Node elementAttribute : nodeList) {
            switch (elementAttribute.getNodeName()) {

                case "elementId":
                    elementId = elementAttribute.getTextContent();
                    break;

                case "onDeviceId":
                    onDeviceId = elementAttribute.getTextContent();
                    break;

                case "browseName":
                    browseName = elementAttribute.getTextContent();
                    break;

                case "writeMask":
                    writeMask = Integer.valueOf(elementAttribute.getTextContent());
                    break;

                case "userWriteMask":
                    userWriteMask = Integer.valueOf(elementAttribute.getTextContent());
                    break;

                case "symbolicName":
                    symbolicName = elementAttribute.getTextContent();
                    break;

                case "displayName":
                    displayName = elementAttribute.getTextContent();
                    break;

                case "description":
                    description = elementAttribute.getTextContent();
                    break;

                case "nodeVersion":
                    nodeVersion = elementAttribute.getTextContent();
                    break;

                case "category":
                    category = elementAttribute.getTextContent();
                    break;

                case "documentation":
                    documentation = elementAttribute.getTextContent();
                    break;

                case "dataType":
                    dataType = elementAttribute.getTextContent();
                    break;

                case "valueRank":
                    valueRank = Integer.valueOf(elementAttribute.getTextContent());
                    break;

                case "arrayDimensions":
                    arrayDimensions = elementAttribute.getTextContent();
                    break;

                case "accessLevel":
                    accessLevel = Integer.valueOf(elementAttribute.getTextContent());
                    break;

                case "userAccessLevel":
                    userAccessLevel = Integer.valueOf(elementAttribute.getTextContent());
                    break;

                case "minimumSamplingInterval":
                    minimumSamplingInterval = Integer.valueOf(elementAttribute.getTextContent());
                    break;

                case "historizing":
                    historizing = Boolean.valueOf(elementAttribute.getTextContent());
                    break;

                case "allowNulls":
                    allowNulls = Boolean.valueOf(elementAttribute.getTextContent());
                    break;

                case "dataTypeVersion":
                    dataTypeVersion = elementAttribute.getTextContent();
                    break;

                case "dictionaryFragment":
                    dictionaryFragment = ByteString.of(elementAttribute.getTextContent().getBytes(StandardCharsets.UTF_8));
                    break;

                case "eventNotifier":
                    eventNotifier = Integer.valueOf(elementAttribute.getTextContent());
                    break;

                case "icon":
                    icon = ByteString.of(elementAttribute.getTextContent().getBytes(StandardCharsets.UTF_8));
                    break;

                case "executable":
                    executable = Boolean.valueOf(elementAttribute.getTextContent());
                    break;

                case "userExecutable":
                    userExecutable = Boolean.valueOf(elementAttribute.getTextContent());
                    break;

                case "size":
                    size = Integer.valueOf(elementAttribute.getTextContent());
                    break;

                case "primitive":
                    primitive = Boolean.valueOf(elementAttribute.getTextContent());
                    break;

                case "isAbstract":
                    isAbstract = Boolean.valueOf(elementAttribute.getTextContent());
                    break;

                case "inverseName":
                    inverseName = elementAttribute.getTextContent();
                    break;

                case "symmetric":
                    symmetric = Boolean.valueOf(elementAttribute.getTextContent());
                    break;

                case "AdditionalElementInformation":
                    additionalElementInfo = elementAttribute;
                    break;

                case "References":
                    references = elementAttribute;
                    break;
            }
        }

        Integer NodeClassInt = null;
        for (Map.Entry<String, String> entry : getAdditionalInfo(additionalElementInfo).entrySet()) {
            if (entry.getKey().matches("NodeClassInt")) NodeClassInt = Integer.parseInt(entry.getValue());
            else elementNodes.add(buildStringNode(entry.getKey(), entry.getValue()));
        }

        if (NodeClassInt != null) {
            //get right Type of Node in OPC UA
            switch (NodeClassInt) {
                case 1: //Object
                    UaObjectNode uaObjectNode = new UaObjectNode(context,
                        new NodeId(2, elementId),
                        newQualifiedName(browseName),
                        new LocalizedText(browseName),
                        new LocalizedText(description),
                        UInteger.valueOf(writeMask),
                        UInteger.valueOf(userWriteMask),
                        UByte.valueOf(eventNotifier));
                    uaObjectNode.setNodeVersion(nodeVersion);
                    varNode = uaObjectNode;
                    break;

                case 2: //Variable
                    NodeId thisId = new NodeId(2, elementId);
                    UaVariableNode uaVariableNode = new UaVariableNode.UaVariableNodeBuilder(context)
                        .setNodeId(thisId)
                        .setBrowseName(QualifiedName.parse(browseName))
                        .setWriteMask(UInteger.valueOf(writeMask))
                        .setUserWriteMask(UInteger.valueOf(userWriteMask))
                        .setDisplayName(LocalizedText.english(displayName))
                        .setDescription(LocalizedText.english(description))
                        .setDataType(getIdentifiers.getNodeId(dataType.toLowerCase()))
                        .setValueRank(valueRank)
                        .setArrayDimensions(stringToIntArray(arrayDimensions))
                        .setAccessLevel(UByte.valueOf(accessLevel))
                        .setUserAccessLevel(UByte.valueOf(userAccessLevel))
                        .setMinimumSamplingInterval(Double.valueOf(minimumSamplingInterval))
                        .setHistorizing(historizing)
                        .build();
                    uaVariableNode.setNodeVersion(nodeVersion);
                    uaVariableNode.setAllowNulls(allowNulls);
                    uaVariableNode.setDataTypeVersion(dataTypeVersion);
                    uaVariableNode.setDictionaryFragment(dictionaryFragment);
                    uaVariableNode.setAttributeDelegate(buildDelegate(onDeviceId, getIdentifiers.getNodeId(dataType.toLowerCase())));
                    varNode = uaVariableNode;
                    break;

                case 4: //Method
                    UaMethodNode uaMethodNode = new UaMethodNode(context,
                        new NodeId(2, elementId),
                        newQualifiedName(browseName),
                        new LocalizedText(displayName),
                        new LocalizedText(description),
                        UInteger.valueOf(writeMask),
                        UInteger.valueOf(userWriteMask),
                        executable,
                        userExecutable);
                    uaMethodNode.setNodeVersion(nodeVersion);
                    varNode = uaMethodNode;
                    break;

                case 8: //ObjectType
                    UaObjectTypeNode uaObjectTypeNode = new UaObjectTypeNode(context,
                        new NodeId(2, elementId),
                        newQualifiedName(browseName),
                        new LocalizedText(displayName),
                        new LocalizedText(description),
                        UInteger.valueOf(writeMask),
                        UInteger.valueOf(userWriteMask),
                        isAbstract);
                    varNode = uaObjectTypeNode;
                    break;

                case 16: //VariableType
                    UaVariableTypeNode uaVariableTypeNode = new UaVariableTypeNode(context,
                        new NodeId(2, elementId),
                        newQualifiedName(browseName),
                        new LocalizedText(displayName),
                        new LocalizedText(description),
                        UInteger.valueOf(writeMask),
                        UInteger.valueOf(userWriteMask),
                        null,
                        getIdentifiers.getNodeId(dataType.toLowerCase()),
                        valueRank,
                        stringToIntArray(arrayDimensions),
                        isAbstract);
                    varNode = uaVariableTypeNode;
                    break;

                case 32: //ReferenceType
                    UaReferenceTypeNode uaReferenceTypeNode = new UaReferenceTypeNode(context,
                        new NodeId(2, elementId),
                        newQualifiedName(browseName),
                        new LocalizedText(displayName),
                        new LocalizedText(description),
                        UInteger.valueOf(writeMask),
                        UInteger.valueOf(userWriteMask),
                        isAbstract,
                        symmetric,
                        new LocalizedText(inverseName));
                    uaReferenceTypeNode.setNodeVersion(nodeVersion);
                    varNode = uaReferenceTypeNode;
                    break;

                case 64: //DataType
                    UaDataTypeNode uaDataTypeNode = new UaDataTypeNode(context,
                        new NodeId(2, elementId),
                        newQualifiedName(browseName),
                        new LocalizedText(displayName),
                        new LocalizedText(description),
                        UInteger.valueOf(writeMask),
                        UInteger.valueOf(userWriteMask),
                        isAbstract);
                    uaDataTypeNode.setNodeVersion(nodeVersion);
                    varNode = uaDataTypeNode;
                    break;
            }
        } else {
            //get right Type of Node complicated and bad way
            if (dataType == null || dataType.isEmpty()) {
                //reftype,objtype,meth,obj
                if (isAbstract == null) {
                    //reftyp,objtyp
                    if (inverseName == null || inverseName.isEmpty()) {
                        //objtyp
                        UaObjectTypeNode uaObjectTypeNode = new UaObjectTypeNode(context,
                            new NodeId(2, elementId),
                            newQualifiedName(browseName),
                            new LocalizedText(displayName),
                            new LocalizedText(description),
                            UInteger.valueOf(writeMask),
                            UInteger.valueOf(userWriteMask),
                            isAbstract);
                        varNode = uaObjectTypeNode;
                    } else {
                        //reftyp
                        UaReferenceTypeNode uaReferenceTypeNode = new UaReferenceTypeNode(context,
                            new NodeId(2, elementId),
                            newQualifiedName(browseName),
                            new LocalizedText(displayName),
                            new LocalizedText(description),
                            UInteger.valueOf(writeMask),
                            UInteger.valueOf(userWriteMask),
                            isAbstract,
                            symmetric,
                            new LocalizedText(inverseName));
                        uaReferenceTypeNode.setNodeVersion(nodeVersion);
                        varNode = uaReferenceTypeNode;
                    }
                } else {
                    //meth,obj
                    if (executable == null) {
                        //meth
                        UaMethodNode uaMethodNode = new UaMethodNode(context,
                            new NodeId(2, elementId),
                            newQualifiedName(browseName),
                            new LocalizedText(displayName),
                            new LocalizedText(description),
                            UInteger.valueOf(writeMask),
                            UInteger.valueOf(userWriteMask),
                            executable,
                            userExecutable);
                        uaMethodNode.setNodeVersion(nodeVersion);
                        varNode = uaMethodNode;
                    } else {
                        //obj
                        UaObjectNode uaObjectNode = new UaObjectNode(context,
                            new NodeId(2, elementId),
                            newQualifiedName(browseName),
                            new LocalizedText(browseName),
                            new LocalizedText(description),
                            UInteger.valueOf(writeMask),
                            UInteger.valueOf(userWriteMask),
                            UByte.valueOf(eventNotifier));
                        uaObjectNode.setNodeVersion(nodeVersion);
                        varNode = uaObjectNode;
                    }
                }
            } else {
                //vartype,datatype,var
                if (isAbstract == null) {
                    //vartyp,datatyp
                    if (valueRank == null) {
                        //datatyp
                        UaDataTypeNode uaDataTypeNode = new UaDataTypeNode(context,
                            new NodeId(2, elementId),
                            newQualifiedName(browseName),
                            new LocalizedText(displayName),
                            new LocalizedText(description),
                            UInteger.valueOf(writeMask),
                            UInteger.valueOf(userWriteMask),
                            false);
                        uaDataTypeNode.setNodeVersion(nodeVersion);
                        varNode = uaDataTypeNode;
                    } else {
                        //vartyp
                        UaVariableTypeNode uaVariableTypeNode = new UaVariableTypeNode(context,
                            new NodeId(2, elementId),
                            newQualifiedName(browseName),
                            new LocalizedText(displayName),
                            new LocalizedText(description),
                            UInteger.valueOf(writeMask),
                            UInteger.valueOf(userWriteMask),
                            null,
                            getIdentifiers.getNodeId(dataType.toLowerCase()),
                            valueRank,
                            stringToIntArray(arrayDimensions),
                            isAbstract);
                        varNode = uaVariableTypeNode;
                    }
                } else {
                    //var
                    NodeId thisId = new NodeId(2, elementId);
                    UaVariableNode uaVariableNode = new UaVariableNode.UaVariableNodeBuilder(context)
                        .setNodeId(thisId)
                        .setBrowseName(QualifiedName.parse(browseName))
                        .setWriteMask(UInteger.valueOf(writeMask))
                        .setUserWriteMask(UInteger.valueOf(userWriteMask))
                        .setDisplayName(LocalizedText.english(displayName))
                        .setDescription(LocalizedText.english(description))
                        .setDataType(getIdentifiers.getNodeId(dataType.toLowerCase()))
                        .setValueRank(valueRank)
                        .setArrayDimensions(stringToIntArray(arrayDimensions))
                        .setAccessLevel(UByte.valueOf(accessLevel))
                        .setUserAccessLevel(UByte.valueOf(userAccessLevel))
                        .setMinimumSamplingInterval(Double.valueOf(minimumSamplingInterval))
                        .setHistorizing(historizing)
                        .build();
                    uaVariableNode.setNodeVersion(nodeVersion);
                    uaVariableNode.setAllowNulls(allowNulls);
                    uaVariableNode.setDataTypeVersion(dataTypeVersion);
                    uaVariableNode.setDictionaryFragment(dictionaryFragment);
                    uaVariableNode.setAttributeDelegate(buildDelegate(onDeviceId, getIdentifiers.getNodeId(dataType.toLowerCase())));
                    varNode = uaVariableNode;
                }
            }
        }

        elementNodes.add(varNode);
        new DataItemX(new NodeId(2, elementId), latestConnectionstring, onDeviceId);
        elementNodes.add(buildStringNode("FieldAddress", onDeviceId));
        if (symbolicName != null && !symbolicName.isEmpty()) {
            elementNodes.add(buildStringNode("SymbolicName", symbolicName));
        }
        if (category != null && !category.isEmpty()) {
            elementNodes.add(buildStringNode("Category", category));
        }
        if (documentation != null && !documentation.isEmpty()) {
            elementNodes.add(buildStringNode("Comment", documentation));
        }
        if (size != null) {
            elementNodes.add(buildStringNode("Size", size.toString()));
        }
        if (primitive != null) {
            elementNodes.add(buildStringNode("Primitive", primitive.toString()));
        }
        if (dataType != null) {
            elementNodes.add(buildStringNode("DataType", dataType));
        }

        UaFolderNode variableFolder = buildFolder(new NodeId(2, browseName + "-Folder-" + UUID.randomUUID()), browseName);
        nodesToAdd.add(variableFolder);
        getReferenceInformation(references, varNode);
        buildReferences();
        for (UaNode elementNode : elementNodes) {
            variableFolder.addOrganizes(elementNode);
            nodesToAdd.add(elementNode);
        }
        return variableFolder;
    }

    private void getReferenceInformation(Node node, UaNode varNode) {
        ArrayList<Node> nodeList = getNodeChildren(node);
        for (Node reference : nodeList) {
            String targetId = null;
            String sourceId = null;
            String referenceType = null;
            Boolean isForward = null;
            Integer order = null;

            ArrayList<Node> referenceAttributes = getNodeChildren(reference);
            for (Node referenceAttribute : referenceAttributes) {
                switch (referenceAttribute.getNodeName()) {

                    case "targetId":
                        targetId = referenceAttribute.getTextContent();
                        break;

                    case "sourceId":
                        sourceId = referenceAttribute.getTextContent();
                        break;

                    case "referenceType":
                        referenceType = referenceAttribute.getTextContent();
                        break;

                    case "isForward":
                        isForward = Boolean.valueOf(referenceAttribute.getTextContent());
                        break;

                    case "order":
                        order = Integer.valueOf(referenceAttribute.getTextContent());
                        break;
                }
            }
            if (targetId.startsWith("OrginalOpcUaServerNodeId:")) {
                String[] onlyTargetId = targetId.split("OrginalOpcUaServerNodeId:");
                if (onlyTargetId[1].startsWith("ns=")) {
                    String[] onlyTargetIdWithOutNsParts = onlyTargetId[1].split(";");
                    String onlyTargetIdWithOutNs = "";
                    for (int k = 1; k < onlyTargetIdWithOutNsParts.length; k++) {
                        onlyTargetIdWithOutNs = onlyTargetIdWithOutNs + onlyTargetIdWithOutNsParts[k];
                    }
                    targetId = onlyTargetIdWithOutNs;
                }
            }

            referenceInformations.add(new ReferenceInformation(new NodeId(2, targetId),
                new NodeId(2, sourceId), getIdentifiers.getNodeId(referenceType.toLowerCase()),
                isForward, order, varNode));
        }
    }

    private void buildReferences() {
        for (ReferenceInformation refInfo : referenceInformations) {
            UaNode sourceNode = refInfo.getVarNode();
            sourceNode.addReference(new Reference(
                refInfo.sourceId,
                refInfo.refType,
                refInfo.targetId.expanded(),
                refInfo.isForward
            ));
        }
    }

    /**
     * converts a File to an Document
     *
     * @param file the File to convert
     * @return the converted Document
     */
    private Document xmlToDocumentCore(File file) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(file);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        System.out.println("Error while parsing file!");
        return null;
    }

    /**
     * gets all NodeChildren of the node and returns it as a ArrayList
     *
     * @param node the parentNode
     * @return a Arraylist with the Node children
     */
    private ArrayList<Node> getNodeChildren(Node node) {
        ArrayList<Node> returnList = new ArrayList<>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                returnList.add(children.item(i));
            }
        }
        return returnList;
    }

    private UaFolderNode buildFolder(NodeId id, String deviceName) {
        return new UaFolderNode(
            context,
            id,
            new QualifiedName(2, deviceName),
            LocalizedText.english(deviceName)
        );
    }

    private ArrayList<UaNode> buildDeviceNodes(String protocol, Node additionalDeviceInfo) {
        ArrayList<UaNode> deviceNodes = new ArrayList<>();

        if (!protocol.matches("not initialized")) {
            deviceNodes.add(buildStringNode("protocol", protocol));
        }
        for (Map.Entry<String, String> entry : getAdditionalInfo(additionalDeviceInfo).entrySet()) {
            deviceNodes.add(buildStringNode(entry.getKey(), entry.getValue()));
        }

        return deviceNodes;
    }

    private ArrayList<UaNode> buildConnectionNodes(String transportProtocol, String host, String port, String ip, Node additionalConnectionInfo) {
        ArrayList<UaNode> connectionNodes = new ArrayList<>();
        if (!transportProtocol.matches("not initialized")) {
            connectionNodes.add(buildStringNode("TransportProtocol", transportProtocol));
        }
        if (!host.matches("not initialized")) {
            connectionNodes.add(buildStringNode("Host", host));
        }
        if (!port.matches("0")) {
            connectionNodes.add(buildStringNode("Port", port));
        }
        if (!ip.matches("not initialized")) {
            connectionNodes.add(buildStringNode("Ip", ip));
        }
        for (Map.Entry<String, String> entry : getAdditionalInfo(additionalConnectionInfo).entrySet()) {
            connectionNodes.add(buildStringNode(entry.getKey(), entry.getValue()));
        }
        return connectionNodes;
    }

    private UaVariableNode buildStringNode(String name, String value) {
        return new UaVariableNode.UaVariableNodeBuilder(context)
            .setNodeId(new NodeId(2, name + "-" + UUID.randomUUID()))
            .setAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE))
            .setUserAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE))
            .setBrowseName(newQualifiedName(name))
            .setDisplayName(LocalizedText.english(name))
            .setDataType(Identifiers.String)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .setValue(new DataValue(new Variant(value)))
            .build();
    }

    private HashMap<String, String> getAdditionalInfo(Node node) {
        HashMap<String, String> additionalInfo = new HashMap<>();
        ArrayList<Node> infoNodes = getNodeChildren(node);
        for (Node infoNode : infoNodes) {
            additionalInfo.put(infoNode.getNodeName(), infoNode.getTextContent());
        }
        return additionalInfo;
    }

    private UInteger[] stringToIntArray(String string) {
        String[] stringParts = string.split(",");
        UInteger[] intArray = new UInteger[stringParts.length];
        intArray = null;
        if (string.isEmpty()) {
            for (int h = 0; h < stringParts.length; h++) {
                if (!stringParts[h].isEmpty()) {
                    intArray[h] = UInteger.valueOf(stringParts[h]);
                }
            }
        }
        return intArray;
    }

    private PlcConnection getConnection(String connectionString) {
        try {
            return new PlcDriverManager().getConnection(connectionString);
        } catch (PlcConnectionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * bulids the Delegates that are stored in the Value of the ValueNode, when a read on this Value is executed
     * the Code in the Delegate is executed
     *
     * @return returns the Delegate
     */
    private AttributeDelegate buildDelegate(String FieldAddress, NodeId dataType) {

        //builds the delegates, they are stored in the Node instead of a Value and forward reads and writes to Plc4x
        AttributeDelegate delegate = new AttributeDelegate() {

            //read
            @Override
            public DataValue getValue(AttributeContext context, VariableNode node) {
                try {
                    return new DataValue(plc4xRead(FieldAddress));
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
                    plc4xWrite(FieldAddress, value, dataType);
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
     * creates a Variant in which the code for a PLC4X-read call is stored
     *
     * @param FieldAddress the Adress of the Variable on the Server
     * @return returns the Variant with the code
     */
    private Variant plc4xRead(String FieldAddress) throws ExecutionException, InterruptedException, PlcRuntimeException {
        PlcReadResponse response = connection.readRequestBuilder().addItem(FieldAddress, FieldAddress).build().execute().get();
        return new Variant(response.getObject(response.getFieldNames().iterator().next()));
    }

    /**
     * creates a Variant in which the code for a PLC4X-write call is stored
     *
     * @param FieldAddress the Adress of the Variable on the Server
     * @param Value        the Value which should be wrote on the PLC4X Server
     * @param DataType     the DataType of the Value
     * @return returns the Variant with the code
     */
    private void plc4xWrite(String FieldAddress, DataValue Value, NodeId DataType) throws ExecutionException, InterruptedException, PlcRuntimeException {
        Object RightTypeValue = Value.getValue().getValue();
        BuiltinDataType.getBackingClass(DataType).cast(RightTypeValue);
        connection.writeRequestBuilder().addItem(FieldAddress, FieldAddress, RightTypeValue).build().execute().get();
    }

}
