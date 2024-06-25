// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.phonenumbers {

    requires transitive com.azure.communication.common;

    // public API surface area
    exports com.azure.communication.phonenumbers;
    exports com.azure.communication.phonenumbers.models;
    exports com.azure.communication.phonenumbers.siprouting;
    exports com.azure.communication.phonenumbers.siprouting.models;

    opens com.azure.communication.phonenumbers.implementation.models
        to com.azure.core;
    opens com.azure.communication.phonenumbers.models
        to com.azure.core;

    opens com.azure.communication.phonenumbers.siprouting.implementation.models
        to com.azure.core;
    opens com.azure.communication.phonenumbers.siprouting.models
        to com.azure.core;

}
