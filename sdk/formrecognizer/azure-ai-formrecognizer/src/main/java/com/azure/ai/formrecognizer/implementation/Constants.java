// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.core.http.policy.HttpLogOptions;

import java.util.function.Supplier;

public class Constants {

    public static final Supplier<HttpLogOptions> DEFAULT_LOG_OPTIONS_SUPPLIER = () -> {
        HttpLogOptions logOptions = new HttpLogOptions();

        logOptions.addAllowedHeaderName("Operation-Location");
        logOptions.addAllowedHeaderName("Location");
        logOptions.addAllowedHeaderName("x-envoy-upstream-service-time");
        logOptions.addAllowedHeaderName("apim-request-id");
        logOptions.addAllowedHeaderName("Strict-Transport-Security");
        logOptions.addAllowedHeaderName("x-content-type-options");
        logOptions.addAllowedHeaderName("ms-azure-ai-errorcode");
        logOptions.addAllowedHeaderName("x-ms-cs-error-code");

        logOptions.addAllowedQueryParamName("includeTextDetails");
        logOptions.addAllowedQueryParamName("locale");
        logOptions.addAllowedQueryParamName("language");
        logOptions.addAllowedQueryParamName("includeKeys");
        logOptions.addAllowedQueryParamName("op");
        logOptions.addAllowedQueryParamName("pages");
        logOptions.addAllowedQueryParamName("readingOrder");

        return logOptions;
    };
}
