// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.identity {
    // FIXME this is unfortunate - java.desktop is used to open the users browser
    requires java.desktop;

    requires transitive com.azure.core;

    // FIXME this is temporary - reactor.netty is only required until this branch syncs with the repo
    requires reactor.netty;

    requires msal4j;

    exports com.azure.identity;
    exports com.azure.identity.credential;
}
