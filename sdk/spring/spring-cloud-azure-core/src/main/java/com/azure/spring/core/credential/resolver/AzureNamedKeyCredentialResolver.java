// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.resolver;

import com.azure.spring.core.credential.provider.AzureNamedKeyCredentialProvider;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.aware.credential.NamedKeyAware;
import com.azure.spring.core.properties.credential.NamedKeyProperties;
import org.springframework.util.StringUtils;

/**
 * Resolve the named key credential according to the azure properties.
 */
public class AzureNamedKeyCredentialResolver implements AzureCredentialResolver<AzureNamedKeyCredentialProvider> {

    @Override
    public AzureNamedKeyCredentialProvider resolve(AzureProperties properties) {
        if (!isResolvable(properties)) {
            return null;
        }

        NamedKeyProperties namedKey = ((NamedKeyAware) properties).getNamedKey();
        if (namedKey == null || !StringUtils.hasText(namedKey.getName()) || !StringUtils.hasText(namedKey.getKey())) {
            return null;
        }

        return new AzureNamedKeyCredentialProvider(namedKey.getName(), namedKey.getKey());
    }

    @Override
    public boolean isResolvable(AzureProperties properties) {
        return properties instanceof NamedKeyAware;
    }

}
