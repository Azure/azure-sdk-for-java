// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.maps.route {
    requires transitive com.azure.core;
    requires transitive com.azure.core.serializer.json.jackson;

    exports com.azure.maps.route;
    exports com.azure.maps.route.models;

    opens com.azure.maps.route.implementation to com.fasterxml.jackson.databind;
    opens com.azure.maps.route.models to com.fasterxml.jackson.databind;
    opens com.azure.maps.route.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
}
