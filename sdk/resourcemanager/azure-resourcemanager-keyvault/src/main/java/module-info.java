module com.azure.resourcemanager.keyvault {
    requires transitive com.azure.resourcemanager.authorization;
    requires transitive com.azure.security.keyvault.keys;
    requires transitive com.azure.security.keyvault.secrets;

    exports com.azure.resourcemanager.keyvault;
    exports com.azure.resourcemanager.keyvault.fluent;
    exports com.azure.resourcemanager.keyvault.fluent.inner;
    exports com.azure.resourcemanager.keyvault.models;

    opens com.azure.resourcemanager.keyvault.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.keyvault.models to com.fasterxml.jackson.databind, com.azure.core;
}
