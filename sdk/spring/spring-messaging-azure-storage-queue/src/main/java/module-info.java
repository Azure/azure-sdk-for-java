module spring.messaging.azure.storage.queue {
    requires com.azure.storage.queue;
    requires spring.core;
    requires spring.messaging.azure;
    requires spring.messaging;
    exports com.azure.spring.storage.queue.core;
}