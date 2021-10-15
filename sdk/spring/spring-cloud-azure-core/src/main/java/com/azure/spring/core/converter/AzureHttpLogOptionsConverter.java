// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.converter;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.spring.core.properties.client.ClientProperties;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts a {@link ClientProperties.LoggingProperties} to a {@link HttpLogOptions}.
 */
public final class AzureHttpLogOptionsConverter implements Converter<ClientProperties.LoggingProperties, HttpLogOptions> {

    @Override
    public HttpLogOptions convert(ClientProperties.LoggingProperties logging) {
        HttpLogOptions logOptions = new HttpLogOptions();

        logOptions.setLogLevel(logging.getLevel());
        logOptions.setPrettyPrintBody(Boolean.TRUE.equals(logging.getPrettyPrintBody()));
        logOptions.setAllowedQueryParamNames(logging.getAllowedQueryParamNames());
        logOptions.setAllowedHeaderNames(logging.getAllowedHeaderNames());

        return logOptions;
    }
}
