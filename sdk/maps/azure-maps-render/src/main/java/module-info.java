// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
module com.azure.maps.render {
    requires transitive com.azure.core;
    requires transitive com.azure.core.serializer.json.jackson;

    exports com.azure.maps.render;
    exports com.azure.maps.render.models;

    opens com.azure.maps.render.implementation to com.fasterxml.jackson.databind;
    opens com.azure.maps.render.models to com.fasterxml.jackson.databind;
    opens com.azure.maps.render.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
}