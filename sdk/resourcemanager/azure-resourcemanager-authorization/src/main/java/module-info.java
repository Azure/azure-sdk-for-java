module com.azure.resourcemanager.authorization {
    requires transitive com.azure.resourcemanager.resources;

    exports com.azure.resourcemanager.authorization;
    exports com.azure.resourcemanager.authorization.fluent;
    exports com.azure.resourcemanager.authorization.fluent.inner;
    exports com.azure.resourcemanager.authorization.models;
    exports com.azure.resourcemanager.authorization.utils;

    opens com.azure.resourcemanager.authorization.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.authorization.models to com.fasterxml.jackson.databind, com.azure.core;
}
