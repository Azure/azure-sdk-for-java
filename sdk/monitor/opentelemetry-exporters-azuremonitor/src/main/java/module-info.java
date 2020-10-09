// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.opentelemetry.exporters.azuremonitor {
    requires transitive com.azure.core;
    requires transitive io.opentelemetry.sdk;
    requires transitive io.opentelemetry.sdk.tracing;
    requires transitive io.opentelemetry.sdk.common;
    requires transitive io.opentelemetry.api;

    exports com.azure.opentelemetry.exporters.azuremonitor;

    opens com.azure.opentelemetry.exporters.azuremonitor.implementation.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
