module com.azure.resourcemanager.containerregistry {
    requires transitive com.azure.resourcemanager.storage;

    exports com.azure.resourcemanager.containerregistry;
    exports com.azure.resourcemanager.containerregistry.fluent;
    exports com.azure.resourcemanager.containerregistry.fluent.inner;
    exports com.azure.resourcemanager.containerregistry.models;

    opens com.azure.resourcemanager.containerregistry.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.containerregistry.models to com.fasterxml.jackson.databind, com.azure.core;
}
