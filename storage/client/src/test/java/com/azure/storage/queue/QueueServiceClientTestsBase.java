// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.test.TestBase;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public abstract class QueueServiceClientTestsBase extends TestBase {
    String queueName;
    TestHelpers helper;

    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    String getQueueName() {
        return testResourceNamer.randomName("queue", 16).toLowerCase();
    }

    @Test
    public abstract void getQueueDoesNotCreateAQueue();

    @Test
    public abstract void createQueue();

    @Test
    public abstract void createQueueWithMetadata();

    @Test
    public abstract void createQueueTwiceSameMetadata();

    @Test
    public abstract void createQueueTwiceDifferentMetadata();

    @Test
    public abstract void deleteExistingQueue();

    @Test
    public abstract void deleteNonExistentQueue();

    @Test
    public abstract void listQueues();

    @Test
    public abstract void listQueuesIncludeMetadata();

    @Test
    public abstract void listQueuesWithPrefix();

    // TODO (alzimmer): This test is off for now until we determine how to handle paging with limited results
    //@Test
    public abstract void listQueuesWithLimit();

    @Test
    public abstract void setProperties();

    QueuesSegmentOptions defaultSegmentOptions() {
        return new QueuesSegmentOptions().prefix(queueName);
    }
}
