package com.azure.spring.integration.servicebus.factory;


public interface ServiceBusProvisioner {

    void provisionNamespace(String namespace);

    void provisionQueue(String namespace, String queue);

    void provisionTopic(String namespace, String topic);

    void provisionSubscription(String namespace, String topic, String subscription);

}
