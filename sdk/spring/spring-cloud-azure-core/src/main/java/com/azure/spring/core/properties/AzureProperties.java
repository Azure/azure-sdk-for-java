// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties;

import com.azure.spring.core.aware.AzureProfileOptionsAware;
import com.azure.spring.core.aware.ClientOptionsAware;
import com.azure.spring.core.aware.ProxyOptionsAware;
import com.azure.spring.core.aware.RetryOptionsAware;
import com.azure.spring.core.aware.authentication.TokenCredentialOptionsAware;

/**
 * Unified properties for Azure SDK clients.
 */
public interface AzureProperties extends RetryOptionsAware, ClientOptionsAware, ProxyOptionsAware, TokenCredentialOptionsAware, AzureProfileOptionsAware {

}
