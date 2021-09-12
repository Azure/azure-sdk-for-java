// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.converter;

import com.azure.core.util.Configuration;
import com.azure.spring.core.properties.AzureProperties;
import org.springframework.core.convert.converter.Converter;


/**
 * Converts a {@link AzureProperties} to a {@link Configuration}.
 */
public final class AzureConfigurationConverter implements Converter<AzureProperties, Configuration> {

    @Override
    public Configuration convert(AzureProperties source) {
        return null;
    }
}
