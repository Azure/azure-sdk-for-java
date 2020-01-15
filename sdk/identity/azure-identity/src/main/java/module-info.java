// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.identity {
    requires transitive com.azure.core;

    requires msal4j;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires nanohttpd;
    requires org.reactivestreams;

    exports com.azure.identity;

    opens com.azure.identity.implementation to com.fasterxml.jackson.databind;
}
