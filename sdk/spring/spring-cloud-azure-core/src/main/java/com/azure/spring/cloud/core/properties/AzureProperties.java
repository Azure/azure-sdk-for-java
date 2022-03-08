// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties;

import com.azure.spring.cloud.core.aware.AzureProfileOptionsProvider;
import com.azure.spring.cloud.core.aware.ClientOptionsProvider;
import com.azure.spring.cloud.core.aware.ProxyOptionsProvider;
import com.azure.spring.cloud.core.aware.authentication.TokenCredentialOptionsProvider;

/**
 * Unified properties for Azure SDK clients.
 */
public interface AzureProperties extends ClientOptionsProvider, ProxyOptionsProvider,
    TokenCredentialOptionsProvider, AzureProfileOptionsProvider {

}
