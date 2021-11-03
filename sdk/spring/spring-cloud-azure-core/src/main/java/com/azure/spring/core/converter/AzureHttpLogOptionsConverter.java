// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.converter;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.spring.core.aware.ClientAware;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts a {@link ClientAware.Logging} to a {@link HttpLogOptions}.
 */
public final class AzureHttpLogOptionsConverter implements Converter<ClientAware.Logging, HttpLogOptions> {

    public static final AzureHttpLogOptionsConverter HTTP_LOG_OPTIONS_CONVERTER = new AzureHttpLogOptionsConverter();

    @Override
    public HttpLogOptions convert(ClientAware.Logging logging) {
        HttpLogOptions logOptions = new HttpLogOptions();

        logOptions.setLogLevel(logging.getLevel());
        logOptions.setPrettyPrintBody(Boolean.TRUE.equals(logging.getPrettyPrintBody()));
        logOptions.setAllowedQueryParamNames(logging.getAllowedQueryParamNames());
        logOptions.setAllowedHeaderNames(logging.getAllowedHeaderNames());

        return logOptions;
    }
}
