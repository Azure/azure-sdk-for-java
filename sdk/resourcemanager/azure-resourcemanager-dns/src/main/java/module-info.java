// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.dns {
    requires transitive com.azure.resourcemanager.resources;

    // export public APIs of dns
    exports com.azure.resourcemanager.dns;
    exports com.azure.resourcemanager.dns.fluent;
    exports com.azure.resourcemanager.dns.fluent.models;
    exports com.azure.resourcemanager.dns.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.dns.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.dns.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
