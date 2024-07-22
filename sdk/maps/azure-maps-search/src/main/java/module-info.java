// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.maps.search {
    requires transitive com.azure.core;

    exports com.azure.maps.search;
    exports com.azure.maps.search.models;

    opens com.azure.maps.search.implementation to com.azure.core;
    opens com.azure.maps.search.models to com.azure.core;
    opens com.azure.maps.search.implementation.models to com.azure.core;
}
