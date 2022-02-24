// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.credential.resolver;

import com.azure.core.credential.AzureSasCredential;
import com.azure.spring.core.aware.authentication.SasTokenAware;
import com.azure.spring.core.credential.AzureCredentialResolver;
import com.azure.spring.core.properties.AzureProperties;
import org.springframework.util.StringUtils;

/**
 * Resolve the sas token credential according to the azure properties.
 */
public final class AzureSasCredentialResolver implements AzureCredentialResolver<AzureSasCredential> {

    @Override
    public AzureSasCredential resolve(AzureProperties properties) {
        if (!isResolvable(properties)) {
            return null;
        }

        String sasToken = ((SasTokenAware) properties).getSasToken();
        if (!StringUtils.hasText(sasToken)) {
            return null;
        }

        return new AzureSasCredential(sasToken);
    }

    @Override
    public boolean isResolvable(AzureProperties properties) {
        return properties instanceof SasTokenAware;
    }
}
