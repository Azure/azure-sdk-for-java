// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.messaging.annotation;

import com.microsoft.azure.spring.messaging.config.AzureBootstrapConfiguration;
import com.microsoft.azure.spring.messaging.config.AzureMessagingConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({AzureMessagingConfiguration.class, AzureBootstrapConfiguration.class})
public @interface EnableAzureMessaging {
}
