// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.messaging.signalr {
    requires transitive com.azure.core;

    // FIXME nimbus would be preferred, but it was causing problems with key length
//    requires nimbus.jose.jwt;
    requires jjwt.api;

    exports com.azure.messaging.signalr;
}
