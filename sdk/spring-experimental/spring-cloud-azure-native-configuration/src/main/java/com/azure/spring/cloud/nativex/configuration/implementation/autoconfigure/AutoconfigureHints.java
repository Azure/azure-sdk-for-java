// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.nativex.configuration.implementation.autoconfigure;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureAmqpConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureServiceConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.client.AmqpClientConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.client.HttpClientConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.proxy.AmqpProxyConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.proxy.HttpProxyConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.retry.AmqpRetryConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.resourcemanager.AzureResourceMetadataConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.StorageRetryConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.client.ClientConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.client.HttpLoggingConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.profile.AzureProfileConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.proxy.ProxyConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.retry.RetryConfigurationProperties;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = AzureGlobalProperties.class,
    types = {
        @TypeHint(
            types = {
                AzureProfileConfigurationProperties.class,
                TokenCredentialConfigurationProperties.class,
                AzureResourceMetadataConfigurationProperties.class,
                AbstractAzureHttpConfigurationProperties.class,
                AbstractAzureServiceConfigurationProperties.class,
                AbstractAzureAmqpConfigurationProperties.class,
                AzureProfileConfigurationProperties.class,
                AzureStorageProperties.class,
                RetryConfigurationProperties.class,
                StorageRetryConfigurationProperties.class,
                AzureGlobalProperties.GlobalRetryConfigurationProperties.class,
                AmqpRetryConfigurationProperties.class,
                ProxyConfigurationProperties.class,
                AzureGlobalProperties.GlobalProxyConfigurationProperties.class,
                HttpProxyConfigurationProperties.class,
                AmqpProxyConfigurationProperties.class,
                HttpLoggingConfigurationProperties.class,
                ClientConfigurationProperties.class,
                HttpClientConfigurationProperties.class,
                AzureGlobalProperties.GlobalClientConfigurationProperties.class,
                AmqpClientConfigurationProperties.class
            },
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS })
    }
)
public class AutoconfigureHints implements NativeConfiguration {
}
