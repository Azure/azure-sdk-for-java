// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.rooms {
    requires transitive com.azure.core;
    requires transitive com.azure.communication.common;

    // public API surface area
    exports com.azure.communication.rooms;
    exports com.azure.communication.rooms.models;
    exports com.azure.communication.rooms.implementation.models;

    opens com.azure.communication.rooms
        to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.communication.rooms.models
        to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.communication.rooms.implementation.models
        to com.fasterxml.jackson.databind, com.azure.core;
}

