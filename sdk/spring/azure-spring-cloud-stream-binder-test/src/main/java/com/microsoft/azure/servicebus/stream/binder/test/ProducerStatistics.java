/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.servicebus.stream.binder.test;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class ProducerStatistics extends AbstractStatistics {
    private final Statistics sendLatency;

    ProducerStatistics(int size, long reportingInterval) {
        super(size, reportingInterval, "Produce ");
        this.sendLatency = new Statistics("Produce Latency (ms)");
    }

    public void record(long messageSize, long latency) {
        super.record(messageSize);
        this.sendLatency.record(latency);
    }

    public void printSummary() {
        super.printSummary();
        this.sendLatency.printSummary();
    }
}
