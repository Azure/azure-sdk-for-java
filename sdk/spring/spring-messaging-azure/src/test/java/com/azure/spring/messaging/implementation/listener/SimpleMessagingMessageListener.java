// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.listener;

import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.messaging.implementation.listener.adapter.MessagingMessageListenerAdapter;

public class SimpleMessagingMessageListener extends MessagingMessageListenerAdapter implements MessageListener<String> {

    @Override
    public void onMessage(String message) {

    }
}
