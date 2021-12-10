module spring.cloud.azure.stream.binder.servicebus.core {
    exports com.azure.spring.cloud.stream.binder.servicebus.properties;
    exports com.azure.spring.cloud.stream.binder.servicebus.provisioning;
    requires spring.cloud.stream;
    requires spring.messaging.azure;
    requires spring.messaging.azure.servicebus;
    requires spring.boot;
}