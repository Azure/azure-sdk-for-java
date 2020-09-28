// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.cosmos {
    requires transitive com.azure.resourcemanager.resources;

    // export public APIs of cosmos
    exports com.azure.resourcemanager.cosmos;
    exports com.azure.resourcemanager.cosmos.fluent;
    exports com.azure.resourcemanager.cosmos.fluent.models;
    exports com.azure.resourcemanager.cosmos.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.cosmos.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.cosmos.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
