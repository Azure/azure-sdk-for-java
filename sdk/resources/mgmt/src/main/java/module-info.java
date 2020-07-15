module com.azure.resourcemanager.resources {
    exports com.azure.resourcemanager.resources.fluentcore.arm;
    exports com.azure.resourcemanager.resources.fluentcore.arm.implementation;
    exports com.azure.resourcemanager.resources.fluentcore.utils;
    exports com.azure.resourcemanager.resources.fluentcore.collection;
    exports com.azure.resourcemanager.resources.fluentcore.arm.models.implementation;
    exports com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;
    exports com.azure.resourcemanager.resources.fluentcore.arm.models;
    exports com.azure.resourcemanager.resources.fluentcore.model;
    exports com.azure.resourcemanager.resources.fluentcore.arm.collection;
    exports com.azure.resourcemanager.resources.fluentcore;

    requires transitive com.azure.resourcemanager.base;
    requires com.github.spotbugs.annotations;
}
