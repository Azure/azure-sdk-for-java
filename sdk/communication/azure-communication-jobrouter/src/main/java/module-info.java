// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.jobrouter {

    requires transitive com.azure.communication.common;

    // public API surface area
    exports com.azure.communication.jobrouter;
    exports com.azure.communication.jobrouter.models;

    opens com.azure.communication.jobrouter
        to com.fasterxml.jackson.databind;
    opens com.azure.communication.jobrouter.implementation.models
        to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.communication.jobrouter.models
        to com.azure.core, com.fasterxml.jackson.databind;
    opens com.azure.communication.jobrouter.implementation to com.azure.core, com.fasterxml.jackson.databind;
}
