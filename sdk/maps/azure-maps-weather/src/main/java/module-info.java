// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.maps.weather {
    requires transitive com.azure.core;

    exports com.azure.maps.weather;
    exports com.azure.maps.weather.models;

    opens com.azure.maps.weather.implementation to com.azure.core;
    opens com.azure.maps.weather.models to com.azure.core;
    opens com.azure.maps.weather.implementation.models to com.azure.core;
}
