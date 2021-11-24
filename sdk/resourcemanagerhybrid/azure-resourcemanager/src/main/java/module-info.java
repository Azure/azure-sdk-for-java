// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager {
    requires transitive com.azure.resourcemanager.resources;
    requires transitive com.azure.resourcemanager.appservice;
    requires transitive com.azure.resourcemanager.compute;
    requires transitive com.azure.resourcemanager.containerregistry;
    requires transitive com.azure.resourcemanager.containerservice;
    requires transitive com.azure.resourcemanager.eventhubs;
    requires transitive com.azure.resourcemanager.monitor;

    exports com.azure.resourcemanager;
}
