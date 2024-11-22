// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.mocking;

import com.azure.core.amqp.implementation.MessageSerializer;
import org.apache.qpid.proton.message.Message;

import java.util.List;

/**
 * Mock implementation of the MessageSerializer interface.
 */
public class MockMessageSerializer implements MessageSerializer {
    @Override
    public int getSize(Message message) {
        return 0;
    }

    @Override
    public <T> Message serialize(T t) {
        return null;
    }

    @Override
    public <T> T deserialize(Message message, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> List<T> deserializeList(Message message, Class<T> aClass) {
        return null;
    }
}
