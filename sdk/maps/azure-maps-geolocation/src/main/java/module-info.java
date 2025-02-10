// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.maps.geolocation {
    requires transitive com.azure.core;

    exports com.azure.maps.geolocation;
    exports com.azure.maps.geolocation.models;

    opens com.azure.maps.geolocation.implementation to com.azure.core;
    opens com.azure.maps.geolocation.models to com.azure.core;
    opens com.azure.maps.geolocation.implementation.models to com.azure.core;
}
