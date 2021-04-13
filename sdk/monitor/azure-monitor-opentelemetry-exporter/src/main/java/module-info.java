// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.monitor.opentelemetry.exporter {

    requires transitive com.azure.core;

    requires transitive io.opentelemetry.sdk;
    requires transitive io.opentelemetry.sdk.trace;
    requires transitive io.opentelemetry.sdk.common;
    requires transitive io.opentelemetry.api;

    exports com.azure.monitor.opentelemetry.exporter;

    opens com.azure.monitor.opentelemetry.exporter.implementation.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
