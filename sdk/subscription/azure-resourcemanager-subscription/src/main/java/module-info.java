// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

module com.azure.resourcemanager.subscription {
    requires transitive com.azure.core.management;

    exports com.azure.resourcemanager.subscription;
    exports com.azure.resourcemanager.subscription.fluent;
    exports com.azure.resourcemanager.subscription.fluent.models;
    exports com.azure.resourcemanager.subscription.models;

    opens com.azure.resourcemanager.subscription.fluent.models to com.azure.core;
    opens com.azure.resourcemanager.subscription.models to com.azure.core;
}
