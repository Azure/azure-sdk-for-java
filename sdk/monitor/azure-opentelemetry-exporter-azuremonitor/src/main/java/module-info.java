// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.opentelemetry.exporter.azuremonitor {

    requires transitive com.azure.core;

    requires transitive io.opentelemetry.sdk;
    requires transitive io.opentelemetry.sdk.trace;
    requires transitive io.opentelemetry.sdk.common;
    requires transitive io.opentelemetry.api;

    requires io.opentelemetry.api.trace;
    requires io.opentelemetry.api.common;

    exports com.azure.opentelemetry.exporter.azuremonitor;

    opens com.azure.opentelemetry.exporter.azuremonitor.implementation.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
