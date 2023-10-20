// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.generic.core.http.client.jdk.JdkHttpClientProvider;

module com.generic.core {

    requires transitive org.slf4j;
    requires java.net.http;
    requires com.generic.json;

    // public API surface area
    exports com.generic.core.annotation;
    exports com.generic.core.credential;
    exports com.generic.core.exception;
    exports com.generic.core.http;
    exports com.generic.core.http.policy;
    exports com.generic.core.http.rest;
    exports com.generic.core.util;
    exports com.generic.core.util.logging;
    exports com.generic.core.util.serializer;
    exports com.generic.core.http.models;
    exports com.generic.core.http.client;
    exports com.generic.core.models;

    // Service Provider Interfaces
    uses com.generic.core.http.client.HttpClientProvider;
    uses com.generic.core.util.serializer.JsonSerializerProvider;

    provides com.generic.core.http.client.HttpClientProvider
        with JdkHttpClientProvider;

}
