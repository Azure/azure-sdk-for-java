// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.privatedns {
    requires transitive com.azure.resourcemanager.resources;

    // export public APIs of privatedns
    exports com.azure.resourcemanager.privatedns;
    exports com.azure.resourcemanager.privatedns.fluent;
    exports com.azure.resourcemanager.privatedns.fluent.models;
    exports com.azure.resourcemanager.privatedns.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.privatedns.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.privatedns.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
