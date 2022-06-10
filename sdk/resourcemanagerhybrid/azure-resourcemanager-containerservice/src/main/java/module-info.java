// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.containerservice {
    requires transitive com.azure.resourcemanager.resources;

    // export public APIs of containerservice
    exports com.azure.resourcemanager.containerservice;
    exports com.azure.resourcemanager.containerservice.fluent;
    exports com.azure.resourcemanager.containerservice.fluent.models;
    exports com.azure.resourcemanager.containerservice.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.containerservice.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.containerservice.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
