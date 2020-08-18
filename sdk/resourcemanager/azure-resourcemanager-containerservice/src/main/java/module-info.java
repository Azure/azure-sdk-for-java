module com.azure.resourcemanager.containerservice {
    requires transitive com.azure.resourcemanager.resources;

    exports com.azure.resourcemanager.containerservice;
    exports com.azure.resourcemanager.containerservice.fluent;
    exports com.azure.resourcemanager.containerservice.fluent.inner;
    exports com.azure.resourcemanager.containerservice.models;

    opens com.azure.resourcemanager.containerservice.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.containerservice.models to com.fasterxml.jackson.databind, com.azure.core;
}
