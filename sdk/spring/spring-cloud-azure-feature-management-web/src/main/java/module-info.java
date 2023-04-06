module com.azure.spring.cloud.feature.management.web {
    exports com.azure.spring.cloud.feature.management.web;

    requires org.reactivestreams;

    requires org.slf4j;

    requires reactor.core;

    requires spring.beans;

    requires spring.boot;

    requires spring.boot.autoconfigure;

    requires spring.context;

    requires spring.core;

    requires spring.web;
    
    requires transitive jakarta.servlet;
    
    requires transitive com.azure.spring.cloud.feature.management;
    
    requires transitive spring.webmvc;
}