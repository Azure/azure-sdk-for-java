module com.azure.resourcemanager.keyvault {
    requires transitive com.azure.resourcemanager.authorization;
    requires transitive com.azure.security.keyvault.keys;
    requires transitive com.azure.security.keyvault.secrets;

    exports com.azure.resourcemanager.keyvault;
    exports com.azure.resourcemanager.keyvault.fluent;
    exports com.azure.resourcemanager.keyvault.fluent.inner;
    exports com.azure.resourcemanager.keyvault.models;
}
