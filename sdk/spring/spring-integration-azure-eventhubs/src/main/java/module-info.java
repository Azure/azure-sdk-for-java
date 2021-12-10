module spring.integration.azure.eventhubs {
    exports com.azure.spring.integration.eventhubs.inbound;
    exports com.azure.spring.integration.eventhubs.inbound.health;
    requires com.azure.messaging.eventhubs;
    requires spring.integration.azure.core;
    requires spring.messaging.azure.eventhubs;
    requires spring.messaging.azure;
    requires spring.integration.core;
    requires spring.messaging;
    requires spring.core;
}