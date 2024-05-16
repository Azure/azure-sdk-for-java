// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

public class CosmosDaemonThreadFactory implements ThreadFactory {
    private static final String NAME_TEMPLATE = "cosmos-daemon-%s[%s]";
    private final String namePrefix;
    private final AtomicInteger threadCount;

    public CosmosDaemonThreadFactory(String namePrefix) {
        checkNotNull(namePrefix, "Argument namePrefix must not be null.");
        this.namePrefix = namePrefix;
        this.threadCount = new AtomicInteger(0);
    }

    @Override
    public Thread newThread(Runnable r) {
        final String name = lenientFormat(NAME_TEMPLATE, this.namePrefix, this.threadCount.incrementAndGet());
        Thread t = new Thread(r, name);
        t.setDaemon(true);
        return t;
    }
}