// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.resources {
    requires transitive com.azure.core.management;

    // export public APIs of resources
    exports com.azure.resourcemanager.resources;
    exports com.azure.resourcemanager.resources.fluent;
    exports com.azure.resourcemanager.resources.fluent.models;
    exports com.azure.resourcemanager.resources.models;

    // export public APIs used commonly across multiple services
    exports com.azure.resourcemanager.resources.fluentcore;
    exports com.azure.resourcemanager.resources.fluentcore.arm;
    exports com.azure.resourcemanager.resources.fluentcore.arm.collection;
    exports com.azure.resourcemanager.resources.fluentcore.arm.models;
    exports com.azure.resourcemanager.resources.fluentcore.collection;
    exports com.azure.resourcemanager.resources.fluentcore.dag;
    exports com.azure.resourcemanager.resources.fluentcore.exception;
    exports com.azure.resourcemanager.resources.fluentcore.model;
    exports com.azure.resourcemanager.resources.fluentcore.policy;
    exports com.azure.resourcemanager.resources.fluentcore.rest;
    exports com.azure.resourcemanager.resources.fluentcore.utils;

    // export internal APIs only required for service implementation
    exports com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation to
        com.azure.resourcemanager.appplatform,
        com.azure.resourcemanager.appservice,
        com.azure.resourcemanager.authorization,
        com.azure.resourcemanager.cdn,
        com.azure.resourcemanager.compute,
        com.azure.resourcemanager.containerinstance,
        com.azure.resourcemanager.containerregistry,
        com.azure.resourcemanager.containerservice,
        com.azure.resourcemanager.cosmos,
        com.azure.resourcemanager.dns,
        com.azure.resourcemanager.eventhubs,
        com.azure.resourcemanager.keyvault,
        com.azure.resourcemanager.monitor,
        com.azure.resourcemanager.msi,
        com.azure.resourcemanager.network,
        com.azure.resourcemanager.privatedns,
        com.azure.resourcemanager.redis,
        com.azure.resourcemanager.search,
        com.azure.resourcemanager.servicebus,
        com.azure.resourcemanager.sql,
        com.azure.resourcemanager.storage,
        com.azure.resourcemanager.trafficmanager;
    exports com.azure.resourcemanager.resources.fluentcore.arm.implementation to
        com.azure.resourcemanager.appplatform,
        com.azure.resourcemanager.appservice,
        com.azure.resourcemanager.authorization,
        com.azure.resourcemanager.cdn,
        com.azure.resourcemanager.compute,
        com.azure.resourcemanager.containerinstance,
        com.azure.resourcemanager.containerregistry,
        com.azure.resourcemanager.containerservice,
        com.azure.resourcemanager.cosmos,
        com.azure.resourcemanager.dns,
        com.azure.resourcemanager.eventhubs,
        com.azure.resourcemanager.keyvault,
        com.azure.resourcemanager.monitor,
        com.azure.resourcemanager.msi,
        com.azure.resourcemanager.network,
        com.azure.resourcemanager.privatedns,
        com.azure.resourcemanager.redis,
        com.azure.resourcemanager.search,
        com.azure.resourcemanager.servicebus,
        com.azure.resourcemanager.sql,
        com.azure.resourcemanager.storage,
        com.azure.resourcemanager.trafficmanager,
        com.azure.resourcemanager;
    exports com.azure.resourcemanager.resources.fluentcore.arm.models.implementation to
        com.azure.resourcemanager.appplatform,
        com.azure.resourcemanager.appservice,
        com.azure.resourcemanager.authorization,
        com.azure.resourcemanager.cdn,
        com.azure.resourcemanager.compute,
        com.azure.resourcemanager.containerinstance,
        com.azure.resourcemanager.containerregistry,
        com.azure.resourcemanager.containerservice,
        com.azure.resourcemanager.cosmos,
        com.azure.resourcemanager.dns,
        com.azure.resourcemanager.eventhubs,
        com.azure.resourcemanager.keyvault,
        com.azure.resourcemanager.monitor,
        com.azure.resourcemanager.msi,
        com.azure.resourcemanager.network,
        com.azure.resourcemanager.privatedns,
        com.azure.resourcemanager.redis,
        com.azure.resourcemanager.search,
        com.azure.resourcemanager.servicebus,
        com.azure.resourcemanager.sql,
        com.azure.resourcemanager.storage,
        com.azure.resourcemanager.trafficmanager;
    exports com.azure.resourcemanager.resources.fluentcore.model.implementation to
        com.azure.resourcemanager.appplatform,
        com.azure.resourcemanager.appservice,
        com.azure.resourcemanager.authorization,
        com.azure.resourcemanager.cdn,
        com.azure.resourcemanager.compute,
        com.azure.resourcemanager.containerinstance,
        com.azure.resourcemanager.containerregistry,
        com.azure.resourcemanager.containerservice,
        com.azure.resourcemanager.cosmos,
        com.azure.resourcemanager.dns,
        com.azure.resourcemanager.eventhubs,
        com.azure.resourcemanager.keyvault,
        com.azure.resourcemanager.monitor,
        com.azure.resourcemanager.msi,
        com.azure.resourcemanager.network,
        com.azure.resourcemanager.privatedns,
        com.azure.resourcemanager.redis,
        com.azure.resourcemanager.search,
        com.azure.resourcemanager.servicebus,
        com.azure.resourcemanager.sql,
        com.azure.resourcemanager.storage,
        com.azure.resourcemanager.trafficmanager;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.resources.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.resources.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.resources.fluentcore.model.implementation to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
