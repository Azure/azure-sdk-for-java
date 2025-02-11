// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * This module contains the core classes for Azure SDK.
 */
module com.azure.v2.core {
    requires transitive io.clientcore.core;

    // public API surface area
    exports com.azure.v2.core.annotation;
    exports com.azure.v2.core.client.traits;
    exports com.azure.v2.core.credential;
    exports com.azure.v2.core.cryptography;
    exports com.azure.v2.core.exception;
    exports com.azure.v2.core.http;
    exports com.azure.v2.core.http.policy;
    exports com.azure.v2.core.http.rest;
    exports com.azure.v2.core.util;
    exports com.azure.v2.core.util.io;
    exports com.azure.v2.core.util.paging;
    exports com.azure.v2.core.util.serializer;
    exports com.azure.v2.core.util.tracing;
    exports com.azure.v2.core.util.metrics;

    // Service Provider Interfaces
    uses com.azure.v2.core.util.metrics.MeterProvider;
    uses com.azure.v2.core.util.tracing.Tracer;
    uses com.azure.v2.core.util.tracing.TracerProvider;
}
