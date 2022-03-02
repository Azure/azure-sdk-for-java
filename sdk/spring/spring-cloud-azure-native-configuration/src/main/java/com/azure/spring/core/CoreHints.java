// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;

import com.azure.spring.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.core.implementation.properties.AzureSdkProperties;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.core.properties.client.AmqpClientProperties;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.client.HeaderProperties;
import com.azure.spring.core.properties.client.HttpClientProperties;
import com.azure.spring.core.properties.client.HttpLoggingProperties;
import com.azure.spring.core.properties.profile.AzureEnvironmentProperties;
import com.azure.spring.core.properties.profile.AzureProfileAdapter;
import com.azure.spring.core.properties.profile.AzureProfileProperties;
import com.azure.spring.core.properties.proxy.HttpProxyProperties;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.core.properties.retry.AmqpRetryProperties;
import com.azure.spring.core.properties.retry.RetryProperties;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = AzureProperties.class,
    types = {
        @TypeHint(
            types = {
                AzureProfileAdapter.class,
                AzureHttpSdkProperties.class,
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
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        )
    }
)
public class CoreHints implements NativeConfiguration {
}
