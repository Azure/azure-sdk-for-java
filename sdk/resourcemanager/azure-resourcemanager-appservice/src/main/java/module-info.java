module com.azure.resourcemanager.appservice {
    requires transitive com.azure.resourcemanager.keyvault;
    requires transitive com.azure.resourcemanager.msi;
    requires transitive com.azure.resourcemanager.dns;
    requires transitive com.azure.resourcemanager.storage;

    exports com.azure.resourcemanager.appservice;
    exports com.azure.resourcemanager.appservice.fluent;
    exports com.azure.resourcemanager.appservice.fluent.inner;
    exports com.azure.resourcemanager.appservice.models;

    opens com.azure.resourcemanager.appservice.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.appservice.models to com.fasterxml.jackson.databind, com.azure.core;
}
