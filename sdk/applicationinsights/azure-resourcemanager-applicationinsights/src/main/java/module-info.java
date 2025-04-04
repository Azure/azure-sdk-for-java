// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

module com.azure.resourcemanager.applicationinsights {
    requires transitive com.azure.core.management;

    exports com.azure.resourcemanager.applicationinsights;
    exports com.azure.resourcemanager.applicationinsights.fluent;
    exports com.azure.resourcemanager.applicationinsights.fluent.models;
    exports com.azure.resourcemanager.applicationinsights.models;

    opens com.azure.resourcemanager.applicationinsights.fluent.models to com.azure.core;
    opens com.azure.resourcemanager.applicationinsights.models to com.azure.core;
}
