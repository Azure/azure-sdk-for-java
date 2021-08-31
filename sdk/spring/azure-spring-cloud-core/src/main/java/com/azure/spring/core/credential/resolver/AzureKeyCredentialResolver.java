// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.resolver;

import com.azure.spring.core.credential.provider.AzureKeyCredentialProvider;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.aware.credential.KeyAware;
import org.springframework.util.StringUtils;

/**
 * Resolve the token credential according to the azure properties.
 */
public class AzureKeyCredentialResolver implements AzureCredentialResolver<AzureKeyCredentialProvider> {

    @Override
    public AzureKeyCredentialProvider resolve(AzureProperties properties) {
        if (!isResolvable(properties)) {
            return null;
        }

        String key = ((KeyAware) properties).getKey();
        if (!StringUtils.hasText(key)) {
            return null;
        }

        return new AzureKeyCredentialProvider(key);
    }

    @Override
    public boolean isResolvable(AzureProperties properties) {
        return properties instanceof KeyAware;
    }

}
