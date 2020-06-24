module test.azure.resourcemanager.resources {
    requires org.junit.jupiter.api;
    requires azure.resourcemanager.resources;
    requires com.azure.http.netty;
    requires com.azure.identity;

    exports com.azure.resourcemanager.resources.module.core;
}
