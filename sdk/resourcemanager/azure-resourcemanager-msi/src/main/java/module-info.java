// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.msi {
    requires transitive com.azure.resourcemanager.resources;
    requires transitive com.azure.resourcemanager.authorization;

    // export public APIs of storage
    exports com.azure.resourcemanager.msi;
    exports com.azure.resourcemanager.msi.fluent;
    exports com.azure.resourcemanager.msi.fluent.models;
    exports com.azure.resourcemanager.msi.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.msi.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.msi.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
