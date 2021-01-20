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

import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.math.DoubleMath;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.types.PlcSubscriptionType;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.api.services.AttributeServices;
import org.eclipse.milo.opcua.sdk.server.api.services.AttributeServices.ReadContext;
import org.eclipse.milo.opcua.sdk.server.items.BaseMonitoredItem;
import org.eclipse.milo.opcua.sdk.server.items.MonitoredDataItem;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.subscriptions.Subscription;
import org.eclipse.milo.opcua.sdk.server.util.PendingRead;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.util.ExecutionQueue;

/**
 * This is a modified version of the {@link SubscriptionModel SubscriptionModel} desinged to fit the requirements
 * of using the PLC4X delegates. It splits the incoming in PLC4X and OPC UA SubscriptionItems and procedes them separately.
 * The Values of the OPC UA Subscriptions are checked internally as the stock SubcriptionModel does it. The PLC4X Subscriptions
 * are loaded and compared in a SubscriptionCache. If a PLC4X Subscription determines a changed value it is changed in the
 * SubscriptionCache and in the next cycle of the SubscriptionModel recognised and proceeded.
 */

public class AggregationsSubscriptionModel {

    private static ConcurrentHashMap<ReadValueId, Variant> subscriptionCache = new ConcurrentHashMap<ReadValueId, Variant>();
    private static ConcurrentHashMap<ReadValueId, SubscriptionX> plc4xSubsciptions = new ConcurrentHashMap<ReadValueId, SubscriptionX>();
    private final Set<DataItem> plc4xItemSet = Collections.newSetFromMap(Maps.newConcurrentMap());
    private final Set<DataItem> internItemSet = Collections.newSetFromMap(Maps.newConcurrentMap());
    private final Set<DataItem> aliasItemSet = Collections.newSetFromMap(Maps.newConcurrentMap());
    private final List<Plc4xScheduledUpdate> plc4xSchedule = Lists.newCopyOnWriteArrayList();
    private final List<InterneScheduledUpdate> internSchedule = Lists.newCopyOnWriteArrayList();

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;
    private final ExecutionQueue executionQueue;

    private final OpcUaServer server;
    private final AttributeServices attributeServices;

    public AggregationsSubscriptionModel(OpcUaServer server, AttributeServices attributeServices) {
        this.server = server;

        this.attributeServices = attributeServices;

        executor = server.getExecutorService();
        scheduler = server.getScheduledExecutorService();

        executionQueue = new ExecutionQueue(executor);
    }

    public void onDataItemsCreated(List<DataItem> items) {
        List<DataItem> changedItems = replaceAliasNodes(items);
        subscribe(changedItems);
        executionQueue.submit(() -> {
            addItems(changedItems);
            reschedule();
        });
    }

    public void onDataItemsModified(List<DataItem> items) {
        List<DataItem> changedItems = replaceAliasNodes(items);
        unsubscribe(changedItems);
        subscribe(changedItems);
        executionQueue.submit(this::reschedule);
    }

    public void onDataItemsDeleted(List<DataItem> items) {
        List<DataItem> changedItems = replaceAliasNodes(items);
        executionQueue.submit(() -> {
            removeItems(changedItems);
            reschedule();
        });
        unsubscribe(changedItems);
    }

    public void onMonitoringModeChanged(List<MonitoredItem> items) {
        executionQueue.submit(this::reschedule);
    }

