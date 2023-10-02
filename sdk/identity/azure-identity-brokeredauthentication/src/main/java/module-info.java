// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.identity.brokeredauthentication {
    requires transitive com.azure.core;

    requires com.microsoft.aad.msal4j;
    requires msal4j.brokers;

    exports com.azure.identity.brokeredauthentication;

}
