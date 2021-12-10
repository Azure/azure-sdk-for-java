module spring.messaging.azure.servicebus {
    requires com.azure.messaging.servicebus;
    requires spring.core;
    requires spring.messaging;
    requires spring.beans;
    requires spring.context;
    requires spring.messaging.azure;
    exports com.azure.spring.servicebus.core;
    exports com.azure.spring.servicebus.support.converter;
    exports com.azure.spring.servicebus.support;
    exports com.azure.spring.servicebus.core.properties;
}