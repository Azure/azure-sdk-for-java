// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.sql {
    requires transitive com.azure.resourcemanager.resources;
    requires com.azure.resourcemanager.storage;

    // export public APIs of sql
    exports com.azure.resourcemanager.sql;
    exports com.azure.resourcemanager.sql.fluent;
    exports com.azure.resourcemanager.sql.fluent.models;
    exports com.azure.resourcemanager.sql.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.sql.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.sql.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
