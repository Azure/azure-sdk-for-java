// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.compute {
    requires transitive com.azure.resourcemanager.resources;
    requires transitive com.azure.resourcemanager.msi;
    requires transitive com.azure.resourcemanager.network;
    requires transitive com.azure.resourcemanager.storage;

    // export public APIs of compute
    exports com.azure.resourcemanager.compute;
    exports com.azure.resourcemanager.compute.fluent;
    exports com.azure.resourcemanager.compute.fluent.models;
    exports com.azure.resourcemanager.compute.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.compute.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.compute.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
