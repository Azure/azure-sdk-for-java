/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.servicebus;

import com.microsoft.azure.servicebus.ReceiveMode;

public class Constants {
    public static final String CONNECTION_STRING_PROPERTY = "azure.servicebus.connection-string";
    public static final String QUEUE_NAME_PROPERTY = "azure.servicebus.queue-name";
    public static final String QUEUE_RECEIVE_MODE_PROPERTY = "azure.servicebus.queue-receive-mode";
    public static final String TOPIC_NAME_PROPERTY = "azure.servicebus.topic-name";
    public static final String SUBSCRIPTION_NAME_PROPERTY = "azure.servicebus.subscription-name";
    public static final String SUBSCRIPTION_RECEIVE_MODE_PROPERTY = "azure.servicebus.subscription-receive-mode";

    public static final String INVALID_CONNECTION_STRING = "connection string";
    public static final String CONNECTION_STRING = "Endpoint=sb://test.servicebus.windows.net/;" +
            "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=dummy-key";
    public static final String QUEUE_NAME = "queue name";
    public static final ReceiveMode QUEUE_RECEIVE_MODE = ReceiveMode.PEEKLOCK;
    public static final String TOPIC_NAME = "topic name";
    public static final String SUBSCRIPTION_NAME = "subscription name";
    public static final ReceiveMode SUBSCRIPTION_RECEIVE_MODE = ReceiveMode.PEEKLOCK;
}
