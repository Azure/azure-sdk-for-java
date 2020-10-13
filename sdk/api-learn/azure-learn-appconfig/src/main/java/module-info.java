// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.learn.appconfig {
    requires transitive com.azure.core;
    exports com.azure.learn.appconfig.models;
    exports com.azure.learn.appconfig.implementation.models to com.fasterxml.jackson.databind;
    opens com.azure.learn.appconfig.implementation.models to com.azure.core, com.fasterxml.jackson.databind;
    opens com.azure.learn.appconfig.models to com.fasterxml.jackson.databind;
}
