// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.messaging.listener;

import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageHandler;

public interface AzureMessageHandler extends MessageHandler {

    @Nullable
    Class<?> getMessagePayloadType();
}
