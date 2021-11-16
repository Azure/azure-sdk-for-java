// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.resolver;

import com.azure.spring.core.aware.authentication.SasTokenAware;
import com.azure.spring.core.credential.provider.AzureSasCredentialProvider;
import com.azure.spring.core.properties.AzureProperties;
import org.springframework.util.StringUtils;

/**
 * Resolve the sas token credential according to the azure properties.
 */
public class AzureSasCredentialResolver implements AzureCredentialResolver<AzureSasCredentialProvider> {

    @Override
    public AzureSasCredentialProvider resolve(AzureProperties properties) {
        if (!isResolvable(properties)) {
            return null;
        }

        String sasToken = ((SasTokenAware) properties).getSasToken();
        if (!StringUtils.hasText(sasToken)) {
            return null;
        }

        return new AzureSasCredentialProvider(sasToken);
    }

    @Override
    public boolean isResolvable(AzureProperties properties) {
        return properties instanceof SasTokenAware;
    }
}
