// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.tables {
    requires transitive com.azure.core;

    // public API surface area
    exports com.azure.data.tables;
    exports com.azure.data.tables.models;
    exports com.azure.data.tables.sas;

    opens com.azure.data.tables to com.azure.core;
    opens com.azure.data.tables.implementation to com.azure.core;
    opens com.azure.data.tables.implementation.models to com.azure.core;
    opens com.azure.data.tables.models to com.azure.core;
    opens com.azure.data.tables.sas to com.azure.core;
}
