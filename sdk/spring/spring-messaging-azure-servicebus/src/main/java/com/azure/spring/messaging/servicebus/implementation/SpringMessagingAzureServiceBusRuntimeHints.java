// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation;

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
                "com.azure.spring.messaging.servicebus.core.properties.CommonProperties",
                "com.azure.spring.messaging.servicebus.core.properties.ConsumerProperties",
                "com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties",
                "com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties",
                "com.azure.spring.messaging.servicebus.core.properties.ProducerProperties",
                "com.azure.spring.messaging.servicebus.core.properties.ServiceBusContainerProperties"
                )
            .forEach(typeName -> reflectionHints.registerTypeIfPresent(classLoader, typeName,
                builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_METHODS)));
    }

}
