// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.listener;

import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageHandler;

/**
 * Interface for Azure message handler.
 */
public interface AzureMessageHandler extends MessageHandler {

    @Nullable
    Class<?> getMessagePayloadType();
}
