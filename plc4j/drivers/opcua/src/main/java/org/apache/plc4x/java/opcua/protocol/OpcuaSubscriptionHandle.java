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
package org.apache.plc4x.java.opcua.protocol;

import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.opcua.field.OpcuaField;
import org.apache.plc4x.java.opcua.readwrite.*;
import org.apache.plc4x.java.opcua.readwrite.io.OpcuaMessageIO;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.apache.plc4x.java.spi.messages.DefaultPlcSubscriptionEvent;
//import org.apache.plc4x.java.opcua.connection.OpcuaTcpPlcConnection;
import org.apache.plc4x.java.spi.messages.PlcSubscriber;
import org.apache.plc4x.java.spi.messages.utils.ResponseItem;
import org.apache.plc4x.java.spi.model.DefaultPlcConsumerRegistration;
import org.apache.plc4x.java.spi.model.DefaultPlcSubscriptionHandle;
import org.apache.plc4x.java.spi.transaction.RequestTransactionManager;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 */
public class OpcuaSubscriptionHandle extends DefaultPlcSubscriptionHandle {

    private Set<Consumer<PlcSubscriptionEvent>> consumers = new HashSet<>();
    private OpcuaField field;
    private long clientHandle;
    CompletableFuture<PlcSubscriptionResponse> future = null;
    private AtomicBoolean destroy = new AtomicBoolean(false);
    private OpcuaProtocolLogic plcSubscriber;
    private Long subscriptionId;

    /**
     * @param field    corresponding map key in the PLC4X request/reply map
     *
     */
    public OpcuaSubscriptionHandle(OpcuaProtocolLogic plcSubscriber, Long subscriptionId, OpcuaField field) {
        super(plcSubscriber);
        this.field = field;
        this.subscriptionId = subscriptionId;
        startSubscriber();
    }

    /**
     *
     * @return
     */
    public void startSubscriber() {
        CompletableFuture.supplyAsync(() -> {
            LinkedList<Long> outstandingAcknowledgements = new LinkedList<>();
            while (!this.destroy.get()) {
                int requestHandle = this.plcSubscriber.getRequestHandle();

                RequestHeader requestHeader = new RequestHeader(this.plcSubscriber.getAuthenticationToken(),
                    OpcuaProtocolLogic.getCurrentDateTime(),
                    requestHandle,
                    0L,
                    OpcuaProtocolLogic.NULL_STRING,
                    OpcuaProtocolLogic.REQUEST_TIMEOUT_LONG,
                    OpcuaProtocolLogic.NULL_EXTENSION_OBJECT);

                SubscriptionAcknowledgement[] acks = null;
                int ackLength = outstandingAcknowledgements.size();
                if (outstandingAcknowledgements.size() > 0) {
                    acks = new SubscriptionAcknowledgement[outstandingAcknowledgements.size()];
                    for (int i = 0; i < outstandingAcknowledgements.size(); i++) {
                        acks[i] = new SubscriptionAcknowledgement(this.subscriptionId, outstandingAcknowledgements.remove())
                    }
                }

                PublishRequest publishRequest = new PublishRequest((byte) 1,
                    (byte) 0,
                    requestHeader,
                    ackLength,
                    acks
                );

                //Pick up from here, also create a Utils class for static variables.
                try {
                    WriteBuffer buffer = new WriteBuffer(createSubscriptionRequest.getLengthInBytes(), true);
                    OpcuaMessageIO.staticSerialize(buffer, createSubscriptionRequest);

                    int transactionId = getTransactionIdentifier();

                    OpcuaMessageRequest createMessageRequest = new OpcuaMessageRequest(FINAL_CHUNK,
                        channelId.get(),
                        tokenId.get(),
                        transactionId,
                        transactionId,
                        buffer.getData());

                    RequestTransactionManager.RequestTransaction transaction = tm.startRequest();
                    transaction.submit(() -> context.sendRequest(new OpcuaAPU(createMessageRequest))
                        .expectResponse(OpcuaAPU.class, REQUEST_TIMEOUT)
                        .onTimeout(future::completeExceptionally)
                        .onError((p, e) -> future.completeExceptionally(e))
                        .check(p -> p.getMessage() instanceof OpcuaMessageResponse)
                        .unwrap(p -> (OpcuaMessageResponse) p.getMessage())
                        .handle(opcuaResponse -> {
                            CreateSubscriptionResponse responseMessage = null;
                            try {
                                responseMessage = (CreateSubscriptionResponse) OpcuaMessageIO.staticParse(new ReadBuffer(opcuaResponse.getMessage(), true));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            // Pass the response back to the application.
                            future.complete(responseMessage);

                            // Finish the request-transaction.
                            transaction.endRequest();
                        }));
                } catch (ParseException e) {
                    LOGGER.info("Unable to serialize subscription request");
                }
            }





            return;
        });

    }


    /**
     *
     * @return
     */
    public void stopSubscriber() {
        this.destroy.set(true);
    }


    public long getClientHandle() {
        return clientHandle;
    }

    /**
     * @param item
     * @param value
     */
    public void onSubscriptionValue(Object item, Object value) {
        consumers.forEach(plcSubscriptionEventConsumer -> {
            PlcResponseCode resultCode = PlcResponseCode.OK;
            PlcValue stringItem = null;
            /*if (value.getStatusCode() != StatusCode.GOOD) {
                resultCode = PlcResponseCode.NOT_FOUND;
            } else {
                stringItem = OpcuaTcpPlcConnection.encodePlcValue(value);

            }*/
            Map<String, ResponseItem<PlcValue>> fields = new HashMap<>();
            ResponseItem<PlcValue> newPair = new ResponseItem<>(resultCode, stringItem);
            fields.put(field.getIdentifier(), newPair);
            PlcSubscriptionEvent event = new DefaultPlcSubscriptionEvent(Instant.now(), fields);
            plcSubscriptionEventConsumer.accept(event);
        });
    }

    @Override
    public PlcConsumerRegistration register(Consumer<PlcSubscriptionEvent> consumer) {
        consumers.add(consumer);
        return null;
//        return () -> consumers.remove(consumer);
    }



}
