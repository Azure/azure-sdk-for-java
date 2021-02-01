// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.common {

    requires transitive com.azure.core;

    // public API surface area
    exports com.azure.communication.common;

    exports com.azure.communication.common.implementation to
        com.azure.communication.administration,
        com.azure.communication.sms,
        com.azure.communication.identity,
        com.azure.communication.chat;

    opens com.azure.communication.common.implementation;
}
