module spring.cloud.azure.stream.binder.eventhubs.core {
    requires spring.cloud.stream;
    requires spring.messaging.azure.eventhubs;
    requires spring.messaging.azure;
    requires spring.boot;
    exports com.azure.spring.cloud.stream.binder.eventhubs.properties;
    exports com.azure.spring.cloud.stream.binder.eventhubs.provisioning;
}