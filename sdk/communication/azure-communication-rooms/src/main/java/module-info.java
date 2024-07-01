// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.rooms {
    requires com.azure.json;
    requires transitive com.azure.communication.common;

    // public API surface area
    exports com.azure.communication.rooms;
    exports com.azure.communication.rooms.models;

    opens com.azure.communication.rooms
        to com.azure.core;
    opens com.azure.communication.rooms.models
        to com.azure.core;
    opens com.azure.communication.rooms.implementation.models
        to com.azure.core;
}

