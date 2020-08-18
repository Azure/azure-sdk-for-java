module com.azure.resourcemanager.storage {
    requires transitive com.azure.resourcemanager.resources;

    exports com.azure.resourcemanager.storage;
    exports com.azure.resourcemanager.storage.fluent;
    exports com.azure.resourcemanager.storage.fluent.inner;
    exports com.azure.resourcemanager.storage.models;

    opens com.azure.resourcemanager.storage.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.storage.models to com.fasterxml.jackson.databind, com.azure.core;
}
