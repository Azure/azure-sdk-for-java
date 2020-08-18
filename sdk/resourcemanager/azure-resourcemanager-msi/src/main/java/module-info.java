module com.azure.resourcemanager.msi {
    requires transitive com.azure.resourcemanager.authorization;

    exports com.azure.resourcemanager.msi;
    exports com.azure.resourcemanager.msi.fluent;
    exports com.azure.resourcemanager.msi.fluent.inner;
    exports com.azure.resourcemanager.msi.models;

    opens com.azure.resourcemanager.msi.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.msi.models to com.fasterxml.jackson.databind, com.azure.core;
}
