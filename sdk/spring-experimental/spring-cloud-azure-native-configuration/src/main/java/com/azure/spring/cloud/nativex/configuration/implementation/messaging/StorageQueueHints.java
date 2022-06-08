// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.nativex.configuration.implementation.messaging;

import com.azure.spring.messaging.storage.queue.core.properties.StorageQueueProperties;
import com.azure.storage.queue.QueueClientBuilder;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = QueueClientBuilder.class,
    types = @TypeHint(
        types = StorageQueueProperties.class,
        access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
    )
)
public class StorageQueueHints implements NativeConfiguration {
}