    private void reschedule() {
        //plc4xItems
        Map<Double, List<DataItem>> plc4xBySamplingInterval = plc4xItemSet.stream()
            .filter(DataItem::isSamplingEnabled)
            .collect(Collectors.groupingBy(DataItem::getSamplingInterval));

        List<Plc4xScheduledUpdate> plc4xUpdates = plc4xBySamplingInterval.keySet().stream()
            .map(samplingInterval -> {
                List<DataItem> plc4xItems = plc4xBySamplingInterval.get(samplingInterval);

                return new Plc4xScheduledUpdate(samplingInterval, plc4xItems);
            })
            .collect(Collectors.toList());

        plc4xSchedule.forEach(Plc4xScheduledUpdate::cancel);
        plc4xSchedule.clear();
        plc4xSchedule.addAll(plc4xUpdates);
        plc4xSchedule.forEach(scheduler::execute);

        //internItems
        Map<Double, List<DataItem>> internBySamplingInterval = internItemSet.stream()
            .filter(DataItem::isSamplingEnabled)
            .collect(Collectors.groupingBy(DataItem::getSamplingInterval));

        List<InterneScheduledUpdate> internUpdates = internBySamplingInterval.keySet().stream()
            .map(samplingInterval -> {
                List<DataItem> internItems = internBySamplingInterval.get(samplingInterval);

                return new InterneScheduledUpdate(samplingInterval, internItems);
            })
            .collect(Collectors.toList());

        internSchedule.forEach(InterneScheduledUpdate::cancel);
        internSchedule.clear();
        internSchedule.addAll(internUpdates);
        internSchedule.forEach(scheduler::execute);

    }

