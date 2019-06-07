// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.test.TestBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public abstract class QueueServiceClientTestsBase extends TestBase {
    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    @Test
    public abstract void createWithSharedKey();

    @Test
    public abstract void createWithSASToken();

    @Test
    public abstract void createWithMetadata();

    @Test
    public abstract void createTwiceSameMetadata();

    @Test
    public abstract void createTwiceDifferentMetadata();

    @Test
    public abstract void deleteExisting();

    @Test
    public abstract void deleteNonExistent();

    @Test
    public abstract void getProperties();

    @Test
    public abstract void getPropertiesQueueDoesNotExist();

    @Test
    public abstract void setMetadata();

    @Test
    public abstract void setMetadataQueueDoesNotExist();

    @Test
    public abstract void setInvalidMetadata();

    @Test
    public abstract void deleteMetadata();

    @Test
    public abstract void getAccessPolicy();

    @Test
    public abstract void getAccessPolicyQueueDoesNotExist();

    @Test
    public abstract void setAccessPolicy();

    @Test
    public abstract void setAccessPolicyQueueDoesNotExist();

    @Test
    public abstract void setInvalidAccessPolicy(); // max name length is 64

    @Test
    public abstract void enqueueMessage();

    @Test
    public abstract void enqueueEmptyMessage();

    @Test
    public abstract void enqueueTooLargeMessage();

    @Test
    public abstract void enqueueShortTimeToLiveMessage();

    @Test
    public abstract void enqueueQueueDoesNotExist();

    @Test
    public abstract void dequeueMessage();

    @Test
    public abstract void dequeueMultipleMessages();

    @Test
    public abstract void dequeueTooManyMessages();

    @Test
    public abstract void dequeueQueueDoesNotExist();

    @Test
    public abstract void peekMessage();

    @Test
    public abstract void peekMultipleMessages();

    @Test
    public abstract void peekTooManyMessages();

    @Test
    public abstract void peekQueueDoesNotExist();

    @Test
    public abstract void clearMessages();

    @Test
    public abstract void clearMessagesQueueDoesNotExist();

    @Test
    public abstract void deleteMessage();

    @Test
    public abstract void deleteMessageInvalidPopReceipt();

    @Test
    public abstract void deleteMessageQueueDoesNotExist();

    @Test
    public abstract void updateMessage();

    @Test
    public abstract void updateMessageInvalidPopReceipt();

    @Test
    public abstract void updateMessageQueueDoesNotExist();
}
