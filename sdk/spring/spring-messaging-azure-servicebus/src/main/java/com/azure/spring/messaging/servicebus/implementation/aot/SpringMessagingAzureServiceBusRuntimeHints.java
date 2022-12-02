// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.aot;

import com.azure.spring.messaging.servicebus.core.properties.CommonProperties;
import com.azure.spring.messaging.servicebus.core.properties.ConsumerProperties;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProducerProperties;
import com.azure.spring.messaging.servicebus.core.properties.ServiceBusContainerProperties;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.stream.Stream;

class SpringMessagingAzureServiceBusRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        ReflectionHints reflectionHints = hints.reflection();
        Stream.of(
                CommonProperties.class,
                ConsumerProperties.class,
                NamespaceProperties.class,
                ProcessorProperties.class,
                ProducerProperties.class,
                ServiceBusContainerProperties.class)
            .forEach(type -> reflectionHints.registerType(type,
                builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_METHODS)));
    }

}
