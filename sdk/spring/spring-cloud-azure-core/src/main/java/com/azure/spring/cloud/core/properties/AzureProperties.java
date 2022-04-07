// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties;

import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.core.provider.ClientOptionsProvider;
import com.azure.spring.cloud.core.provider.ProxyOptionsProvider;
import com.azure.spring.cloud.core.provider.authentication.TokenCredentialOptionsProvider;

/**
 * Unified properties for Azure SDK clients.
 */
public interface AzureProperties extends ClientOptionsProvider, ProxyOptionsProvider,
    TokenCredentialOptionsProvider, AzureProfileOptionsProvider {

}
