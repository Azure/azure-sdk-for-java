module com.azure.resourcemanager.sql {
    requires transitive com.azure.resourcemanager.storage;

    exports com.azure.resourcemanager.sql;
    exports com.azure.resourcemanager.sql.fluent;
    exports com.azure.resourcemanager.sql.fluent.inner;
    exports com.azure.resourcemanager.sql.models;

    opens com.azure.resourcemanager.sql.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.sql.models to com.fasterxml.jackson.databind, com.azure.core;
}
