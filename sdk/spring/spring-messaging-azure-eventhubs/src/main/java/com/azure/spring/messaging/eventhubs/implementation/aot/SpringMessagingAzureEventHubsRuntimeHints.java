// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.aot;

import com.azure.spring.messaging.eventhubs.core.properties.CommonProperties;
import com.azure.spring.messaging.eventhubs.core.properties.ConsumerProperties;
import com.azure.spring.messaging.eventhubs.core.properties.EventHubsContainerProperties;
import com.azure.spring.messaging.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.messaging.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.messaging.eventhubs.core.properties.ProducerProperties;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.stream.Stream;

class SpringMessagingAzureEventHubsRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        ReflectionHints reflectionHints = hints.reflection();
        Stream.of(
                CommonProperties.class,
                ConsumerProperties.class,
                EventHubsContainerProperties.class,
                NamespaceProperties.class,
                ProcessorProperties.class,
                ProducerProperties.class)
            .forEach(type -> reflectionHints.registerType(type,
                builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_METHODS)));
    }

}
