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

import java.util.concurrent.ConcurrentHashMap;


/**
 * This is Class encapsulates all relevant informations to a Variable and its Connection and provides methods to access them easy.
 */
public class DataItemX {
    protected static ConcurrentHashMap<String, NodeId> getIdByParams = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<NodeId, DataItemX> dataItems = new ConcurrentHashMap<>();

    private NodeId id;
    private String ConnectionString;
    private String FieldAddress;
    private NodeId DataType;
    private String MaschineName;
    private String VariableName;

    private ConnectionX connectionX;
    private VariableX variableX;

    protected DataItemX(NodeId id, String ConnectionString, String FieldAddress, NodeId DataType, String MaschineName, String VariableName) {
        this.id = id;
        this.ConnectionString = ConnectionString;
        this.FieldAddress = FieldAddress;
        this.DataType = DataType;
        this.MaschineName = MaschineName;
        this.VariableName = VariableName;

        dataItems.put(id, this);
        getIdByParams.put(stringPair(ConnectionString, FieldAddress), id);

        if (!ConnectionString.equals(MaschineName)) {
            getIdByParams.put(stringPair(MaschineName, FieldAddress), id);
        }
        if (!FieldAddress.equals(VariableName)) {
            getIdByParams.put(stringPair(ConnectionString, VariableName), id);
        }
        if (!ConnectionString.equals(MaschineName) && !FieldAddress.equals(VariableName)) {
            getIdByParams.put(stringPair(MaschineName, VariableName), id);
        }
    }

    protected DataItemX(NodeId id, String ConnectionString, String FieldAddress) {
        this.id = id;
        this.ConnectionString = ConnectionString;
        this.FieldAddress = FieldAddress;
        dataItems.put(id, this);
        getIdByParams.put(stringPair(ConnectionString, FieldAddress), id);
    }

    protected static NodeId getId(String A, String B) {
        return getIdByParams.get(stringPair(A, B));
    }

    protected static String stringPair(String A, String B) {
        return A + "@" + B;
    }

    protected String getConnectionString() {
        return ConnectionString;
    }

    protected String getFieldAddress() {
        return FieldAddress;
    }

    public NodeId getDataType() {
        return DataType;
    }

    public String getMaschineName() {
        return MaschineName;
    }

    public String getVariableName() {
        return VariableName;
    }

    public ConnectionX getConnectionX() {
        return connectionX;
    }

    protected void setConnectionX(ConnectionX connectionX) {
        this.connectionX = connectionX;
    }

    protected VariableX getVariableX() {
        return variableX;
    }

    protected void setVariableX(VariableX variableX) {
        this.variableX = variableX;
    }

    protected void deleteItem() {
        getIdByParams.remove(stringPair(ConnectionString, FieldAddress));
        getIdByParams.remove(stringPair(MaschineName, FieldAddress));
        getIdByParams.remove(stringPair(ConnectionString, VariableName));
        getIdByParams.remove(stringPair(MaschineName, VariableName));

        dataItems.remove(id);
    }

}
