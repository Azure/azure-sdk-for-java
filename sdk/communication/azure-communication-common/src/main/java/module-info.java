// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.common {

    requires transitive com.azure.core;

    // public API surface area
    exports com.azure.communication.common;
    opens com.azure.communication.common to
        com.azure.core;

    exports com.azure.communication.common.implementation to
        com.azure.communication.email,
        com.azure.communication.sms,
        com.azure.communication.identity,
        com.azure.communication.phonenumbers,
        com.azure.communication.chat,
        com.azure.communication.rooms,
        com.azure.communication.callingserver,
        com.azure.communication.callautomation,
        com.azure.communication.jobrouter,
        com.azure.communication.messages;
}
