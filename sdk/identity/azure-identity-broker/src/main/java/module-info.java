// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.identity.broker {
    requires transitive com.azure.core;
    requires com.azure.identity;
    requires com.microsoft.aad.msal4j;
    requires msal4j.brokers;

//    exports com.azure.identity.brokeredauthentication;
    exports com.azure.identity.broker.implementation;

}
