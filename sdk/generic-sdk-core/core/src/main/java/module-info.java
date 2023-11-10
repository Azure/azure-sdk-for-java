// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.typespec.core.http.client.HttpClientProvider;

module com.typespec.core {

    requires transitive org.slf4j;
    requires transitive com.typespec.json;

    // public API surface area
    exports com.typespec.core.annotation;
    exports com.typespec.core.credential;
    exports com.typespec.core.http.exception;
    exports com.typespec.core.models;

    exports com.typespec.core.http;
    exports com.typespec.core.http.client;
    exports com.typespec.core.http.models;
    exports com.typespec.core.http.annotation;
    exports com.typespec.core.http.pipeline;
    exports com.typespec.core.http.policy;

    exports com.typespec.core.util.serializer;
    exports com.typespec.core.util;
    exports com.typespec.core.util.configuration;

    // Service Provider Interfaces
    uses HttpClientProvider;
}
