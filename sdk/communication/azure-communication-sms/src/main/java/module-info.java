// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.sms {

    requires transitive com.azure.communication.common;

    // public API surface area
    exports com.azure.communication.sms;
    exports com.azure.communication.sms.models;

    opens com.azure.communication.sms.models to com.azure.core;
    opens com.azure.communication.sms.implementation.models to com.azure.core;
}
