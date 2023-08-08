// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.perf;

import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServiceBusTestUtil {

    public static List<IMessage> getMessagesToSend(int messageSize, int messageToSend) {
        String messageContent = TestDataCreationHelper.generateRandomString(messageSize);
        List<IMessage> messages = new ArrayList<>();
        for (int i = 0; i < messageToSend; ++i) {
            Message message = new Message(messageContent);
            message.setMessageId(UUID.randomUUID().toString());
            messages.add(message);
        }

        return messages;
    }
}
