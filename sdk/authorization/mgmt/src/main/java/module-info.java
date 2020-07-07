module com.azure.resourcemanager.authorization {
    requires transitive com.azure.core;
    requires transitive com.azure.core.management;
    requires com.azure.resourcemanager.resources;

    exports com.azure.resourcemanager.authorization.models;
    exports com.azure.resourcemanager.authorization;

    opens com.azure.resourcemanager.authorization.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.resourcemanager.authorization.fluent.inner to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
