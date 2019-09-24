// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.identity {
    // FIXME this is unfortunate - java.desktop is used to open the users browser
    requires java.desktop;

    requires transitive com.azure.core;

    requires msal4j;
    requires jna;
    requires jna.platform;
    requires nanohttpd;

    exports com.azure.identity;
    exports com.azure.identity.credential;
}
