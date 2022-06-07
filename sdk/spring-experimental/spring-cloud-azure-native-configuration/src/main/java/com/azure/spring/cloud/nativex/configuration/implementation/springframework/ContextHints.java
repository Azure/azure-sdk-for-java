// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.nativex.configuration.implementation.springframework;

import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.event.DefaultEventListenerFactory;
import org.springframework.context.event.EventListenerMethodProcessor;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    types = @TypeHint(
        types = {
            ConfigurationClassPostProcessor.class,
            EventListenerMethodProcessor.class,
            DefaultEventListenerFactory.class,
            CommonAnnotationBeanPostProcessor.class
        },
        access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
    ),
    jdkProxies = {
        @JdkProxyHint(types = org.springframework.context.annotation.Role.class)
    }
)
public class ContextHints implements NativeConfiguration {
}