    /**
     * creates for each OPC UA Subscription a PLC4X Subscription
     *
     * @param items list of Dataitems which should be subscribed
     */
    private void subscribe(List<DataItem> items) {
        //extract the Parameters needed to create a Plc4x Subscriprion
        for (DataItem dataItem : items) {
            MonitoredDataItem item = (MonitoredDataItem) dataItem;

            NodeId nodeId = item.getReadValueId().getNodeId();
            if (isPlc4xItem(nodeId)) {
                String connectionString = DataItemX.dataItems.get(nodeId).getConnectionString();
                String fieldAdress = DataItemX.dataItems.get(nodeId).getFieldAddress();

                double sampelingIntervall = item.getSamplingInterval();

                PlcSubscriptionType subType = monitoringMode2SubType(item.getMonitoringMode());

                ReadValueId key = item.getReadValueId();

                //createing the actual Plc4x Subscription
                SubscriptionX subscriptionX = new SubscriptionX(fieldAdress, connectionString, subType, sampelingIntervall, ChronoUnit.MILLIS);
                plc4xSubsciptions.put(key, subscriptionX);
                try {
                    //If the Plc4x Subscription updates a Value it's updated in the subscriptionCache
                    subscriptionX.subscribe((Value) -> subscriptionCache.put(key, Value));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (PlcConnectionException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * deltes the PLC4X Subscriptions when OPC UA subscriptions are deleted
     *
     * @param items list of Dataitems which should be unsubscribed
     */
    private void unsubscribe(List<DataItem> items) {
        for (DataItem x : items) {
            ReadValueId key = x.getReadValueId();
            if (isPlc4xItem(key.getNodeId())) {
                SubscriptionX subscriptionX = plc4xSubsciptions.get(key);
                subscriptionX.unsubscribe();
                subscriptionCache.remove(key);
                plc4xSubsciptions.remove(key);
            }
        }
    }

    /**
     * checks if a Node is a OPC UA intern Node or just a delegate to a PLC4X Variable
     *
     * @param id NodeId of the Node which is checked
     * @return returns if the Node is a PLC4X delegate Node
     */
    private boolean isPlc4xItem(NodeId id) {
        return DataItemX.dataItems.containsKey(id);
    }

    /**
     * mapps OPC UA Monitoring Modes to the PLC Subscription Types
     *
     * @param mode the OPC UA MonitoringMode of a Subscription
     * @return returns the matching PlcSubscriptionType
     */
    private PlcSubscriptionType monitoringMode2SubType(MonitoringMode mode) {
        switch (mode) {
            case Reporting:
                return PlcSubscriptionType.CHANGE_OF_STATE;

            case Sampling:
                return PlcSubscriptionType.CYCLIC;

            default:
                return PlcSubscriptionType.CHANGE_OF_STATE;
        }
    }

    private List<DataItem> replaceAliasNodes(List<DataItem> items) {
        List<DataItem> changedItems = Lists.newArrayList();
        for (DataItem dataItem : items) {
            MonitoredDataItem item = (MonitoredDataItem) dataItem;
            if (AggregationsNamespace.get().getNode(item.getReadValueId().getNodeId()).get() instanceof AliasNode) {
                DataItem specialItem = createAliasDataItem(item);
                changedItems.add(specialItem);
                aliasItemSet.add(specialItem);
            } else {
                changedItems.add(dataItem);
            }
        }
        return changedItems;
    }

    /**
     * splits the incoming items in OPC UA intern Nodes and delegates to PLC4X Variables and stores them in to different Sets
     *
     * @param items list of Dataitems which should be un/subscribed
     */
    private void addItems(List<DataItem> items) {
        //splitting the items subscribed at the OPC UA Server into Subscriptions onto the OPC UA Server and Plc4x Subscriptions
        for (DataItem dataItem : items) {
            MonitoredDataItem item = (MonitoredDataItem) dataItem;

            if (isPlc4xItem(item.getReadValueId().getNodeId())) {
                plc4xItemSet.add(dataItem);
            } else {
                internItemSet.add(dataItem);
            }
        }
    }

    /**
     * removes incoming items from the two Sets for OPC UA intern Nodes and delegates to PLC4X Variables
     *
     * @param items list of Dataitems which should be un/subscribed
     */
    private void removeItems(List<DataItem> items) {
        for (DataItem dataItem : items) {
            MonitoredDataItem item = (MonitoredDataItem) dataItem;

            if (aliasItemSet.contains(item)) {
                plc4xItemSet.remove(dataItem);
                aliasItemSet.remove(dataItem);
            } else if (isPlc4xItem(item.getReadValueId().getNodeId())) {
                plc4xItemSet.remove(dataItem);
            } else {
                internItemSet.remove(dataItem);
            }
        }
    }

    /**
     * removes the item from the belonging subscription and replaces it with a custom one.
     * The custom one has the target of the originalNode not the Alias
     *
     * @param item MonitoredDataItem in which the target should be changed
     * @return changeMonitoredDataItem the custom one
     */
    private DataItem createAliasDataItem(DataItem item) {
        Subscription subscription = AggregationsNamespace.get().getUaServer().getSubscriptions().get(item.getSubscriptionId());
        AliasNode aliasNode = (AliasNode) AggregationsNamespace.get().getNode(item.getReadValueId().getNodeId()).get();
        NodeId orgnialNodeId = aliasNode.orginalNodeId;

        List<BaseMonitoredItem<?>> monitoredItemsToDelete = new ArrayList<>();
        List<BaseMonitoredItem<?>> monitoredItemsToAdd = new ArrayList<>();

        monitoredItemsToDelete.add((BaseMonitoredItem<?>) item);
        subscription.removeMonitoredItems(monitoredItemsToDelete);

        MonitoredDataItem changeMonitoredDataItem = changeMonitoredDataItemTarget((MonitoredDataItem) item, orgnialNodeId);
        monitoredItemsToAdd.add(changeMonitoredDataItem);
        subscription.addMonitoredItems(monitoredItemsToAdd);

        return changeMonitoredDataItem;
    }

    /**
     * replicates a MonitoredDataItem but changes the NodeId of its Target
     * there may be Problems with the filter
     *
     * @param item          MonitoredDataItem in which the target should be changed
     * @param orgnialNodeId the NodeId of the new Target
     * @return changedMonitoredDataItem the MonitoredDataItem witch the new Target
     */
    private MonitoredDataItem changeMonitoredDataItemTarget(MonitoredDataItem item, NodeId orgnialNodeId) {
        ReadValueId changedReadValueId = new ReadValueId(orgnialNodeId,
            item.getReadValueId().getAttributeId(),
            item.getReadValueId().getIndexRange(),
            item.getReadValueId().getDataEncoding());

        MonitoredDataItem changedMonitoredDataItem = null;
        try {
            changedMonitoredDataItem = new MonitoredDataItem(
                server,
                item.getSession(),
                item.getId(),
                item.getSubscriptionId(),
                changedReadValueId,
                item.getMonitoringMode(),
                item.getTimestampsToReturn(),
                UInteger.valueOf(item.getClientHandle()),
                item.getSamplingInterval(),
                item.getFilterResult(),
                UInteger.valueOf(item.getQueueSize()),
                item.isDiscardOldest());
        } catch (UaException e) {
            e.printStackTrace();
        }

        return changedMonitoredDataItem;
    }

    private class Plc4xScheduledUpdate implements Runnable {

        private final long samplingInterval;
        private final List<DataItem> items;
        private volatile boolean cancelled = false;

        private Plc4xScheduledUpdate(double samplingInterval, List<DataItem> items) {
            this.samplingInterval = DoubleMath.roundToLong(samplingInterval, RoundingMode.UP);
            this.items = items;
        }

        private void cancel() {
            cancelled = true;
        }

        @Override
        public void run() {
            List<PendingRead> pending = items.stream()
                .map(item -> new PendingRead(item.getReadValueId()))
                .collect(Collectors.toList());

            List<ReadValueId> ids = pending.stream()
                .map(PendingRead::getInput)
                .collect(Collectors.toList());

            ReadContext context = new ReadContext(server, null);

            context.getFuture().thenAcceptAsync(values -> {
                Iterator<DataItem> ii = items.iterator();
                Iterator<DataValue> vi = values.iterator();

                while (ii.hasNext() && vi.hasNext()) {
                    DataItem item = ii.next();
                    DataValue value = vi.next();

                    item.setValue(value);
                }

                if (!cancelled) {
                    scheduler.schedule(this, samplingInterval, TimeUnit.MILLISECONDS);
                }
            }, executor);

            //here s the major change to the stock SubsciptionModell
            //the values are coming from the SubscriptionCache and not the intern DataModell
            context.success(getSubscriptionCache(ids));
        }

        private List<DataValue> getSubscriptionCache(List<ReadValueId> ids) {
            List<DataValue> results = Lists.newArrayListWithCapacity(ids.size());
            for (ReadValueId readValueId : ids) {
                results.add(new DataValue(subscriptionCache.get(readValueId)));
            }
            return results;
        }

    }

    private class InterneScheduledUpdate implements Runnable {

        private final long samplingInterval;
        private final List<DataItem> items;
        //works like the standart milo SubscriptionModel
        private volatile boolean cancelled = false;

        private InterneScheduledUpdate(double samplingInterval, List<DataItem> items) {
            this.samplingInterval = DoubleMath.roundToLong(samplingInterval, RoundingMode.UP);
            this.items = items;
        }

        private void cancel() {
            cancelled = true;
        }

        @Override
        public void run() {
            List<PendingRead> pending = items.stream()
                .map(item -> new PendingRead(item.getReadValueId()))
                .collect(Collectors.toList());

            List<ReadValueId> ids = pending.stream()
                .map(PendingRead::getInput)
                .collect(Collectors.toList());

            ReadContext context = new ReadContext(server, null);

            context.getFuture().thenAcceptAsync(values -> {
                Iterator<DataItem> ii = items.iterator();
                Iterator<DataValue> vi = values.iterator();

                while (ii.hasNext() && vi.hasNext()) {
                    DataItem item = ii.next();
                    DataValue value = vi.next();

                    TimestampsToReturn timestamps = item.getTimestampsToReturn();

                    if (timestamps != null) {
                        UInteger attributeId = item.getReadValueId().getAttributeId();

                        value = (AttributeId.Value.isEqual(attributeId)) ?
                            DataValue.derivedValue(value, timestamps) :
                            DataValue.derivedNonValue(value, timestamps);
                    }

                    item.setValue(value);
                }

                if (!cancelled) {
                    scheduler.schedule(this, samplingInterval, TimeUnit.MILLISECONDS);
                }
            }, executor);

            executor.execute(() -> attributeServices.read(context, 0d, TimestampsToReturn.Both, ids));
        }

    }

}
