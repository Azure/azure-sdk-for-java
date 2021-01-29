// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.mixedreality.remoterendering {
    requires com.azure.core;
    requires azure.mixedreality.authentication;
    opens com.azure.mixedreality.remoterendering.implementation.models;
    exports com.azure.mixedreality.remoterendering;
    exports com.azure.mixedreality.remoterendering.models;
}
