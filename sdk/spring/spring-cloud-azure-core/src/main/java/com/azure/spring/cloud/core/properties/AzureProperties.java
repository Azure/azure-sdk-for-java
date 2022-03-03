// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties;

import com.azure.spring.cloud.core.aware.AzureProfileOptionsAware;
import com.azure.spring.cloud.core.aware.ClientOptionsAware;
import com.azure.spring.cloud.core.aware.ProxyOptionsAware;
import com.azure.spring.cloud.core.aware.authentication.TokenCredentialOptionsAware;

/**
 * Unified properties for Azure SDK clients.
 */
public interface AzureProperties extends ClientOptionsAware, ProxyOptionsAware,
    TokenCredentialOptionsAware, AzureProfileOptionsAware {

}
