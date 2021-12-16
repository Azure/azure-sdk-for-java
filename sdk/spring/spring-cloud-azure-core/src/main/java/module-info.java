module com.azure.spring.core {

    requires com.azure.core.amqp;
    requires org.slf4j;
    requires spring.core;
    requires com.azure.identity;
    requires com.azure.core.management;
    requires java.desktop;
    requires spring.beans;
    requires spring.context;
    requires static com.azure.storage.blob;
    requires static com.azure.storage.file.share;

    exports com.azure.spring.core;
    exports com.azure.spring.core.aware;
    exports com.azure.spring.core.aware.authentication;
    exports com.azure.spring.core.connectionstring;
    exports com.azure.spring.core.credential;
    exports com.azure.spring.core.credential.descriptor;
    exports com.azure.spring.core.customizer;
    exports com.azure.spring.core.factory;
    exports com.azure.spring.core.factory.credential;
    exports com.azure.spring.core.properties;
    exports com.azure.spring.core.properties.authentication;
    exports com.azure.spring.core.properties.client;
    exports com.azure.spring.core.properties.profile;
    exports com.azure.spring.core.properties.proxy;
    exports com.azure.spring.core.properties.resource;
    exports com.azure.spring.core.properties.retry;
    exports com.azure.spring.core.resource;
    exports com.azure.spring.core.service;
    exports com.azure.spring.core.trace;
    exports com.azure.spring.core.util;
    exports com.azure.spring.core.credential.provider;

    exports com.azure.spring.core.implementation.converter to com.azure.spring.service;
}
