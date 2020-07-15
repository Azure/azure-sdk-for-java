module azure.resourcemanager.test {
    requires transitive azure.resourcemanager.base;
    requires com.azure.http.netty;
    requires com.azure.core.test;
    requires com.azure.identity;
    requires org.junit.jupiter.api;
    requires nimbus.jose.jwt;
}
