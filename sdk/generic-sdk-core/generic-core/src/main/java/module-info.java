// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.generic.core {
    requires com.generic.json;
    requires transitive org.slf4j;

    // public API surface area
    exports com.generic.core.annotation;
    exports com.generic.core.credential;
    exports com.generic.core.http;
    exports com.generic.core.http.annotation;
    exports com.generic.core.http.client;
    exports com.generic.core.http.exception;
    exports com.generic.core.http.models;
    exports com.generic.core.http.pipeline;
    exports com.generic.core.http.policy;
    exports com.generic.core.models.traits;
    exports com.generic.core.util;
    exports com.generic.core.util.binarydata;
    exports com.generic.core.util.configuration;
    exports com.generic.core.util.serializer;

    exports com.generic.core.implementation.util to com.generic.core.http.okhttp;

    uses com.generic.core.http.client.HttpClientProvider;
}
