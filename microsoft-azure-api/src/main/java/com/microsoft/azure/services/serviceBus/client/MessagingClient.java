package com.microsoft.azure.services.serviceBus.client;

import javax.inject.Inject;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.Message;
import com.microsoft.azure.services.serviceBus.ReceiveMode;
import com.microsoft.azure.services.serviceBus.ServiceBusService;

public class MessagingClient {
	private ServiceBusService service;


	public MessagingClient() throws Exception {
		this.setService(Configuration.load().create(ServiceBusService.class));
	}

	public MessagingClient(String profile) throws Exception {
		this.setService(Configuration.load().create(profile, ServiceBusService.class));
	}

	public MessagingClient(Configuration configuration) throws Exception {
		this.setService(configuration.create(ServiceBusService.class));
	}

	public MessagingClient(String profile, Configuration configuration) throws Exception {
		this.setService(configuration.create(profile, ServiceBusService.class));
	}

	@Inject
	public MessagingClient(ServiceBusService service) {
		this.setService(service);
	}

	public void setService(ServiceBusService service) {
		this.service = service;
	}

	public ServiceBusService getService() {
		return service;
	}

	public MessageTransceiver openQueue(String queueName) {
		final String queue = queueName;
		return new MessageTransceiver() {

			public void sendMessage(Message message) throws ServiceException {
				service.sendMessage(queue, message);
			}

			public Message receiveMessage() throws ServiceException {
				return receiveMessage(ReceiveMessageOptions.DEFAULT);
			}

			public Message receiveMessage(ReceiveMessageOptions options)
					throws ServiceException {
				return service.receiveMessage(queue, options.getTimeout(),
						ReceiveMode.RECEIVE_AND_DELETE);
			}

			public Message peekLockMessage() throws ServiceException {
				return peekLockMessage(ReceiveMessageOptions.DEFAULT);
			}

			public Message peekLockMessage(ReceiveMessageOptions options)
					throws ServiceException {
				return service.receiveMessage(queue, options.getTimeout(),
						ReceiveMode.PEEK_LOCK);
			}

			public void abandonMessage(Message message) throws ServiceException {
				service.abandonMessage(message);
			}

			public void completeMessage(Message message)
					throws ServiceException {
				service.completeMessage(message);
			}
		};
	}

	public MessageSender openTopic(String topicName) {
		final String topic = topicName;
		return new MessageSender() {
			public void sendMessage(Message message) throws ServiceException {
				service.sendMessage(topic, message);
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
				return service.receiveMessage(topic, subscription, options.getTimeout(),
						ReceiveMode.RECEIVE_AND_DELETE);
			}
			
			public Message peekLockMessage() throws ServiceException {
				return peekLockMessage(ReceiveMessageOptions.DEFAULT);
			}

			public Message peekLockMessage(ReceiveMessageOptions options)
					throws ServiceException {
				return service.receiveMessage(topic, subscription, options.getTimeout(),
						ReceiveMode.PEEK_LOCK);
			}

			public void abandonMessage(Message message) throws ServiceException {
				service.abandonMessage(message);
			}

			public void completeMessage(Message message)
					throws ServiceException {
				service.completeMessage(message);
			}
		};
	}
}
