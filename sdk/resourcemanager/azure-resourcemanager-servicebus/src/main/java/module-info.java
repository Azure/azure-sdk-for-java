// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.servicebus {
    requires transitive com.azure.resourcemanager.resources;

    // export public APIs of servicebus
    exports com.azure.resourcemanager.servicebus;
    exports com.azure.resourcemanager.servicebus.fluent;
    exports com.azure.resourcemanager.servicebus.fluent.models;
    exports com.azure.resourcemanager.servicebus.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.servicebus.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.servicebus.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.servicebus.implementation to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
