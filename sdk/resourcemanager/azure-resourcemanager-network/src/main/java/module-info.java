// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.network {
    requires transitive com.azure.resourcemanager.resources;

    // export public APIs of network
    exports com.azure.resourcemanager.network;
    exports com.azure.resourcemanager.network.fluent;
    exports com.azure.resourcemanager.network.fluent.models;
    exports com.azure.resourcemanager.network.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.network.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.network.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
