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

import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class ReferenceInformation {
    NodeId targetId;
    NodeId sourceId;
    NodeId refType;
    Boolean isForward;
    Integer order;
    UaNode varNode;

    public ReferenceInformation(NodeId targetId, NodeId sourceId, NodeId referenceType, Boolean isForward, Integer order, UaNode varNode) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.refType = referenceType;
        this.isForward = isForward;
        this.order = order;
        this.varNode = varNode;
    }

    public NodeId getTargetId() {
        return targetId;
    }

    public NodeId getSourceId() {
        return sourceId;
    }

    public NodeId getReferenceType() {
        return refType;
    }

    public Boolean getForward() {
        return isForward;
    }

    public Integer getOrder() {
        return order;
    }

    public UaNode getVarNode() {
        return varNode;
    }
}
