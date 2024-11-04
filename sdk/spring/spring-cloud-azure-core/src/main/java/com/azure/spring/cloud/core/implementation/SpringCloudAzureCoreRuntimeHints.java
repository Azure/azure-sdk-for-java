// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation;

import com.azure.spring.cloud.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.client.AmqpClientProperties;
import com.azure.spring.cloud.core.properties.client.ClientProperties;
import com.azure.spring.cloud.core.properties.client.HeaderProperties;
import com.azure.spring.cloud.core.properties.client.HttpClientProperties;
import com.azure.spring.cloud.core.properties.client.HttpLoggingProperties;
import com.azure.spring.cloud.core.properties.profile.AzureEnvironmentProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;
import com.azure.spring.cloud.core.properties.proxy.AmqpProxyProperties;
import com.azure.spring.cloud.core.properties.proxy.HttpProxyProperties;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;
import com.azure.spring.cloud.core.properties.retry.AmqpRetryProperties;
import com.azure.spring.cloud.core.properties.retry.ExponentialRetryProperties;
import com.azure.spring.cloud.core.properties.retry.FixedRetryProperties;
import com.azure.spring.cloud.core.properties.retry.RetryProperties;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.stream.Stream;

class SpringCloudAzureCoreRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        ReflectionHints reflectionHints = hints.reflection();
        Stream.of(
                TokenCredentialProperties.class,
                NamedKeyProperties.class,
                AmqpClientProperties.class,
                ClientProperties.class,
                HeaderProperties.class,
                HttpClientProperties.class,
                HttpLoggingProperties.class,
                AzureEnvironmentProperties.class,
                AzureProfileProperties.class,
                AmqpProxyProperties.class,
                HttpProxyProperties.class,
                ProxyProperties.class,
                AmqpRetryProperties.class,
                ExponentialRetryProperties.class,
                FixedRetryProperties.class,
                RetryProperties.class)
            .forEach(type -> reflectionHints.registerType(type,
                builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_METHODS)));
    }

}
