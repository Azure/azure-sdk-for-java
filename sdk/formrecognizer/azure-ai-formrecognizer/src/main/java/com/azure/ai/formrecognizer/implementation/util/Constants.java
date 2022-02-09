// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.ClientOptions;

import java.time.Duration;
import java.util.function.Supplier;

public class Constants {

    static final String DEFAULT_SCOPE = "https://cognitiveservices.azure.com/.default";
    static final String FORM_RECOGNIZER_PROPERTIES = "azure-ai-formrecognizer.properties";
    static final String NAME = "name";
    static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    static final String VERSION = "version";
    static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();
    static final HttpHeaders DEFAULT_HTTP_HEADERS = new HttpHeaders();
    static final HttpLogOptions DEFAULT_LOG_OPTIONS = new HttpLogOptions();
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);
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
