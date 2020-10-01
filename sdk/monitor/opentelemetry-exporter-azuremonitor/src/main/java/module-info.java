// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.opentelemetry.exporter.azuremonitor {
    requires transitive com.azure.core;

    exports com.opentelemetry.exporter.azuremonitor;
    exports com.opentelemetry.exporter.azuremonitor.models;
}
