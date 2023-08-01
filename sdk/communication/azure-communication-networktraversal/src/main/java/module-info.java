// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.networktraversal {

    requires transitive com.azure.communication.common;

    // public API surface area
    exports com.azure.communication.networktraversal;
    exports com.azure.communication.networktraversal.models;

    opens com.azure.communication.networktraversal.models
        to com.fasterxml.jackson.databind;
    opens com.azure.communication.networktraversal.implementation.models
        to com.fasterxml.jackson.databind, com.azure.core;
}
