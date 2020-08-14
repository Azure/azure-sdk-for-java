module com.azure.resourcemanager.appservice {
    requires transitive com.azure.resourcemanager.keyvault;
    requires transitive com.azure.resourcemanager.msi;
    requires transitive com.azure.resourcemanager.dns;
    requires transitive com.azure.resourcemanager.storage;

    exports com.azure.resourcemanager.appservice;
    exports com.azure.resourcemanager.appservice.fluent;
    exports com.azure.resourcemanager.appservice.fluent.inner;
    exports com.azure.resourcemanager.appservice.models;
}
