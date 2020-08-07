/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.messaging.annotation;

import com.microsoft.azure.spring.messaging.config.AzureBootstrapConfiguration;
import com.microsoft.azure.spring.messaging.config.AzureMessagingConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({AzureMessagingConfiguration.class, AzureBootstrapConfiguration.class})
public @interface EnableAzureMessaging {
}
