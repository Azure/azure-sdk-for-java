// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.monitor {
    requires transitive com.azure.resourcemanager.resources;

    // export public APIs of monitor
    exports com.azure.resourcemanager.monitor;
    exports com.azure.resourcemanager.monitor.fluent;
    exports com.azure.resourcemanager.monitor.fluent.models;
    exports com.azure.resourcemanager.monitor.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.monitor.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.monitor.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
