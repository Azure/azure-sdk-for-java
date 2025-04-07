// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import java.util.Objects;

/**
 * A carrier for {@link EventDataBatch} that can also indicate absence of a batch via {@link EventDataBatchCarrier#EMPTY}.
 */
final class EventDataBatchCarrier {
    static final EventDataBatchCarrier EMPTY = new EventDataBatchCarrier();
    private final EventDataBatch batch;

    EventDataBatchCarrier(EventDataBatch batch) {
        this.batch = Objects.requireNonNull(batch, "'batch' cannot be null");
    }

    EventDataBatch getBatch() {
        assert this != EMPTY;
        return batch;
    }

    private EventDataBatchCarrier() {
        // private constructor for EMPTY.
        this.batch = null;
    }
}
