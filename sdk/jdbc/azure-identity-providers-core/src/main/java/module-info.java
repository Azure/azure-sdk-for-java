module com.azure.identity.providers.core {
    requires com.azure.identity;

    exports com.azure.identity.providers.jdbc.implementation.template to
        com.azure.identity.providers.mysql,
        com.azure.identity.providers.postgresql;
}
