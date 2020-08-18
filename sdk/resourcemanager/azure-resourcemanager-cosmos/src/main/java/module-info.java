module com.azure.resourcemanager.cosmos {
    requires transitive com.azure.resourcemanager.resources;

    exports com.azure.resourcemanager.cosmos;
    exports com.azure.resourcemanager.cosmos.fluent;
    exports com.azure.resourcemanager.cosmos.fluent.inner;
    exports com.azure.resourcemanager.cosmos.models;

    opens com.azure.resourcemanager.cosmos.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.cosmos.models to com.fasterxml.jackson.databind, com.azure.core;
}
