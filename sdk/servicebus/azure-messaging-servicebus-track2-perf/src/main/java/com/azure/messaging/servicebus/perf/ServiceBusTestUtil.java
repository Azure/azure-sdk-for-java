// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.perf.test.core.TestDataCreationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServiceBusTestUtil {

    public static List<ServiceBusMessage> geMessagesToSend(int messageSize, int messageToSend) {
        List<ServiceBusMessage> messages = new ArrayList<>();
        for (int i = 0; i < messageToSend; i++) {
            String messageContent = TestDataCreationHelper.generateRandomString(messageSize);
            ServiceBusMessage message = new ServiceBusMessage(messageContent);
            message.setMessageId(UUID.randomUUID().toString());
            messages.add(message);
        }
        return messages;
    }
}
