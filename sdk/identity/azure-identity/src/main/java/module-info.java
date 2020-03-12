// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.identity {
    // FIXME this is unfortunate - java.desktop is used to open the users browser
    requires java.desktop;

    requires transitive com.azure.core;

    requires msal4j;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires nanohttpd;
    requires org.reactivestreams;

    exports com.azure.identity;
}
