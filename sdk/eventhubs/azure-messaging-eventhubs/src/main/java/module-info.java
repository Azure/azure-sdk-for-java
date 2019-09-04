module com.azure.messaging.eventhubs {
    requires transitive com.azure.core;
    requires transitive com.azure.amqp;

    requires proton.j;
    requires qpid.proton.j.extensions;

    exports com.azure.messaging.eventhubs;
    exports com.azure.messaging.eventhubs.models;
}
