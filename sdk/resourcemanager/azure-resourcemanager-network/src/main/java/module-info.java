module com.azure.resourcemanager.network {
    requires transitive com.azure.resourcemanager.resources;

    exports com.azure.resourcemanager.network;
    exports com.azure.resourcemanager.network.fluent;
    exports com.azure.resourcemanager.network.fluent.inner;
    exports com.azure.resourcemanager.network.models;

    opens com.azure.resourcemanager.network.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.network.models to com.fasterxml.jackson.databind, com.azure.core;
}
