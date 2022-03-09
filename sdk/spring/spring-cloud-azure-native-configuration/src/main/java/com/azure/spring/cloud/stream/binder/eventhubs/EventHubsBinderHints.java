// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.spring.cloud.stream.binder.eventhubs.config.EventHubsBinderConfiguration;
import com.azure.spring.cloud.stream.binder.eventhubs.config.EventHubsBinderHealthIndicatorConfiguration;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelResourceManagerProvisioner;
import org.springframework.cloud.stream.function.FunctionConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = FunctionConfiguration.class,
    types = {
        @TypeHint(
            types = {
                EventHubsBinderConfiguration.class,
                EventHubsBinderHealthIndicatorConfiguration.class,
                EventHubsBindingProperties.class,
                EventHubsProducerProperties.class,
                EventHubsConsumerProperties.class,
                EventHubsExtendedBindingProperties.class,
                EventHubsChannelProvisioner.class,
                EventHubsChannelResourceManagerProvisioner.class
            },
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.DECLARED_CLASSES }
        )
    }
)
public class EventHubsBinderHints implements NativeConfiguration {
}
