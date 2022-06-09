// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.nativex.configuration.implementation.core;

import com.azure.spring.cloud.core.implementation.properties.AzureAmqpSdkProperties;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.cloud.core.implementation.properties.AzureSdkProperties;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.client.AmqpClientProperties;
import com.azure.spring.cloud.core.properties.client.ClientProperties;
import com.azure.spring.cloud.core.properties.client.HeaderProperties;
import com.azure.spring.cloud.core.properties.client.HttpClientProperties;
import com.azure.spring.cloud.core.properties.client.HttpLoggingProperties;
import com.azure.spring.cloud.core.properties.profile.AzureEnvironmentProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileOptionsAdapter;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;
import com.azure.spring.cloud.core.properties.proxy.HttpProxyProperties;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.core.properties.retry.AmqpRetryProperties;
import com.azure.spring.cloud.core.properties.retry.RetryProperties;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = AzureProperties.class,
    types = {
        @TypeHint(
            types = {
                AzureProfileOptionsAdapter.class,
                AzureHttpSdkProperties.class,
                AzureAmqpSdkProperties.class,
                AzureSdkProperties.class,
                AzureEnvironmentProperties.class,
                AzureProfileProperties.class,
                TokenCredentialProperties.class,
                NamedKeyProperties.class,
                HttpClientProperties.class,
                ClientProperties.class,
                RetryProperties.class,
                AmqpRetryProperties.class,
                ProxyProperties.class,
                HttpProxyProperties.class,
                ClientProperties.class,
                AmqpClientProperties.class,
                HeaderProperties.class,
                HttpClientProperties.class,
                HttpLoggingProperties.class,
                AzureResourceMetadata.class
            },
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS })
    }
)
@ResourceHint(patterns = { "META-INF/project.properties", "additional-spring-configuration-metadata.json"})
public class CoreHints implements NativeConfiguration {
}
