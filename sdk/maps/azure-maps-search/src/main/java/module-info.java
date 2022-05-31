// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.maps.search {
    requires transitive com.azure.core;
    requires transitive com.azure.core.serializer.json.jackson;

    exports com.azure.maps.search;
    exports com.azure.maps.search.models;

    opens com.azure.maps.search.implementation to com.fasterxml.jackson.databind;
    opens com.azure.maps.search.models to com.fasterxml.jackson.databind;
    opens com.azure.maps.search.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
}