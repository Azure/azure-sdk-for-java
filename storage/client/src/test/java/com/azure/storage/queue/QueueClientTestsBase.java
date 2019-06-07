// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.test.TestBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public abstract class QueueClientTestsBase extends TestBase {
    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
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
    public abstract void setProperties();

    // TODO (alzimmer): determine how, or if, to test getting statistics
}
