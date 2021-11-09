// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties;

import com.azure.spring.core.aware.AzureProfileAware;
import com.azure.spring.core.aware.ClientAware;
import com.azure.spring.core.aware.ProxyAware;
import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.aware.authentication.TokenCredentialAware;

/**
 * Unified properties for Azure SDK clients.
 */
public interface AzureProperties extends RetryAware, ClientAware, ProxyAware, TokenCredentialAware, AzureProfileAware {

}
