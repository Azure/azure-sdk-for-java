// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus.utils;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;

public final class AzureServiceBusPropertiesUtils {

    private AzureServiceBusPropertiesUtils() {
    }

    public static String getServiceBusProperties(ConditionContext context, String... names) {
        Environment environment = context.getEnvironment();
        String property = null;
        for (String name : names) {
            property = environment.getProperty("spring.cloud.azure.servicebus." + name);
            if (property != null) {
                break;
            }
        }
        return property;
    }
}
