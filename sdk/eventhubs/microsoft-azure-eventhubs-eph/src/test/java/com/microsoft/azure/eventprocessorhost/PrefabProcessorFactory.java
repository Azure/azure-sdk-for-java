// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import java.util.ArrayList;
import java.util.HashMap;

public class PrefabProcessorFactory implements IEventProcessorFactory<IEventProcessor> {
    private String telltale;
    private PrefabEventProcessor.CheckpointChoices doCheckpoint;
    private boolean doMarker;
    private boolean logEveryEvent;

    private ArrayList<String> errors = new ArrayList<String>();
    private HashMap<String, Boolean> foundTelltale = new HashMap<String, Boolean>();
    private int eventsReceivedCount = 0;
    private PartitionContext partitionContextOnEvents;

    PrefabProcessorFactory(String telltale, PrefabEventProcessor.CheckpointChoices doCheckpoint, boolean doMarker) {
        this(telltale, doCheckpoint, doMarker, false);
    }

    PrefabProcessorFactory(String telltale, PrefabEventProcessor.CheckpointChoices doCheckpoint, boolean doMarker, boolean logEveryEvent) {
        this.telltale = telltale;
        this.doCheckpoint = doCheckpoint;
        this.doMarker = doMarker;
        this.logEveryEvent = logEveryEvent;
    }

    void putError(String error) {
        this.errors.add(error);
    }

    ArrayList<String> getErrors() {
        return this.errors;
    }

    int getErrorCount() {
        return this.errors.size();
    }

    boolean getTelltaleFound(String partitionId) {
        Boolean retval = this.foundTelltale.get(partitionId);
        return ((retval != null) ? retval : false);
    }

    boolean getAnyTelltaleFound() {
        return (this.foundTelltale.size() > 0);
    }

    void setTelltaleFound(String partitionId) {
        this.foundTelltale.put(partitionId, true);
    }

    synchronized void addBatch(int batchSize) {
        this.eventsReceivedCount += batchSize;
    }

    int getEventsReceivedCount() {
        return this.eventsReceivedCount;
    }

    PartitionContext getOnEventsContext() {
        return this.partitionContextOnEvents;
    }

    void setOnEventsContext(PartitionContext value) {
        this.partitionContextOnEvents = value;
    }

    @Override
    public IEventProcessor createEventProcessor(PartitionContext context) throws Exception {
        return new PrefabEventProcessor(this, this.telltale, this.doCheckpoint, this.doMarker, this.logEveryEvent);
    }
}
