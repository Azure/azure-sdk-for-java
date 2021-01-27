// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.administration {

    requires transitive com.azure.communication.common;
    requires transitive com.azure.core;

    // public API surface area
    exports com.azure.communication.administration;
    exports com.azure.communication.administration.models;

    opens com.azure.communication.administration.models
        to com.fasterxml.jackson.databind;
}
