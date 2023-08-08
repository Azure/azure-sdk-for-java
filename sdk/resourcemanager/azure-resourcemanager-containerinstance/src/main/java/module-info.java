// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.containerinstance {
    requires transitive com.azure.resourcemanager.resources;
    requires transitive com.azure.resourcemanager.msi;
    requires com.azure.resourcemanager.storage;
    requires com.azure.resourcemanager.network;
    requires com.azure.storage.file.share;

    // export public APIs of containerinstance
    exports com.azure.resourcemanager.containerinstance;
    exports com.azure.resourcemanager.containerinstance.fluent;
    exports com.azure.resourcemanager.containerinstance.fluent.models;
    exports com.azure.resourcemanager.containerinstance.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.containerinstance.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.containerinstance.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
