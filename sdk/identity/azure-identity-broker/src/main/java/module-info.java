// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.identity.broker {
    requires transitive com.azure.identity;
    requires msal4j.brokers;
    requires com.microsoft.aad.msal4j;

    opens com.azure.identity.broker.implementation to com.azure.identity;

    exports com.azure.identity.broker;
}
