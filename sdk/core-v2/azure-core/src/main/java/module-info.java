// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * This module contains the core classes for Azure SDK.
 */
module com.azure.v2.core {
    requires transitive io.clientcore.core;

    requires com.microsoft.aad.msal4j;
    requires msal4j.persistence.extension;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires java.xml;

    // public API surface area
    exports com.azure.v2.core.credentials;
    exports com.azure.v2.core.http.pipeline;
    exports com.azure.v2.core.http.polling;
    exports com.azure.v2.core.identity;
    exports com.azure.v2.core.identity.models;
    exports com.azure.v2.core.models;
    exports com.azure.v2.core.traits;
}
