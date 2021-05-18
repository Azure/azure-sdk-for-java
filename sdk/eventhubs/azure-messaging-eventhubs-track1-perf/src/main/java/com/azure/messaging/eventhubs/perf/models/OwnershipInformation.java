// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf.models;

import com.microsoft.azure.eventprocessorhost.Checkpoint;
import com.microsoft.azure.eventprocessorhost.CompleteLease;

public class OwnershipInformation {
    private CompleteLease lease;
    private Checkpoint checkpoint;

    public CompleteLease getLease() {
        synchronized (this) {
            return lease;
        }
    }

    public OwnershipInformation setLease(CompleteLease lease) {
        synchronized (this) {
            this.lease = lease;
        }
        return this;
    }

    public Checkpoint getCheckpoint() {
        synchronized (this) {
            return checkpoint;
        }
    }

    public OwnershipInformation setCheckpoint(Checkpoint checkpoint) {
        synchronized (this) {
            this.checkpoint = checkpoint;
        }

        return this;
    }
}
