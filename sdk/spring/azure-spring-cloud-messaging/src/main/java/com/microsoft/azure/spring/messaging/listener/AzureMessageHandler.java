/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.messaging.listener;

import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageHandler;

public interface AzureMessageHandler extends MessageHandler {

    @Nullable
    Class<?> getMessagePayloadType();
}
