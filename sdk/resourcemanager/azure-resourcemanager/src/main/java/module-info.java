// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager {
    requires transitive com.azure.resourcemanager.resources;
    requires transitive com.azure.resourcemanager.appplatform;
    requires transitive com.azure.resourcemanager.appservice;
    requires transitive com.azure.resourcemanager.cdn;
    requires transitive com.azure.resourcemanager.compute;
    requires transitive com.azure.resourcemanager.containerinstance;
    requires transitive com.azure.resourcemanager.containerregistry;
    requires transitive com.azure.resourcemanager.containerservice;
    requires transitive com.azure.resourcemanager.cosmos;
    requires transitive com.azure.resourcemanager.eventhubs;
    requires transitive com.azure.resourcemanager.monitor;
    requires transitive com.azure.resourcemanager.privatedns;
    requires transitive com.azure.resourcemanager.redis;
    requires transitive com.azure.resourcemanager.search;
    requires transitive com.azure.resourcemanager.servicebus;
    requires transitive com.azure.resourcemanager.sql;
    requires transitive com.azure.resourcemanager.trafficmanager;

    exports com.azure.resourcemanager;
}
