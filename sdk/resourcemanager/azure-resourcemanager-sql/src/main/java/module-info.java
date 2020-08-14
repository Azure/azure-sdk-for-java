module com.azure.resourcemanager.sql {
    requires transitive com.azure.resourcemanager.storage;

    exports com.azure.resourcemanager.sql;
    exports com.azure.resourcemanager.sql.fluent;
    exports com.azure.resourcemanager.sql.fluent.inner;
    exports com.azure.resourcemanager.sql.models;
}
