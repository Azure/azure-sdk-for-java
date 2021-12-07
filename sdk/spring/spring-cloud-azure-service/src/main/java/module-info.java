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


}
