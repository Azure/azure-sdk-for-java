// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.monitor.ingestion {
    requires transitive com.azure.core;

    exports com.azure.monitor.ingestion;
    exports com.azure.monitor.ingestion.models;

    opens com.azure.monitor.ingestion to com.azure.core, com.fasterxml.jackson.databind;
}
