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

import org.apache.plc4x.java.ads.connection.AdsTcpPlcConnection;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.*;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.types.PlcSubscriptionType;
import org.apache.plc4x.java.base.messages.DefaultPlcSubscriptionEvent;
import org.apache.plc4x.java.base.messages.DefaultPlcSubscriptionRequest;
import org.apache.plc4x.java.base.model.SubscriptionPlcField;
import org.apache.plc4x.java.opcua.connection.OpcuaTcpPlcConnection;
import org.apache.plc4x.java.opcua.protocol.OpcuaField;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

interface function {
    void execute(Variant Value);
}

/**
 * This is Class provides the Functionality of createing and delting PLC4X Subscriptions.
 */

public class SubscriptionX {

    private static ConcurrentHashMap<String, PlcConnection> Connections = new ConcurrentHashMap<String, PlcConnection>();

    private String connectionString;
    private String FieldAddress;
    private PlcSubscriptionType SubType;
    private double CycleTime;
    private ChronoUnit CycleTimeUnit;
    private String Name;
    private PlcConsumerRegistration registration;

    public SubscriptionX(String FieldAddress, String connectionString, PlcSubscriptionType subType, double cycleTime, ChronoUnit cycleTimeUnit) {
        this.SubType = subType;
        this.FieldAddress = FieldAddress;
        this.CycleTime = cycleTime;
        this.CycleTimeUnit = cycleTimeUnit;
        this.Name = "Name";
        this.connectionString = connectionString;
    }

    /**
     * creates a PLC4X Subscsription
     *
     * @param f the function which decides what is done with the return Value of the PLC4X Subscription
     */
    public void subscribe(function f) throws ExecutionException, InterruptedException, PlcConnectionException, URISyntaxException {
        URI connectionUri = new URI(connectionString);

        //need to check which type of Connection we got because only OpcuaTcpPlcConnection and AdsTcpPlcConnection support Subscriptions at the moment.
        //better way would be with PlcConnection
        if (connectionUri.getScheme().equals("opcua")) {
            OpcuaTcpPlcConnection Connection;
            if (!Connections.containsKey(connectionString)) {
                Connection = (OpcuaTcpPlcConnection) ConnectionX.getConnectionX(connectionString).getConnection();
                Connections.put(connectionString, Connection);
            } else {
                Connection = (OpcuaTcpPlcConnection) Connections.get(connectionString);
            }
            PlcSubscriptionResponse subResp = Connection.subscribe(new DefaultPlcSubscriptionRequest(
                Connection,
                new LinkedHashMap<>(
                    Collections.singletonMap(Name,
                        new SubscriptionPlcField(SubType, OpcuaField.of(FieldAddress), Duration.of((long) CycleTime, CycleTimeUnit)))
                )
            )).get();

            Consumer<PlcSubscriptionEvent> consumer = plcSubscriptionEvent -> {
                DefaultPlcSubscriptionEvent event = (DefaultPlcSubscriptionEvent) plcSubscriptionEvent;
                f.execute(new Variant(event.getValues().get(null).getRight().getObject(0)));
                /*
                TODO Bug workaround
                it's a workaround because there was a Bug to get the Value by its Name
                the correct code could be:
                f.execute(new Variant(event.getObject(FieldAddress)));
                */
            };
            registration = Connection.register(consumer, subResp.getSubscriptionHandles());

        } else if (connectionUri.getScheme().equals("ads")) {
            AdsTcpPlcConnection Connection;
            if (!Connections.containsKey(connectionString)) {
                Connection = (AdsTcpPlcConnection) ConnectionX.getConnectionX(connectionString).getConnection();
                Connections.put(connectionString, Connection);
            } else {
                Connection = (AdsTcpPlcConnection) Connections.get(connectionString);
            }

            PlcSubscriptionResponse subResp = null;


            if (SubType == PlcSubscriptionType.CHANGE_OF_STATE) {
                PlcSubscriptionRequest.Builder subscriptionRequestBuilder = Connection.subscriptionRequestBuilder();
                PlcSubscriptionRequest subscriptionRequest = subscriptionRequestBuilder
                    .addChangeOfStateField(Name, FieldAddress)
                    .build();
                subResp = subscriptionRequest.execute().get();
            } else if (SubType == PlcSubscriptionType.CYCLIC) {
                PlcSubscriptionRequest.Builder subscriptionRequestBuilder = Connection.subscriptionRequestBuilder();
                PlcSubscriptionRequest subscriptionRequest = subscriptionRequestBuilder
                    .addCyclicField(Name, FieldAddress, Duration.of((long) CycleTime, CycleTimeUnit))
                    .build();
                subResp = subscriptionRequest.execute().get();
            }

            Consumer<PlcSubscriptionEvent> consumer = plcSubscriptionEvent -> {
                DefaultPlcSubscriptionEvent event = (DefaultPlcSubscriptionEvent) plcSubscriptionEvent;
                f.execute(new Variant(event.getObject(Name)));
            };
            registration = Connection.register(consumer, subResp.getSubscriptionHandles());
        } else throw new PlcConnectionException("Subscribe is only supported with OPC UA and ADS TCP connections.");
    }

    /**
     * deletes a PLC4X Subscsription
     **/
    public void unsubscribe() {
        registration.unregister();
    }
}