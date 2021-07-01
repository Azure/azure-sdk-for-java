// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.iot.deviceupdate {
    requires transitive com.azure.core;

    exports com.azure.iot.deviceupdate;
    exports com.azure.iot.deviceupdate.models;
    
    opens com.azure.iot.deviceupdate.implementation to com.fasterxml.jackson.databind;
    opens com.azure.iot.deviceupdate.models to com.fasterxml.jackson.databind;
}