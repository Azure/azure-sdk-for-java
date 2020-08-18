module com.azure.resourcemanager.compute {
    requires transitive com.azure.resourcemanager.msi;
    requires transitive com.azure.resourcemanager.storage;
    requires transitive com.azure.resourcemanager.network;

    exports com.azure.resourcemanager.compute;
    exports com.azure.resourcemanager.compute.fluent;
    exports com.azure.resourcemanager.compute.fluent.inner;
    exports com.azure.resourcemanager.compute.models;

    opens com.azure.resourcemanager.compute.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.compute.models to com.fasterxml.jackson.databind, com.azure.core;
}
