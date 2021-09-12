// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties;

import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.credential.TokenCredentialProperties;
import com.azure.spring.core.properties.profile.AzureProfile;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.RetryProperties;

/**
 * Unified properties for Azure SDK clients.
 */
public interface AzureProperties {

    ClientProperties getClient();

    ProxyProperties getProxy();

    RetryProperties getRetry();

    TokenCredentialProperties getCredential();

    AzureProfile getProfile();

}
