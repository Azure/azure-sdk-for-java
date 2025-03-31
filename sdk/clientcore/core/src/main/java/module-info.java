// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * This module provides core functionality for the Java SDK.
 */
module io.clientcore.core {
    requires transitive java.xml;

    requires java.net.http;
    requires jdk.httpserver;

    // public API surface area
    exports io.clientcore.core.annotations;
    exports io.clientcore.core.credentials;
    exports io.clientcore.core.credentials.oauth;
    exports io.clientcore.core.http.annotations;
    exports io.clientcore.core.http.client;
    exports io.clientcore.core.http.models;
    exports io.clientcore.core.http.paging;
    exports io.clientcore.core.http.pipeline;
    exports io.clientcore.core.instrumentation;
    exports io.clientcore.core.instrumentation.logging;
    exports io.clientcore.core.instrumentation.metrics;
    exports io.clientcore.core.instrumentation.tracing;
    exports io.clientcore.core.models;
    exports io.clientcore.core.models.binarydata;
    exports io.clientcore.core.models.geo;
    exports io.clientcore.core.serialization;
    exports io.clientcore.core.serialization.json;
    exports io.clientcore.core.serialization.json.models;
    exports io.clientcore.core.serialization.xml;
    exports io.clientcore.core.traits;
    exports io.clientcore.core.utils;
    exports io.clientcore.core.utils.configuration;
    exports io.clientcore.core.implementation.http;
    exports io.clientcore.core.implementation.utils;

    uses io.clientcore.core.http.client.HttpClientProvider;
}
