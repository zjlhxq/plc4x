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

import com.sun.net.httpserver.Authenticator;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;

public class CreateItem extends AbstractMethodInvocationHandler {


    private static final Argument ConnectionString = new Argument(
        "ConnectionString",
        Identifiers.String,
        ValueRanks.Scalar,
        null,
        new LocalizedText("The Address of the Machine")
    );

    private static final Argument FieldAdress = new Argument(
        "FieldAdresses",
        Identifiers.String,
        ValueRanks.Scalar,
        null,
        new LocalizedText("The Address of the Variable on the Machine")
    );

    private static final Argument DataTyp = new Argument(
        "DataTypes",
        Identifiers.String,
        ValueRanks.Scalar,
        null,
        new LocalizedText("DataType of the Variable")
    );

    protected CreateItem(UaMethodNode node) {
        super(node);
    }

    @Override
    public Argument[] getInputArguments() {
        return new Argument[]{ConnectionString, FieldAdress, DataTyp};
    }

    @Override
    public Argument[] getOutputArguments() {
        return new Argument[]{};
    }

    @Override
    protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputValues) {
        String ConnectionString = (String) inputValues[0].getValue();
        String FieldAdress = (String) inputValues[1].getValue();
        NodeId DataType = AggregationsNamespace.getDataType((String) inputValues[2].getValue());
        Boolean success = AggregationsNamespace.get().createItem(ConnectionString, FieldAdress, DataType);
        if (!success) {
            String[] intendedNullPointer = null;
            String x = intendedNullPointer[42];
        }
        return new Variant[]{new Variant(true)};
    }
}
