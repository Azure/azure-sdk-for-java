// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.authentication.KeyProvider;
import org.springframework.util.StringUtils;

/**
 * Resolve the token credential according to the azure properties.
 */
public final class AzureKeyCredentialResolver implements AzureCredentialResolver<AzureKeyCredential> {

    @Override
    public AzureKeyCredential resolve(AzureProperties properties) {
        if (!isResolvable(properties)) {
            return null;
        }

        String key = ((KeyProvider) properties).getKey();
        if (!StringUtils.hasText(key)) {
            return null;
        }

        return new AzureKeyCredential(key);
    }

    @Override
    public boolean isResolvable(AzureProperties properties) {
        return properties instanceof KeyProvider;
    }

}
