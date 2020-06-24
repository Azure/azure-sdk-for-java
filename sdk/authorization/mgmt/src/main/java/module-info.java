module azure.resourcemanager.authorization {
    exports com.azure.resourcemanager.authorization.models;
    exports com.azure.resourcemanager.authorization;
    requires transitive com.azure.core;
    requires transitive azure.core.management;
    requires azure.resourcemanager.resources;
}
