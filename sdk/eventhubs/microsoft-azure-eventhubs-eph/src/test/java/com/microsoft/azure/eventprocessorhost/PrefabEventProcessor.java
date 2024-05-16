// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.EventData;

import java.util.Arrays;

public class PrefabEventProcessor implements IEventProcessor {
    private PrefabProcessorFactory factory;

    private byte[] telltaleBytes;
    private CheckpointChoices doCheckpoint;
    private boolean doMarker;
    private boolean logEveryEvent;
    private boolean telltaleOnTimeout;
    private int eventCount = 0;

    PrefabEventProcessor(PrefabProcessorFactory factory, String telltale, CheckpointChoices doCheckpoint, boolean doMarker, boolean logEveryEvent) {
        this.factory = factory;
        this.telltaleBytes = telltale.getBytes();
        this.doCheckpoint = doCheckpoint;
        this.doMarker = doMarker;
        this.logEveryEvent = logEveryEvent;
        this.telltaleOnTimeout = telltale.isEmpty();
    }

    @Override
    public void onOpen(PartitionContext context) throws Exception {
        TestBase.logInfo(context.getOwner() + " opening " + context.getPartitionId());
    }

    @Override
    public void onClose(PartitionContext context, CloseReason reason) throws Exception {
        TestBase.logInfo(context.getOwner() + " closing " + context.getPartitionId());
    }

    @Override
    public void onEvents(PartitionContext context, Iterable<EventData> events) throws Exception {
        int batchSize = 0;
        EventData lastEvent = null;
        int baseline = this.eventCount;
        if (events != null && events.iterator().hasNext()) {
            this.factory.setOnEventsContext(context);

            for (EventData event : events) {
                this.eventCount++;
                batchSize++;
                /*
                if (((this.eventCount % 10) == 0) && this.doMarker) {
                    TestBase.logInfo("P" + context.getPartitionId() + ": " + this.eventCount);
                }
                */
                if (this.logEveryEvent) {
                    TestBase.logInfo("(" + context.getOwner() + ") P" + context.getPartitionId() + " " + new String(event.getBytes()) + " @ " + event.getSystemProperties().getOffset());
                }
                if (Arrays.equals(event.getBytes(), this.telltaleBytes)) {
                    this.factory.setTelltaleFound(context.getPartitionId());
                }
                lastEvent = event;
            }
        }
        if (batchSize == 0) {
            if (this.telltaleOnTimeout) {
                TestBase.logInfo("P" + context.getPartitionId() + " got expected timeout");
                this.factory.setTelltaleFound(context.getPartitionId());
            } else {
                TestBase.logError("P" + context.getPartitionId() + " got UNEXPECTED timeout");
                this.factory.putError("P" + context.getPartitionId() + " got UNEXPECTED timeout");
            }
        }
        this.factory.addBatch(batchSize);
        if (this.doMarker) {
            TestBase.logInfo("(" + context.getOwner() + ") P" + context.getPartitionId() + " total " + this.eventCount + "(" + (this.eventCount - baseline) + ")");
        }

        switch (doCheckpoint) {
            case CKP_EXPLICIT:
                context.checkpoint(lastEvent).get();
                TestBase.logInfo("P" + context.getPartitionId() + " checkpointed at " + lastEvent.getSystemProperties().getOffset());
                break;

            case CKP_NOARGS:
                context.checkpoint().get();
                TestBase.logInfo("P" + context.getPartitionId() + " checkpointed without arguments");
                break;
            case CKP_NONE:
            default:
                break;
        }
    }

    @Override
    public void onError(PartitionContext context, Throwable error) {
        TestBase.logInfo("P" + context.getPartitionId() + "onError: " + error.toString() + " " + error.getMessage());
        this.factory.putError(context.getPartitionId() + " onError: " + error.toString() + " " + error.getMessage());
    }

    public enum CheckpointChoices {
        CKP_NONE,
        CKP_EXPLICIT,
        CKP_NOARGS
    }
}
