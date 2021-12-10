module spring.messaging.azure.eventhubs {
    requires com.azure.messaging.eventhubs;
    requires spring.core;
    requires spring.messaging.azure;
    requires spring.beans;
    requires spring.context;
    requires spring.messaging;

    exports com.azure.spring.eventhubs.checkpoint;
    exports com.azure.spring.eventhubs.core;
    exports com.azure.spring.eventhubs.support.converter;
    exports com.azure.spring.eventhubs.core.properties;
    exports com.azure.spring.eventhubs.core.processor;
    exports com.azure.spring.eventhubs.core.producer;
}