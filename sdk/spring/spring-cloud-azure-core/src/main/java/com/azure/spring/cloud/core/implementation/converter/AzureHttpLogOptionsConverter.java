// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.spring.cloud.core.provider.HttpLoggingOptionsProvider;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts a {@link HttpLoggingOptionsProvider.HttpLoggingOptions} to a {@link HttpLogOptions}.
 */
public final class AzureHttpLogOptionsConverter implements Converter<HttpLoggingOptionsProvider.HttpLoggingOptions, HttpLogOptions> {

    public static final AzureHttpLogOptionsConverter HTTP_LOG_OPTIONS_CONVERTER = new AzureHttpLogOptionsConverter();

    private AzureHttpLogOptionsConverter() {

    }

    @Override
    public HttpLogOptions convert(HttpLoggingOptionsProvider.HttpLoggingOptions logging) {
        HttpLogOptions logOptions = new HttpLogOptions();

        logOptions.setLogLevel(logging.getLevel())
                  .setPrettyPrintBody(Boolean.TRUE.equals(logging.getPrettyPrintBody()))
                  .setAllowedQueryParamNames(logging.getAllowedQueryParamNames())
                  .setAllowedHeaderNames(logging.getAllowedHeaderNames());

        return logOptions;
    }
}
