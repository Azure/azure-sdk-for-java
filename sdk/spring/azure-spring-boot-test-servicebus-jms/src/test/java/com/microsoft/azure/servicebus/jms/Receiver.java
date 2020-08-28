// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.jms;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.Exchanger;

@Component
public class Receiver {

    public static final String QUEUE_NAME = "que001";

    public static final Exchanger<String> EXCHANGER = new Exchanger<>();

    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void receiveMessage(User user) throws InterruptedException {
        EXCHANGER.exchange(user.getName());
    }

}
