module com.azure.storage.queue {
    requires transitive com.azure.core;
    requires com.azure.storage.common;

    exports com.azure.storage.queue;
    exports com.azure.storage.queue.models;

    opens com.azure.storage.queue.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.queue.implementation to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
