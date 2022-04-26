// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.nativex.configuration.implementation.messaging;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.messaging.eventhubs.core.properties.CommonProperties;
import com.azure.spring.messaging.eventhubs.core.properties.ConsumerProperties;
import com.azure.spring.messaging.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.messaging.eventhubs.core.properties.ProcessorProperties;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = SendOperation.class,
    types = @TypeHint(
        types = {
            CommonProperties.class,
            ConsumerProperties.class,
            ProcessorProperties.class,
            NamespaceProperties.class,
            CheckpointStore.class
        },
        access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
    )
)
public class EventHubsHints implements NativeConfiguration {
}
