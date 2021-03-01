// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.mixedreality.remoterendering {
    requires transitive com.azure.core;
    requires com.azure.mixedreality.authentication;

    opens com.azure.mixedreality.remoterendering.implementation.models to com.fasterxml.jackson.databind, com.azure.core;

    exports com.azure.mixedreality.remoterendering;
    exports com.azure.mixedreality.remoterendering.models;
}
