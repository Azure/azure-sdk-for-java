// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.maps.traffic {
    requires transitive com.azure.core;

    exports com.azure.maps.traffic;
    exports com.azure.maps.traffic.models;

    opens com.azure.maps.traffic.implementation to com.azure.core;
    opens com.azure.maps.traffic.models to com.azure.core;
    opens com.azure.maps.traffic.implementation.models to com.azure.core;
}
