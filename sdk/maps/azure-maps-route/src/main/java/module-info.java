// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.maps.route {
    requires transitive com.azure.core;

    exports com.azure.maps.route;
    exports com.azure.maps.route.models;

    opens com.azure.maps.route.implementation to com.azure.core;
    opens com.azure.maps.route.models to com.azure.core;
    opens com.azure.maps.route.implementation.models to com.azure.core;
}
