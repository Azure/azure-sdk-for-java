module spring.cloud.azure.stream.binder.eventhubs {
    requires com.azure.messaging.eventhubs;
    requires spring.cloud.azure.stream.binder.eventhubs.core;
    requires spring.messaging.azure.eventhubs;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.cloud.stream;
    requires spring.context;
    requires spring.boot.actuator.autoconfigure;
    requires spring.core;
    requires spring.integration.azure.core;
    requires spring.boot.actuator;
    requires spring.integration.azure.eventhubs;
    requires spring.messaging.azure;
    requires slf4j.api;
    requires spring.expression;
    requires spring.integration.core;
    requires spring.messaging;
}