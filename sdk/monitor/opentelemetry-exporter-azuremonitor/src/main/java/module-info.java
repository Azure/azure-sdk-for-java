// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.opentelemetry.exporter.azuremonitor {
    requires transitive com.azure.core;

    exports com.azure.opentelemetry.exporter.azuremonitor;
    exports com.azure.opentelemetry.exporter.azuremonitor.models;

    opens com.azure.opentelemetry.exporter.azuremonitor.models to com.fasterxml.jackson.databind;

}
