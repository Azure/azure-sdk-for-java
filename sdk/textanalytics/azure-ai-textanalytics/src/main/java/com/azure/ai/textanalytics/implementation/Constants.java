// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.core.http.policy.HttpLogOptions;

import java.util.function.Supplier;

public class Constants {

    public static final Supplier<HttpLogOptions> DEFAULT_LOG_OPTIONS_SUPPLIER = () -> {
        HttpLogOptions logOptions = new HttpLogOptions();

        logOptions.addAllowedHeaderName("Operation-Location");
        logOptions.addAllowedHeaderName("x-envoy-upstream-service-time");
        logOptions.addAllowedHeaderName("apim-request-id");
        logOptions.addAllowedHeaderName("Strict-Transport-Security");
        logOptions.addAllowedHeaderName("x-content-type-options");

        logOptions.addAllowedQueryParamName("jobId");
        logOptions.addAllowedQueryParamName("$top");
        logOptions.addAllowedQueryParamName("$skip");
        logOptions.addAllowedQueryParamName("showStats");
        logOptions.addAllowedQueryParamName("model-version");
        logOptions.addAllowedQueryParamName("domain");
        logOptions.addAllowedQueryParamName("stringIndexType");
        logOptions.addAllowedQueryParamName("piiCategories");
        logOptions.addAllowedQueryParamName("opinionMining");

        return logOptions;
    };
}
