// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.maps.timezone {
    requires transitive com.azure.core;

    exports com.azure.maps.timezone;
    exports com.azure.maps.timezone.models;

    opens com.azure.maps.timezone.implementation to com.fasterxml.jackson.databind;
    opens com.azure.maps.timezone.models to com.fasterxml.jackson.databind;
    opens com.azure.maps.timezone.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
}