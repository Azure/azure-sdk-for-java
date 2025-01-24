// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * This module provides core functionality for the Java SDK.
 */
module io.clientcore.core {
    requires transitive java.xml;

    requires java.net.http;

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
    exports io.clientcore.core.serialization.json;
    exports io.clientcore.core.serialization.xml;
    exports io.clientcore.core.util;
    exports io.clientcore.core.util.binarydata;
    exports io.clientcore.core.util.configuration;
    exports io.clientcore.core.util.serializer;
    exports io.clientcore.core.util.auth;
    exports io.clientcore.core.instrumentation;
    exports io.clientcore.core.instrumentation.tracing;
    exports io.clientcore.core.instrumentation.logging;

    uses io.clientcore.core.http.client.HttpClientProvider;
    uses io.clientcore.core.serialization.json.JsonProvider;
}
