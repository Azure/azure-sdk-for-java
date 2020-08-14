module com.azure.resourcemanager.resources {
    requires transitive com.azure.core.management;
    requires com.github.spotbugs.annotations;

    exports com.azure.resourcemanager.resources.fluentcore;
    exports com.azure.resourcemanager.resources.fluentcore.arm;
    exports com.azure.resourcemanager.resources.fluentcore.arm.collection;
    exports com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;
    exports com.azure.resourcemanager.resources.fluentcore.arm.implementation;
    exports com.azure.resourcemanager.resources.fluentcore.arm.models;
    exports com.azure.resourcemanager.resources.fluentcore.arm.models.implementation;
    exports com.azure.resourcemanager.resources.fluentcore.collection;
    exports com.azure.resourcemanager.resources.fluentcore.dag;
    exports com.azure.resourcemanager.resources.fluentcore.model;
    exports com.azure.resourcemanager.resources.fluentcore.model.implementation;
    exports com.azure.resourcemanager.resources.fluentcore.rest;
    exports com.azure.resourcemanager.resources.fluentcore.utils;

    exports com.azure.resourcemanager.resources;
    exports com.azure.resourcemanager.resources.models;
    exports com.azure.resourcemanager.resources.fluent;
    exports com.azure.resourcemanager.resources.fluent.inner;
}
