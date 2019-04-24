// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.EventData;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


class DummyPump extends PumpManager {
    DummyPump(HostContext hostContext, Closable parent) {
        super(hostContext, parent);
    }

    Iterable<String> getPumpsList() {
        return this.pumpStates.keySet();
    }

    @Override
    protected PartitionPump createNewPump(CompleteLease lease) {
        return new DummyPartitionPump(this.hostContext, lease, this, this);
    }

    @Override
    protected void removingPumpTestHook(String partitionId) {
        TestBase.logInfo("Steal detected, host " + this.hostContext.getHostName() + " removing " + partitionId);
    }


    private class DummyPartitionPump extends PartitionPump implements Callable<Void> {
        CompletableFuture<Void> blah = null;

        DummyPartitionPump(HostContext hostContext, CompleteLease lease, Closable parent, Consumer<String> pumpManagerCallback) {
            super(hostContext, lease, parent, pumpManagerCallback);
        }

        @Override
        CompletableFuture<Void> startPump() {
            this.blah = new CompletableFuture<Void>();
            ((InMemoryLeaseManager) this.hostContext.getLeaseManager()).notifyOnSteal(this.hostContext.getHostName(), this.lease.getPartitionId(), this);
            super.scheduleLeaseRenewer();
            return this.blah;
        }

        @Override
        protected void internalShutdown(CloseReason reason, Throwable e) {
            super.cancelPendingOperations();
            if (e != null) {
                this.blah.completeExceptionally(e);
            } else {
                this.blah.complete(null);
            }
        }

        @Override
        CompletableFuture<Void> shutdown(CloseReason reason) {
            internalShutdown(reason, null);
            return this.blah;
        }

        @Override
        public void onReceive(Iterable<EventData> events) {
        }

        @Override
        public void onError(Throwable error) {
        }

        @Override
        public Void call() {
            if (this.blah != null) {
                this.blah.completeExceptionally(new LeaseLostException(this.lease, "lease stolen"));
            }
            return null;
        }
    }
}
