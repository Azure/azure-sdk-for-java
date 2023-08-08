// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.maps.traffic {
    requires transitive com.azure.core;

    exports com.azure.maps.traffic;
    exports com.azure.maps.traffic.models;

    opens com.azure.maps.traffic.implementation to com.fasterxml.jackson.databind;
    opens com.azure.maps.traffic.models to com.fasterxml.jackson.databind;
    opens com.azure.maps.traffic.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
}