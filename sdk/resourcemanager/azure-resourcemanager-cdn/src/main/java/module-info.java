// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.cdn {
    requires transitive com.azure.resourcemanager.resources;

    // export public APIs of cdn
    exports com.azure.resourcemanager.cdn;
    exports com.azure.resourcemanager.cdn.fluent;
    exports com.azure.resourcemanager.cdn.fluent.models;
    exports com.azure.resourcemanager.cdn.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.cdn.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.cdn.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
