// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.tables {
    requires transitive com.azure.core;
    requires transitive com.azure.storage.common;

    // public API surface area
    exports com.azure.data.tables;
    exports com.azure.data.tables.models;

    exports com.azure.data.tables.implementation to com.azure.core;
    exports com.azure.data.tables.implementation.models to com.azure.core;

    // exporting some packages specifically for Jackson
    opens com.azure.data.tables to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.data.tables.implementation to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.data.tables.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.data.tables.models to com.fasterxml.jackson.databind, com.azure.core;
}
