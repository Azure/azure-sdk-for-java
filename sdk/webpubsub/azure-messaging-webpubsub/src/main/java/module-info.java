// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.messaging.webpubsub {
    requires transitive com.azure.core;

    requires com.nimbusds.jose.jwt;

    exports com.azure.messaging.webpubsub;
    exports com.azure.messaging.webpubsub.models;
}
