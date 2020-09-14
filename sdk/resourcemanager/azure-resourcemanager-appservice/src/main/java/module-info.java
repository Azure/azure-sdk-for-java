// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.appservice {
    requires transitive com.azure.resourcemanager.resources;
    requires com.azure.resourcemanager.authorization;
    requires com.azure.resourcemanager.dns;
    requires com.azure.resourcemanager.keyvault;
    requires com.azure.resourcemanager.msi;
    requires com.azure.resourcemanager.storage;

    // export public APIs of appservice
    exports com.azure.resourcemanager.appservice;
    exports com.azure.resourcemanager.appservice.fluent;
    exports com.azure.resourcemanager.appservice.fluent.inner;
    exports com.azure.resourcemanager.appservice.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.appservice.fluent.inner to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.appservice.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
