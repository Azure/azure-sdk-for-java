/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.exceptioncontracts;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.MessagingFactory;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.FaultInjectingReactorFactory;
import com.microsoft.azure.eventhubs.lib.TestContext;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MsgFactoryOpenCloseTest extends ApiTestBase {

    static ConnectionStringBuilder connStr;

    @BeforeClass
    public static void initialize()  throws Exception
    {
        connStr = TestContext.getConnectionString();
    }

    @Test()
    public void VerifyThreadReleaseOnMsgFactoryOpenError() throws Exception    {

        final LifecycleTrackingThreadFactory threadFactory = new LifecycleTrackingThreadFactory();
        final FaultInjectingReactorFactory networkOutageSimulator = new FaultInjectingReactorFactory();
        networkOutageSimulator.setFaultType(FaultInjectingReactorFactory.FaultType.NetworkOutage);

        final CompletableFuture<MessagingFactory> openFuture = MessagingFactory.createFromConnectionString(
                connStr.toString(), null,
                threadFactory,
                networkOutageSimulator);
        try {
            openFuture.get();
            Assert.assertFalse(true);
        }
        catch (ExecutionException error) {
            Assert.assertEquals(EventHubException.class, error.getCause().getClass());
        };

        Assert.assertEquals(1, threadFactory.Threads.size());

        int retryAttempt = 0;
        while (retryAttempt++ < 3 && threadFactory.Threads.get(0).isAlive()) {
            Thread.sleep(1000); // for reactor to transition from cleanup to complete-stop
        }

        Assert.assertEquals(false, threadFactory.Threads.get(0).isAlive());
    }

    public static final class LifecycleTrackingThreadFactory extends MessagingFactory.ThreadFactory {
        final public List<Thread> Threads = new LinkedList<>();

        @Override
        public Thread create(final Runnable worker) {
            final Thread newThread = super.create(worker);
            this.Threads.add(newThread);
            return newThread;
        }
    }
}
