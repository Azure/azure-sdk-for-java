// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.annotation;

import com.azure.spring.messaging.implementation.config.AzureMessagingBootstrapConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * With using this annotation, method annotated with Azure Message Listener will
 * automatically be registered as a message listener.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AzureMessagingBootstrapConfiguration.class)
public @interface EnableAzureMessaging {

}
