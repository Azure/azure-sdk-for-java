// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.v2.identity {
    requires transitive com.azure.v2.core;

    requires com.microsoft.aad.msal4j;
    requires msal4j.persistence.extension;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires java.xml;

    exports com.azure.v2.identity;
    exports com.azure.v2.identity.exceptions;
    exports com.azure.v2.identity.models;
}
