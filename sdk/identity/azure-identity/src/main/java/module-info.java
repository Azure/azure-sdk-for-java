// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.identity {
    requires transitive com.azure.core;

    requires com.microsoft.aad.msal4j;
    requires msal4j.persistence.extension;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires org.reactivestreams;
    requires java.xml;

    exports com.azure.identity;
    exports com.azure.identity.implementation to com.azure.identity.broker;
    opens com.azure.identity to com.azure.identity.broker;
    opens com.azure.identity.implementation to com.azure.identity.broker, com.azure.core;
}
