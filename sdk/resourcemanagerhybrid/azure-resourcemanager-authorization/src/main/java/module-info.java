// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.authorization {
    requires transitive com.azure.resourcemanager.resources;

    // export public APIs of authorization
    exports com.azure.resourcemanager.authorization;
    exports com.azure.resourcemanager.authorization.fluent;
    exports com.azure.resourcemanager.authorization.fluent.models;
    exports com.azure.resourcemanager.authorization.models;
    exports com.azure.resourcemanager.authorization.utils;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.authorization.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.authorization.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
