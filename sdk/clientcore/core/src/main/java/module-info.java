// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import io.clientcore.core.http.client.HttpClientProvider;

module io.clientcore.core {
    requires transitive io.clientcore.core.json;

    // public API surface area
    exports io.clientcore.core.annotation;
    exports io.clientcore.core.credential;
    exports io.clientcore.core.http;
    exports io.clientcore.core.http.annotation;
    exports io.clientcore.core.http.client;
    exports io.clientcore.core.http.exception;
    exports io.clientcore.core.http.models;
    exports io.clientcore.core.http.pipeline;
    exports io.clientcore.core.models.traits;
    exports io.clientcore.core.util;
    exports io.clientcore.core.util.binarydata;
    exports io.clientcore.core.util.configuration;
    exports io.clientcore.core.util.serializer;
    exports io.clientcore.core.util.auth;

    // This is for exporting IOUtils and other utils classes
    exports io.clientcore.core.implementation.util;
    exports io.clientcore.core.implementation;

    uses HttpClientProvider;
}
