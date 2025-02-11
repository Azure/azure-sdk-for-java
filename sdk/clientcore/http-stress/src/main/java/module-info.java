// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * Contains classes for stress tests.
 */
module io.clientcore.http.stress {
    requires transitive com.azure.core.test.perf;
    requires transitive io.clientcore.core;
    requires transitive io.clientcore.http.okhttp3;

    requires com.azure.monitor.opentelemetry.autoconfigure;
    requires com.azure.core;
    requires jcommander;
    requires io.opentelemetry.api;
    requires io.opentelemetry.context;
    requires io.opentelemetry.instrumentation.logback_appender_1_0;
    requires io.opentelemetry.instrumentation.runtime_telemetry_java8;
    requires io.opentelemetry.sdk;
    requires io.opentelemetry.sdk.autoconfigure;
    requires io.opentelemetry.sdk.autoconfigure.spi;
    requires io.opentelemetry.sdk.trace;

    exports io.clientcore.http.stress;
    exports io.clientcore.http.stress.util;
}
