module com.azure.spring.service {

    requires transitive com.azure.spring.core;
    requires static com.azure.cosmos;
    requires static com.azure.data.appconfiguration;
    requires static com.azure.messaging.eventhubs;
    requires static com.azure.messaging.servicebus;
    requires static com.azure.security.keyvault.certificates;
    requires static com.azure.security.keyvault.secrets;
    requires static com.azure.storage.blob;
    requires static com.azure.storage.file.share;
    requires static com.azure.storage.queue;
    requires spring.core;

    exports com.azure.spring.service.appconfiguration;
    exports com.azure.spring.service.cosmos;
    exports com.azure.spring.service.eventhubs.factory;
    exports com.azure.spring.service.eventhubs.processor;
    exports com.azure.spring.service.eventhubs.processor.consumer;
    exports com.azure.spring.service.eventhubs.properties;
    exports com.azure.spring.service.keyvault;
    exports com.azure.spring.service.keyvault.certificates;
    exports com.azure.spring.service.keyvault.secrets;
    exports com.azure.spring.service.servicebus.factory;
    exports com.azure.spring.service.servicebus.processor;
    exports com.azure.spring.service.servicebus.processor.consumer;
    exports com.azure.spring.service.servicebus.properties;
    exports com.azure.spring.service.storage.blob;
    exports com.azure.spring.service.storage.common;
    exports com.azure.spring.service.storage.common.credential;
    exports com.azure.spring.service.storage.fileshare;
    exports com.azure.spring.service.storage.queue;

}
