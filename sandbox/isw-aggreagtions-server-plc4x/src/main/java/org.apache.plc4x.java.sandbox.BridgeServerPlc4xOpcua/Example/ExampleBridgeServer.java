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
package org.apache.plc4x.java.sandbox.BridgeServerPlc4xOpcua.Example;


import org.apache.plc4x.java.sandbox.BridgeServerPlc4xOpcua.AggregationsNamespace;
import org.apache.plc4x.java.sandbox.BridgeServerPlc4xOpcua.AggregationsServer;
import org.eclipse.milo.examples.server.ExampleServer;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class ExampleBridgeServer {
    private AggregationsServer bridgeServer;
    private AggregationsNamespace bridgeServerNamespace;
    private String connectionString = "opcua:tcp://localhost:12686/milo";
    private String stringId = "ns=2;s=HelloWorld/ScalarTypes/String";
    private String int32Id = "ns=2;s=HelloWorld/ScalarTypes/Int32";
    private String int16Id = "ns=2;s=HelloWorld/ScalarTypes/Int16";
    private String doubleId = "ns=2;s=HelloWorld/ScalarTypes/Double";
    private String integerId = "ns=2;s=HelloWorld/ScalarTypes/Integer";
    private String floatId = "ns=2;s=HelloWorld/ScalarTypes/Float";
    private NodeId stringType = Identifiers.String;
    private NodeId int32Type = Identifiers.Int32;
    private NodeId int16Type = Identifiers.Int16;
    private NodeId doubleType = Identifiers.Double;
    private NodeId integerType = Identifiers.Integer;
    private NodeId floatType = Identifiers.Float;
    public ExampleBridgeServer() {
        try {
            bridgeServer = new AggregationsServer();
            bridgeServerNamespace = bridgeServer.getNamespace();
            bridgeServer.startup().get();
            this.serverConfig(this);
            final CompletableFuture<Void> future = new CompletableFuture<>();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> future.complete(null)));
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        //starts a MiloServer as an ExampleServer to work with
        ExampleServer exampleServer = new ExampleServer();
        exampleServer.startup().get();

        //starts the BridgeServer
        ExampleBridgeServer exampleBridgeServer = new ExampleBridgeServer();

        //Both these Servers can be reached by the Addresses in the Console
    }

    public void serverConfig(ExampleBridgeServer exampleServer) {
        //You can use all Methods of the bridgeServerNamespace you find, but i recommend using the one specified
        //in the AggregationServerInterface. Those are used to code the Examples below.

        //adds the Variables String, Int32, Int16, Double and Float
        exampleServer.addVariables();

        //deletes the Variable Float
        exampleServer.deleteNode();

        //creates a Folder and binds it to the RootFolder
        NodeId folderNodeId = new NodeId(2, "thisIsMyFolderNodeId");
        exampleServer.createFolderNode(folderNodeId);
        exampleServer.createReference(new NodeId(2, "RootNode"), folderNodeId);

        //creates a Alias to the String-Variable and binds it to the previous build Folder
        NodeId aliasNodeId = new NodeId(2, "thisIsMyAliasNodeId");
        exampleServer.createAlias(aliasNodeId);
        exampleServer.createReference(folderNodeId, aliasNodeId);

        //creates a TransformationNode and binds it to the previous build Folder
        NodeId transformationNodeId = new NodeId(2, "thisIsMyTransforamtionNodeId");
        exampleServer.createTransformationNode(transformationNodeId);
        exampleServer.createReference(folderNodeId, transformationNodeId);
    }

    /**
     * Creates the VariableNodes listed with the Parameters listed above.
     * You can individually Name the Variable or the Machine they are on if you want to.
     */
    private void addVariables() {
        bridgeServerNamespace.createAggregationsServerNode(connectionString, stringId, stringType, null, null);
        bridgeServerNamespace.createAggregationsServerNode(connectionString, int32Id, int32Type, null, null);
        bridgeServerNamespace.createAggregationsServerNode(connectionString, int16Id, int16Type, null, null);
        bridgeServerNamespace.createAggregationsServerNode(connectionString, doubleId, doubleType, null, null);
        bridgeServerNamespace.createAggregationsServerNode(connectionString, integerId, integerType, null, null);
        bridgeServerNamespace.createAggregationsServerNode(connectionString, floatId, floatType, null, null);
    }

    /**
     * Deletes the Node with the given Parameters.
     */
    private void deleteNode() {
        bridgeServerNamespace.deleteAggregationsServerNode(connectionString, null, floatId, null);
    }

    /**
     * Creates a FolderNode with the given Parameters.
     */
    private void createFolderNode(NodeId folderNodeId) {
        bridgeServerNamespace.createAggregationsServerFolderNode(folderNodeId, "ThisIsMyFolderName");
    }

    /**
     * Adds a Reference with the given Parameters.
     * In this case it's a Organize Reference from the Node FROM to the Node TO.
     */
    private void createReference(NodeId from, NodeId to) {
        bridgeServerNamespace.createAggregationsServerReference(from, to, Identifiers.Organizes, true);
    }

    /**
     * Adds a Alias with the given Parameters.
     * It's a new Node which directs the read of any Attribute also Value to the corresponding Attribute of the orginal Node.
     */
    private void createAlias(NodeId aliasNodeId) {
        //this is the NodeId the Server generates for the String Node
        NodeId orginalNodeId = AggregationsNamespace.get().generateNodeId(connectionString, stringId);
        bridgeServerNamespace.createAggregationsServerAliasNode(orginalNodeId, aliasNodeId, true);
    }

    /**
     * Adds a TransfomtaionNode with the given Parameters.
     * The Value of this Node is the Result of a JavaScript Code you can write yourself.
     *
     * @inputNodes this Hashmap contains the Nodes you use in your JavaScript Code. You use these Nodes as Variables in
     * the JavaScript Code. The Key of the Map is the Name of the Variable you use. The Value is the NodeId of the Node
     * whose Value-Attribute you want to use as your Variable.
     * @javaScriptTransforamtionCode this is the Code The JavaScript Engine uses. The Value-Attribute of the
     * TransforamtionNode will be "TransformationNodeOutput".
     * <p>
     * In this case the Inputs should be 16 for int16 and 3.14 for double. So the Result the Value-Attribute of
     * the TransforamtionNode should be 16*3.14+7 = 57.24.
     */
    private void createTransformationNode(NodeId transformationNodeId) {
        HashMap<String, NodeId> inputNodes = new HashMap<>();
        inputNodes.put("int16", AggregationsNamespace.get().generateNodeId(connectionString, int16Id));
        inputNodes.put("double", AggregationsNamespace.get().generateNodeId(connectionString, doubleId));

        String javaScriptTransforamtionCode = "TransformationNodeOutput = int16 * double + 7";

        bridgeServerNamespace.createAggregationsServerTransformationNode(transformationNodeId,
            "thisIsTheBrowseNameOfMyTransforamtionNode",
            "thisIsTheDisplayNameOfMyTransforamtionNode", inputNodes, javaScriptTransforamtionCode);
    }
}
