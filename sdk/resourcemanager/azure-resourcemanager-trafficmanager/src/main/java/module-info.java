// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.trafficmanager {
    requires transitive com.azure.resourcemanager.resources;

    // export public APIs of trafficmanager
    exports com.azure.resourcemanager.trafficmanager;
    exports com.azure.resourcemanager.trafficmanager.fluent;
    exports com.azure.resourcemanager.trafficmanager.fluent.models;
    exports com.azure.resourcemanager.trafficmanager.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.trafficmanager.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.trafficmanager.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
