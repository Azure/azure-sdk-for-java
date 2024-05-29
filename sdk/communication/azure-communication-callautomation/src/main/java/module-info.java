// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.callautomation {

    requires transitive com.azure.communication.common;

    // public API surface area
    exports com.azure.communication.callautomation;
    exports com.azure.communication.callautomation.models;
    exports com.azure.communication.callautomation.models.events;

    // exporting some packages specifically for Jackson
    opens com.azure.communication.callautomation.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.communication.callautomation to com.fasterxml.jackson.databind;
    opens com.azure.communication.callautomation.models to com.azure.core, com.fasterxml.jackson.databind;
    opens com.azure.communication.callautomation.models.events to com.fasterxml.jackson.databind;
    opens com.azure.communication.callautomation.implementation.converters to com.azure.core, com.fasterxml.jackson.databind;
}
