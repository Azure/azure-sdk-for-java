// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.http.policy.HttpLogOptions;

import java.util.function.Supplier;

public class Constants {
    public static final String ENUM_INTERNAL_ERROR_MSG = "The enum does not exist in internally used model %s.";
    public static final String ENUM_EXTERNAL_ERROR_MSG = "The enum does not exist in externally used model %s.";

    public static final String ABSTRACT_INTERNAL_ERROR_MSG = "The subclass does not exist in internal used model %s.";
    public static final String ABSTRACT_EXTERNAL_ERROR_MSG = "The subclass does not exist in external used model %s.";

    public static final Supplier<HttpLogOptions> DEFAULT_LOG_OPTIONS_SUPPLIER = () -> {
        HttpLogOptions logOptions = new HttpLogOptions();

        logOptions.addAllowedHeaderName("Access-Control-Allow-Credentials");
        logOptions.addAllowedHeaderName("Access-Control-Allow-Headers");
        logOptions.addAllowedHeaderName("Access-Control-Allow-Methods");
        logOptions.addAllowedHeaderName("Access-Control-Allow-Origin");
        logOptions.addAllowedHeaderName("Access-Control-Expose-Headers");
        logOptions.addAllowedHeaderName("Access-Control-Max-Age");
        logOptions.addAllowedHeaderName("Access-Control-Request-Headers");
        logOptions.addAllowedHeaderName("Access-Control-Request-Method");
        logOptions.addAllowedHeaderName("client-request-id");
        logOptions.addAllowedHeaderName("elapsed-time");
        logOptions.addAllowedHeaderName("Location");
        logOptions.addAllowedHeaderName("OData-MaxVersion");
        logOptions.addAllowedHeaderName("OData-Version");
        logOptions.addAllowedHeaderName("Origin");
        logOptions.addAllowedHeaderName("Prefer");
        logOptions.addAllowedHeaderName("request-id");
        logOptions.addAllowedHeaderName("return-client-request-id");
        logOptions.addAllowedHeaderName("throttle-reason");

        logOptions.addAllowedQueryParamName("api-version");
        logOptions.addAllowedQueryParamName("allowIndexDowntime");

        return logOptions;
    };
}
