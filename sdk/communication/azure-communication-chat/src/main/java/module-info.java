// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.chat {

    requires transitive com.azure.communication.common;

    // public API surface area
    exports com.azure.communication.chat;
    exports com.azure.communication.chat.models;

    opens com.azure.communication.chat
        to com.fasterxml.jackson.databind;
    opens com.azure.communication.chat.models
        to com.fasterxml.jackson.databind;
    opens com.azure.communication.chat.implementation.models
        to com.fasterxml.jackson.databind, com.azure.core;
}
