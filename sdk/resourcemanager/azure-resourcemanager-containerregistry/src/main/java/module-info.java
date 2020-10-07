// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.containerregistry {
    requires transitive com.azure.resourcemanager.resources;
    requires transitive com.azure.resourcemanager.storage;

    // export public APIs of containerregistry
    exports com.azure.resourcemanager.containerregistry;
    exports com.azure.resourcemanager.containerregistry.fluent;
    exports com.azure.resourcemanager.containerregistry.fluent.models;
    exports com.azure.resourcemanager.containerregistry.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.containerregistry.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.containerregistry.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
