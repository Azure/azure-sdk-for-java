// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.identity {
    requires com.azure.json;
    requires transitive com.azure.communication.common;

    // public API surface area
    exports com.azure.communication.identity;
    exports com.azure.communication.identity.models;

    opens com.azure.communication.identity.models
        to com.azure.core;
    opens com.azure.communication.identity.implementation.models
        to com.azure.core;
}
