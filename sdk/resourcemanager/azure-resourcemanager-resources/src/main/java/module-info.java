module com.azure.resourcemanager.resources {
    requires transitive com.azure.core.management;
    requires com.github.spotbugs.annotations;

    exports com.azure.resourcemanager.resources.fluentcore;
    exports com.azure.resourcemanager.resources.fluentcore.arm;
    exports com.azure.resourcemanager.resources.fluentcore.arm.collection;
    exports com.azure.resourcemanager.resources.fluentcore.arm.models;
    exports com.azure.resourcemanager.resources.fluentcore.collection;
    exports com.azure.resourcemanager.resources.fluentcore.dag;
    exports com.azure.resourcemanager.resources.fluentcore.model;
    exports com.azure.resourcemanager.resources.fluentcore.rest;
    exports com.azure.resourcemanager.resources.fluentcore.utils;

    exports com.azure.resourcemanager.resources;
    exports com.azure.resourcemanager.resources.models;
    exports com.azure.resourcemanager.resources.fluent;
    exports com.azure.resourcemanager.resources.fluent.inner;

    opens com.azure.resourcemanager.resources.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.resources.models to com.fasterxml.jackson.databind, com.azure.core;

    exports com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation
        to com.azure.resourcemanager.storage, com.azure.resourcemanager.authorization, com.azure.resourcemanager.keyvault,
        com.azure.resourcemanager.msi, com.azure.resourcemanager.network, com.azure.resourcemanager.compute,
        com.azure.resourcemanager.sql, com.azure.resourcemanager.dns, com.azure.resourcemanager.appservice,
        com.azure.resourcemanager.cosmos, com.azure.resourcemanager.containerservice, com.azure.resourcemanager.monitor,
        com.azure.resourcemanager.containerregistry, com.azure.resourcemanager.appplatform, com.azure.resourcemanager.containerinstance,
        com.azure.resourcemanager.privatedns, com.azure.resourcemanager;
    exports com.azure.resourcemanager.resources.fluentcore.arm.implementation
        to com.azure.resourcemanager.storage, com.azure.resourcemanager.authorization, com.azure.resourcemanager.keyvault,
        com.azure.resourcemanager.msi, com.azure.resourcemanager.network, com.azure.resourcemanager.compute,
        com.azure.resourcemanager.sql, com.azure.resourcemanager.dns, com.azure.resourcemanager.appservice,
        com.azure.resourcemanager.cosmos, com.azure.resourcemanager.containerservice, com.azure.resourcemanager.monitor,
        com.azure.resourcemanager.containerregistry, com.azure.resourcemanager.appplatform, com.azure.resourcemanager.containerinstance,
        com.azure.resourcemanager.privatedns, com.azure.resourcemanager;
    exports com.azure.resourcemanager.resources.fluentcore.arm.models.implementation
        to com.azure.resourcemanager.storage, com.azure.resourcemanager.authorization, com.azure.resourcemanager.keyvault,
        com.azure.resourcemanager.msi, com.azure.resourcemanager.network, com.azure.resourcemanager.compute,
        com.azure.resourcemanager.sql, com.azure.resourcemanager.dns, com.azure.resourcemanager.appservice,
        com.azure.resourcemanager.cosmos, com.azure.resourcemanager.containerservice, com.azure.resourcemanager.monitor,
        com.azure.resourcemanager.containerregistry, com.azure.resourcemanager.appplatform, com.azure.resourcemanager.containerinstance,
        com.azure.resourcemanager.privatedns, com.azure.resourcemanager;
    exports com.azure.resourcemanager.resources.fluentcore.model.implementation
        to com.azure.resourcemanager.storage, com.azure.resourcemanager.authorization, com.azure.resourcemanager.keyvault,
        com.azure.resourcemanager.msi, com.azure.resourcemanager.network, com.azure.resourcemanager.compute,
        com.azure.resourcemanager.sql, com.azure.resourcemanager.dns, com.azure.resourcemanager.appservice,
        com.azure.resourcemanager.cosmos, com.azure.resourcemanager.containerservice, com.azure.resourcemanager.monitor,
        com.azure.resourcemanager.containerregistry, com.azure.resourcemanager.appplatform, com.azure.resourcemanager.containerinstance,
        com.azure.resourcemanager.privatedns, com.azure.resourcemanager;
}
