// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.nativex.configuration.implementation.autoconfigure;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsCommonProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = EventHubClientBuilder.class,
    types = {
        @TypeHint(types = {
            AzureEventHubsProperties.class,
            AzureEventHubsProperties.Consumer.class,
            AzureEventHubsProperties.Processor.class,
            AzureEventHubsProperties.Processor.StartPosition.class,
            AzureEventHubsProperties.Processor.LoadBalancing.class,
            AzureEventHubsProperties.Processor.EventBatch.class,
            AzureEventHubsProperties.Processor.BlobCheckpointStore.class,
            AzureEventHubsCommonProperties.class
        }, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS })
    }
)
public class EventHubsHints implements NativeConfiguration {
}
