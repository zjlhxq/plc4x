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

import org.eclipse.milo.opcua.sdk.server.nodes.AttributeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;


import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TransformationNode extends UaVariableNode {

    AttributeContext attributeContext = new AttributeContext(AggregationsNamespace.get().getUaServer());
    private HashMap<String, NodeId> inputNodes;
    private String javaScriptTransforamtionCode;
    private ScriptEngine engine;
    private DataValue value;


    public TransformationNode(NodeId nodeId, QualifiedName browseName, LocalizedText displayName,
                              HashMap<String, NodeId> inputNodes, String javaScriptTransforamtionCode) {

        super(AggregationsNamespace.get().getUaNodecontext(), nodeId, browseName, displayName);
        this.inputNodes = inputNodes;
        this.javaScriptTransforamtionCode = javaScriptTransforamtionCode;
        setUpTransformation();
    }

    public TransformationNode(NodeId nodeId, QualifiedName browseName, LocalizedText displayName,
                              HashMap<String, NodeId> inputNodes, NodeId outputDataType, String javaScriptTransforamtionCode) {
        this(nodeId, browseName, displayName, inputNodes, javaScriptTransforamtionCode);
        setDataType(outputDataType);
    }


    private void setUpTransformation() {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("nashorn");
        updateAliasNodes();
    }

    private void updateAliasNodes() {
        //getValuesIntoScript
        for (Map.Entry<String, NodeId> nameAndId : inputNodes.entrySet()) {
            String name = nameAndId.getKey();
            Object value = AggregationsNamespace.get().getNode(nameAndId.getValue()).get()
                .readAttribute(attributeContext, UInteger.valueOf(13)).getValue().getValue();

            engine.put(name, value);
        }

        //runTransformationScript
        try {
            engine.eval(javaScriptTransforamtionCode);
        } catch (ScriptException e) {
            e.printStackTrace();
        }

        //getValuesOutOfScript
        this.value = new DataValue(new Variant(engine.get("TransformationNodeOutput")));

    }

    @Override
    public DataValue getValue() {
        updateAliasNodes();
        return this.value;
    }
}
