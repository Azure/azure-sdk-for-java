package com.microsoft.azure.services.serviceBus.client;

import javax.inject.Inject;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.Message;
import com.microsoft.azure.services.serviceBus.ReceiveMessageOptions;
import com.microsoft.azure.services.serviceBus.ReceiveMode;
import com.microsoft.azure.services.serviceBus.ServiceBusContract;

public class MessagingClient {
    private ServiceBusContract service;

    public MessagingClient() throws Exception {
        this.setService(Configuration.load().create(ServiceBusContract.class));
    }

    public MessagingClient(String profile) throws Exception {
        this.setService(Configuration.load().create(profile, ServiceBusContract.class));
    }

    public MessagingClient(Configuration configuration) throws Exception {
        this.setService(configuration.create(ServiceBusContract.class));
    }

    public MessagingClient(String profile, Configuration configuration) throws Exception {
        this.setService(configuration.create(profile, ServiceBusContract.class));
    }

    @Inject
    public MessagingClient(ServiceBusContract service) {
        this.setService(service);
    }

    public void setService(ServiceBusContract service) {
        this.service = service;
    }

    public ServiceBusContract getService() {
        return service;
    }

    public MessageTransceiver openQueue(String queueName) {
        final String queue = queueName;
        return new MessageTransceiver() {

            public void sendMessage(Message message) throws ServiceException {
                service.sendQueueMessage(queue, message);
            }

            public Message receiveMessage() throws ServiceException {
                return receiveMessage(ReceiveMessageOptions.DEFAULT);
            }

            public Message receiveMessage(ReceiveMessageOptions options)
                    throws ServiceException {
                return service.receiveQueueMessage(queue, options);
            }

            public void unlockMessage(Message message) throws ServiceException {
                service.unlockMessage(message);
            }

            public void deleteMessage(Message message)
                    throws ServiceException {
                service.deleteMessage(message);
            }
        };
    }

    public MessageSender openTopic(String topicName) {
        final String topic = topicName;
        return new MessageSender() {
            public void sendMessage(Message message) throws ServiceException {
                service.sendQueueMessage(topic, message);
            }
        };
    }

    public MessageReceiver openSubscription(String topicName, String subscriptionName) {
        final String topic = topicName;
        final String subscription = subscriptionName;
        return new MessageReceiver() {

            public Message receiveMessage() throws ServiceException {
                return receiveMessage(ReceiveMessageOptions.DEFAULT);
            }

            public Message receiveMessage(ReceiveMessageOptions options)
                    throws ServiceException {
                return service.receiveSubscriptionMessage(topic, subscription, options);
            }

            public void unlockMessage(Message message) throws ServiceException {
                service.unlockMessage(message);
            }

            public void deleteMessage(Message message)
                    throws ServiceException {
                service.deleteMessage(message);
            }
        };
    }
}
