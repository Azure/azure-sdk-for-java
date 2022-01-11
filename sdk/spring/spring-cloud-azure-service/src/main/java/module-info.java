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

    exports com.azure.spring.service.eventhubs.processor.consumer;
    exports com.azure.spring.service.servicebus.processor.consumer;
    exports com.azure.spring.service.servicebus.properties;
//    exports com.azure.spring.service.implementation.appconfiguration;
//    exports com.azure.spring.service.implementation.cosmos;
//    exports com.azure.spring.service.implementation.eventhubs.factory;
//    exports com.azure.spring.service.implementation.eventhubs.processor;
//    exports com.azure.spring.service.implementation.eventhubs.properties;
//    exports com.azure.spring.service.implementation.keyvault;
//    exports com.azure.spring.service.implementation.keyvault.certificates;
//    exports com.azure.spring.service.implementation.keyvault.secrets;
//    exports com.azure.spring.service.implementation.servicebus.factory;
//    exports com.azure.spring.service.implementation.servicebus.processor;

//    exports com.azure.spring.service.implementation.storage.blob;
//    exports com.azure.spring.service.implementation.storage.common;
//    exports com.azure.spring.service.implementation.storage.common.credential;
//    exports com.azure.spring.service.implementation.storage.fileshare;
//    exports com.azure.spring.service.implementation.storage.queue;
}
