// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.appconfiguration {
    requires transitive com.azure.core;

    opens com.azure.data.appconfiguration.implementation to com.fasterxml.jackson.databind;
    opens com.azure.data.appconfiguration.models to com.fasterxml.jackson.databind;

    exports com.azure.data.appconfiguration;
    exports com.azure.data.appconfiguration.models;
}
