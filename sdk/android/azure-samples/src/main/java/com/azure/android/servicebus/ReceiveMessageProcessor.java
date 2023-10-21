package com.azure.android.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;

public class ReceiveMessageProcessor {

    public static boolean processMessage(ServiceBusReceivedMessage message) {
        System.out.printf("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(),
            message.getBody());

        return true;
    }
}
