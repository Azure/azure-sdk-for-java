// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
module com.azure.maps.render {
    requires transitive com.azure.core;

    exports com.azure.maps.render;
    exports com.azure.maps.render.models;

    opens com.azure.maps.render.implementation to com.azure.core;
    opens com.azure.maps.render.models to com.azure.core;
    opens com.azure.maps.render.implementation.models to com.azure.core;
}
