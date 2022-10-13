// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.maps.elevation {
    requires transitive com.azure.core;

    exports com.azure.maps.elevation;
    exports com.azure.maps.elevation.models;

    opens com.azure.maps.elevation.implementation to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.maps.elevation.models to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.maps.elevation.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
}