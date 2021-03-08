// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.search {
    requires transitive com.azure.resourcemanager.resources;

    // export public APIs of search
    exports com.azure.resourcemanager.search;
    exports com.azure.resourcemanager.search.fluent;
    exports com.azure.resourcemanager.search.fluent.models;
    exports com.azure.resourcemanager.search.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.search.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.search.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
