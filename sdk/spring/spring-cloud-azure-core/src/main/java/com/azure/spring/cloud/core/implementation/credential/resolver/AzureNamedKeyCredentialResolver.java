// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.cloud.core.provider.authentication.NamedKeyProvider;
import org.springframework.util.StringUtils;

/**
 * Resolve the named key credential according to the azure properties.
 */
public final class AzureNamedKeyCredentialResolver implements AzureCredentialResolver<AzureNamedKeyCredential> {

    @Override
    public AzureNamedKeyCredential resolve(AzureProperties properties) {
        if (!isResolvable(properties)) {
            return null;
        }

        NamedKeyProperties namedKey = ((NamedKeyProvider) properties).getNamedKey();
        if (namedKey == null || !StringUtils.hasText(namedKey.getName()) || !StringUtils.hasText(namedKey.getKey())) {
            return null;
        }

        return new AzureNamedKeyCredential(namedKey.getName(), namedKey.getKey());
    }

    @Override
    public boolean isResolvable(AzureProperties properties) {
        return properties instanceof NamedKeyProvider;
    }

}
