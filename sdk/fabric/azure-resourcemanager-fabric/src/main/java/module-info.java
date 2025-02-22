// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

module com.azure.resourcemanager.fabric {
    requires transitive com.azure.core.management;

    exports com.azure.resourcemanager.fabric;
    exports com.azure.resourcemanager.fabric.fluent;
    exports com.azure.resourcemanager.fabric.fluent.models;
    exports com.azure.resourcemanager.fabric.models;

    opens com.azure.resourcemanager.fabric.fluent.models to com.azure.core;
    opens com.azure.resourcemanager.fabric.models to com.azure.core;
    opens com.azure.resourcemanager.fabric.implementation.models to com.azure.core;
}
