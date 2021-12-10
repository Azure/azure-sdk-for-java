module spring.cloud.azure.stream.binder.servicebus {
    requires spring.messaging.azure.servicebus;
    requires spring.cloud.azure.stream.binder.servicebus.core;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.cloud.stream;
    requires spring.context;
    requires spring.core;
}