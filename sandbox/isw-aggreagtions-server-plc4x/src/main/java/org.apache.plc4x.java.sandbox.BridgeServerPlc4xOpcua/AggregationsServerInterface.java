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

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import java.util.HashMap;

public interface AggregationsServerInterface {

    void createAggregationsServerNode(String connectionString, String variableAdress, NodeId dataType,
                                      String maschineName, String variableName);

    void deleteAggregationsServerNode(String connectionString, String maschineName, String variableAdress,
                                      String variableName);

    void createAggregationsServerAliasNode(NodeId orginalNodeId, NodeId aliasNodeId, Boolean takeRefereces);

    void createAggregationsServerFolderNode(NodeId NodeId, String Name);

    void createAggregationsServerReference(NodeId sourceNodeId, NodeId targetNodeId, NodeId ReferenceType, Boolean forward);

    void createAggregationsServerTransformationNode(NodeId nodeId, String browseName, String displayName,
                                                    HashMap<String, NodeId> inputNodes, String javaScriptTransformationCode);
}