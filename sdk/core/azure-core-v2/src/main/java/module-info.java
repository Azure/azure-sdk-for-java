// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core {
    requires io.clientcore.core;

    // public API surface area
    exports com.azure.core.v2.annotation;
    exports com.azure.core.v2.client.traits;
    exports com.azure.core.v2.credential;
    exports com.azure.core.v2.cryptography;
    exports com.azure.core.v2.exception;
    exports com.azure.core.v2.http;
    exports com.azure.core.v2.http.policy;
    exports com.azure.core.v2.http.rest;
    exports com.azure.core.v2.util;
    exports com.azure.core.v2.util.io;
    exports com.azure.core.v2.util.paging;
    exports com.azure.core.v2.util.serializer;
    exports com.azure.core.v2.util.tracing;
    exports com.azure.core.v2.util.metrics;

    // Service Provider Interfaces
    uses com.azure.core.v2.util.serializer.JsonSerializerProvider;
    uses com.azure.core.v2.util.serializer.MemberNameConverterProvider;
    uses com.azure.core.v2.util.tracing.Tracer;
    uses com.azure.core.v2.util.metrics.MeterProvider;
    uses com.azure.core.v2.util.tracing.TracerProvider;
}
