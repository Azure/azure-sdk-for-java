// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.eventhubs {
    requires transitive com.azure.resourcemanager.resources;
    requires com.azure.resourcemanager.storage;

    // export public APIs of eventhubs
    exports com.azure.resourcemanager.eventhubs;
    exports com.azure.resourcemanager.eventhubs.fluent;
    exports com.azure.resourcemanager.eventhubs.fluent.models;
    exports com.azure.resourcemanager.eventhubs.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.eventhubs.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.eventhubs.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
