// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.identity {
    requires transitive com.azure.core;

    requires com.microsoft.aad.msal4j;
    requires msal4j.persistence.extension;
    requires java.xml;

    exports com.azure.identity.v2;
}
