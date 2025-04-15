// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger.util;

import com.azure.spring.cloud.core.implementation.properties.AzureAmqpSdkProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.client.AmqpClientProperties;
import com.azure.spring.cloud.core.properties.client.ClientProperties;
import com.azure.spring.cloud.core.properties.profile.AzureEnvironmentProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileOptionsAdapter;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;
import com.azure.spring.cloud.core.properties.proxy.AmqpProxyProperties;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;
import com.azure.spring.cloud.core.properties.retry.AmqpRetryProperties;
import com.azure.spring.cloud.core.properties.retry.ExponentialRetryProperties;
import com.azure.spring.cloud.core.properties.retry.FixedRetryProperties;
import com.azure.spring.cloud.core.properties.retry.RetryProperties;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Consumer;

public class TestPropertiesUtils {

    public static final String SETTER_METHOD = "setter";
    public static final String GETTER_METHOD = "getter";
    private static final String OTHERS_METHOD = "others";

    public static final Set<Class<?>> IGNORED_CLASSES = Set.of(Consumer.class, Object.class);

    public static final Set<String> BUILT_IN_MEMBER_VARIABLE_NAMES =
        Set.of("client", "credential", "environment", "exponential", "fixed", "profile", "proxy", "retry");

    public static final Class<?>[] NO_SETTER_PROPERTIES_CLASSES = new Class<?>[] {
        AmqpClientProperties.class,
        AmqpProxyProperties.class,
        AmqpRetryProperties.class,
        AzureAmqpSdkProperties.class,
        AzureEnvironmentProperties.class,
        AzureProfileOptionsAdapter.class,
        AzureProfileProperties.class,
        ClientProperties.class,
        ExponentialRetryProperties.class,
        FixedRetryProperties.class,
        ProxyProperties.class,
        RetryProperties.class,
        TokenCredentialProperties.class
    };

    public static String groupMethodName(Method method) {
        if (isGetter(method)) {
            return GETTER_METHOD;
        }

        if (isSetter(method)) {
            return SETTER_METHOD;
        }

        return OTHERS_METHOD;
    }

    public static boolean isGetter(Method method) {
        if (!method.getName().startsWith("get") && !method.getName().startsWith("is")) {
            return false;
        }
        if (method.getParameterCount() != 0) {
            return false;
        }

        return !void.class.equals(method.getReturnType());
    }

    public static boolean isSetter(Method method) {
        if (!method.getName().startsWith("set")) {
            return false;
        }
        if (method.getParameterCount() != 1) {
            return false;
        }

        return void.class.equals(method.getReturnType());
    }
}
