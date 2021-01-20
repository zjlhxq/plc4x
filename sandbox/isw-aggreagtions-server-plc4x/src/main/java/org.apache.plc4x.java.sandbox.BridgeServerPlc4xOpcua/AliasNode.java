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

import com.google.common.collect.ImmutableList;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.nodes.AttributeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;

import java.util.ArrayList;
import java.util.Optional;

public class AliasNode extends UaVariableNode {
    NodeId orginalNodeId;
    AttributeContext attributeContext = new AttributeContext(AggregationsNamespace.get().getUaServer());
    UaVariableNode orginal = null;

    public AliasNode(NodeId orginalNodeId, NodeId aliasNodeId) throws WrongInputException {
        this(orginalNodeId, aliasNodeId, false);
    }

    public AliasNode(NodeId orginalNodeId, NodeId aliasNodeId, Boolean takeRefereces) throws WrongInputException {
        super(AggregationsNamespace.get().getNode(orginalNodeId).get().getNodeContext(),
            aliasNodeId,
            AggregationsNamespace.get().getNode(orginalNodeId).get().getBrowseName(),
            AggregationsNamespace.get().getNode(orginalNodeId).get().getDisplayName());

        this.orginalNodeId = orginalNodeId;
        if (!(AggregationsNamespace.get().getNode(orginalNodeId).get() instanceof UaVariableNode))
            throw new WrongInputException("AliasNodes must reference a UaVariableNode");
        this.orginal = (UaVariableNode) AggregationsNamespace.get().getNode(orginalNodeId).get();
        if (takeRefereces) setReferences();

    }

    private void setReferences() {
        ImmutableList<Reference> references = orginal.getReferences();
        ArrayList<Reference> referencesWithoutOrganize = new ArrayList<>();
        for (Reference thisReference : references) {

            if (!(thisReference.getReferenceTypeId() == Identifiers.Organizes)) {
                referencesWithoutOrganize.add(thisReference);
            }
        }
        for (Reference reference : referencesWithoutOrganize) {
            Reference newReference = null;

            NodeId sourceNodeId = reference.getSourceNodeId();
            ExpandedNodeId targetExpandedNodeId = reference.getTargetNodeId();
            Optional<NodeId> targetNodeId = reference.getTargetNodeId().local(AggregationsNamespace.get().getUaServer().getNamespaceTable());
            Optional<NodeId> optionalOrginalNodeId = Optional.ofNullable(orginalNodeId);
            NodeId referneceType = reference.getReferenceTypeId();
            Reference.Direction direction = reference.getDirection();

            if (sourceNodeId.equals(orginalNodeId)) {
                newReference = new Reference(this.getNodeId(), referneceType, targetExpandedNodeId, direction);
            } else if (targetNodeId.equals(optionalOrginalNodeId)) {
                newReference = new Reference(sourceNodeId, referneceType, this.getNodeId().expanded(), direction);
            }

            if (newReference != null) this.addReference(newReference);
        }
    }

    public NodeId getOrginalNodeId() {
        return orginalNodeId;
    }

    @Override
    public DataValue getValue() {
        return orginal.readAttribute(attributeContext, UInteger.valueOf(13));
    }

    @Override
    public synchronized void setValue(DataValue value) {
        try {
            orginal.writeAttribute(attributeContext, UInteger.valueOf(13), value, null);
        } catch (UaException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NodeId getDataType() {
        return (NodeId) orginal.readAttribute(attributeContext, UInteger.valueOf(14)).getValue().getValue();
    }

    @Override
    public synchronized void setDataType(NodeId dataType) {
        try {
            orginal.writeAttribute(attributeContext, UInteger.valueOf(14), new DataValue(new Variant(dataType)), null);
        } catch (UaException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer getValueRank() {
        return (int) orginal.readAttribute(attributeContext, UInteger.valueOf(15)).getValue().getValue();
    }

    @Override
    public synchronized void setValueRank(Integer valueRank) {
        try {
            orginal.writeAttribute(attributeContext, UInteger.valueOf(15), new DataValue(new Variant(valueRank)), null);
        } catch (UaException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UInteger[] getArrayDimensions() {
        return (UInteger[]) orginal.readAttribute(attributeContext, UInteger.valueOf(16)).getValue().getValue();
    }

    @Override
    public synchronized void setArrayDimensions(UInteger[] arrayDimensions) {
        try {
            orginal.writeAttribute(attributeContext, UInteger.valueOf(16), new DataValue(new Variant(arrayDimensions)), null);
        } catch (UaException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UByte getAccessLevel() {
        return (UByte) orginal.readAttribute(attributeContext, UInteger.valueOf(17)).getValue().getValue();
    }

    @Override
    public synchronized void setAccessLevel(UByte accessLevel) {
        try {
            orginal.writeAttribute(attributeContext, UInteger.valueOf(17), new DataValue(new Variant(accessLevel)), null);
        } catch (UaException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UByte getUserAccessLevel() {
        return (UByte) orginal.readAttribute(attributeContext, UInteger.valueOf(18)).getValue().getValue();
    }

    @Override
    public synchronized void setUserAccessLevel(UByte userAccessLevel) {
        try {
            orginal.writeAttribute(attributeContext, UInteger.valueOf(18), new DataValue(new Variant(userAccessLevel)), null);
        } catch (UaException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Double getMinimumSamplingInterval() {
        return (Double) orginal.readAttribute(attributeContext, UInteger.valueOf(19)).getValue().getValue();
    }

    @Override
    public void setMinimumSamplingInterval(Double minimumSamplingInterval) {
        try {
            orginal.writeAttribute(attributeContext, UInteger.valueOf(19), new DataValue(new Variant(minimumSamplingInterval)), null);
        } catch (UaException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean getHistorizing() {
        return (boolean) orginal.readAttribute(attributeContext, UInteger.valueOf(20)).getValue().getValue();
    }

    @Override
    public void setHistorizing(Boolean historizing) {
        try {
            orginal.writeAttribute(attributeContext, UInteger.valueOf(20), new DataValue(new Variant(historizing)), null);
        } catch (UaException e) {
            e.printStackTrace();
        }
    }

}
