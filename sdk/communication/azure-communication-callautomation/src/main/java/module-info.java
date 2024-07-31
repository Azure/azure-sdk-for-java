// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.communication.callautomation {

    requires transitive com.azure.communication.common;

    // public API surface area
    exports com.azure.communication.callautomation;
    exports com.azure.communication.callautomation.models;
    exports com.azure.communication.callautomation.models.events;
    exports com.azure.communication.callautomation.implementation.eventprocessor;

    // exporting some packages specifically for azure-core
    opens com.azure.communication.callautomation.implementation.models to com.azure.core;
    opens com.azure.communication.callautomation to com.azure.core;
    opens com.azure.communication.callautomation.models to com.azure.core;
    opens com.azure.communication.callautomation.models.events to com.azure.core;
    opens com.azure.communication.callautomation.implementation.converters to com.azure.core;
    
}
