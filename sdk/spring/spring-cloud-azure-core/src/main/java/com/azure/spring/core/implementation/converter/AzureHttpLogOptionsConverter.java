// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.converter;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.spring.core.aware.HttpLoggingAware;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts a {@link HttpLoggingAware} to a {@link HttpLogOptions}.
 */
public final class AzureHttpLogOptionsConverter implements Converter<HttpLoggingAware.HttpLogging, HttpLogOptions> {

    public static final AzureHttpLogOptionsConverter HTTP_LOG_OPTIONS_CONVERTER = new AzureHttpLogOptionsConverter();

    private AzureHttpLogOptionsConverter() {

    }

    @Override
    public HttpLogOptions convert(HttpLoggingAware.HttpLogging logging) {
        HttpLogOptions logOptions = new HttpLogOptions();

        logOptions.setLogLevel(logging.getLevel());
        logOptions.setPrettyPrintBody(Boolean.TRUE.equals(logging.getPrettyPrintBody()));
        logOptions.setAllowedQueryParamNames(logging.getAllowedQueryParamNames());
        logOptions.setAllowedHeaderNames(logging.getAllowedHeaderNames());

        return logOptions;
    }
}
