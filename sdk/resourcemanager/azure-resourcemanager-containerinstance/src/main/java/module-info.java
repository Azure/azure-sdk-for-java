module com.azure.resourcemanager.containerinstance {
    requires transitive com.azure.resourcemanager.storage;
    requires transitive com.azure.resourcemanager.msi;
    requires transitive com.azure.resourcemanager.network;
    requires com.azure.storage.file.share;

    exports com.azure.resourcemanager.containerinstance;
    exports com.azure.resourcemanager.containerinstance.fluent;
    exports com.azure.resourcemanager.containerinstance.fluent.inner;
    exports com.azure.resourcemanager.containerinstance.models;

    opens com.azure.resourcemanager.containerinstance.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.containerinstance.models to com.fasterxml.jackson.databind, com.azure.core;
}
