// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core.properties;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.messaging.core.SendOperation;
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
public class MessageEventHubsHints implements NativeConfiguration {
}
