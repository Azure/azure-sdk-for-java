// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.stream.binder.test;

import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.CountDownLatch;

@ThreadSafe
public class ConsumerStatistics extends AbstractStatistics {

    private final CountDownLatch testCompleted;

    public ConsumerStatistics(int size, long reportingInterval, CountDownLatch testCompleted) {
        super(size, reportingInterval, "Consume ");
        this.testCompleted = testCompleted;
    }

    @Override
    protected void complete() {
        this.testCompleted.countDown();
    }
}
