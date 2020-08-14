module com.azure.resourcemanager {
    requires transitive com.azure.resourcemanager.compute;
    requires transitive com.azure.resourcemanager.appplatform;
    requires transitive com.azure.resourcemanager.appservice;
    requires transitive com.azure.resourcemanager.containerinstance;
    requires transitive com.azure.resourcemanager.containerregistry;
    requires transitive com.azure.resourcemanager.containerservice;
    requires transitive com.azure.resourcemanager.cosmos;
    requires transitive com.azure.resourcemanager.monitor;
    requires transitive com.azure.resourcemanager.sql;

    exports com.azure.resourcemanager;
}
