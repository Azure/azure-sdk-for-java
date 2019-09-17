module com.azure.messaging.eventhubs {
    requires transitive com.azure.core;
    requires transitive com.azure.amqp;

    requires proton.j;
    requires qpid.proton.j.extensions;
    requires org.reactivestreams;

    exports com.azure.messaging.eventhubs;
    exports com.azure.messaging.eventhubs.models;

    opens com.azure.messaging.eventhubs;
    opens com.azure.messaging.eventhubs.models;

    opens com.azure.messaging.eventhubs.implementation;
    opens com.azure.messaging.eventhubs.implementation.handler;
}
