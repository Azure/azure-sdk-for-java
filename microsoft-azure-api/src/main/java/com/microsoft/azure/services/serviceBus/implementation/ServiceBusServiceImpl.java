package com.microsoft.azure.services.serviceBus.implementation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.services.serviceBus.ListQueuesResult;
import com.microsoft.azure.services.serviceBus.ListTopicsResult;
import com.microsoft.azure.services.serviceBus.Message;
import com.microsoft.azure.services.serviceBus.Queue;
import com.microsoft.azure.services.serviceBus.ReceiveMessageOptions;
import com.microsoft.azure.services.serviceBus.ServiceBusService;
import com.microsoft.azure.services.serviceBus.Topic;
import com.microsoft.azure.utils.ServiceExceptionFactory;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ServiceBusServiceImpl implements ServiceBusService {

	private ServiceBusService service;
	static Log log = LogFactory.getLog(ServiceBusService.class);

	public ServiceBusServiceImpl(ServiceBusServiceForJersey service)
	{
		this.service = service;
	}
	

	private ServiceException processCatch(ServiceException e) {
		log.warn(e.getMessage(), e.getCause());
		return ServiceExceptionFactory.process("serviceBus", e);
	}


	public void sendMessage(String path, Message message)
			throws ServiceException {
		try {
			service.sendMessage(path, message);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public Message receiveQueueMessage(String queueName)
			throws ServiceException {
		try {
			return service.receiveQueueMessage(queueName);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public Message receiveQueueMessage(String queueName,
			ReceiveMessageOptions options) throws ServiceException {
		try {
			return service.receiveQueueMessage(queueName, options);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public Message receiveSubscriptionMessage(String topicName,
			String subscriptionName) throws ServiceException {
		try {
			return service.receiveSubscriptionMessage(topicName,
					subscriptionName);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public Message receiveSubscriptionMessage(String topicName,
			String subscriptionName, ReceiveMessageOptions options)
			throws ServiceException {
		try {
			return service.receiveSubscriptionMessage(topicName,
					subscriptionName, options);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public void unlockMessage(Message message) throws ServiceException {
		try {
			service.unlockMessage(message);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public void deleteMessage(Message message) throws ServiceException {
		try {
			service.deleteMessage(message);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public Queue createQueue(Queue queue) throws ServiceException {
		try {
			return service.createQueue(queue);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public void deleteQueue(String queuePath) throws ServiceException {
		try {
			service.deleteQueue(queuePath);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public Queue getQueue(String queuePath) throws ServiceException {
		try {
			return service.getQueue(queuePath);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public ListQueuesResult listQueues() throws ServiceException {
		try {
			return service.listQueues();
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public Topic createTopic(Topic topic) throws ServiceException {
		try {
			return service.createTopic(topic);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public void deleteTopic(String topicPath) throws ServiceException {
		try {
			service.deleteTopic(topicPath);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public Topic getTopic(String topicPath) throws ServiceException {
		try {
			return service.getTopic(topicPath);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public ListTopicsResult listTopics() throws ServiceException {
		try {
			return service.listTopics();
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public void addSubscription(String topicPath, String subscriptionName,
			Entry subscription) throws ServiceException {
		try {
			service.addSubscription(topicPath, subscriptionName, subscription);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public void removeSubscription(String topicPath, String subscriptionName)
			throws ServiceException {
		try {
			service.removeSubscription(topicPath, subscriptionName);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public Entry getSubscription(String topicPath, String subscriptionName)
			throws ServiceException {
		try {
			return service.getSubscription(topicPath, subscriptionName);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public Feed getSubscriptions(String topicPath) throws ServiceException {
		try {
			return service.getSubscriptions(topicPath);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public void addRule(String topicPath, String subscriptionName,
			String ruleName, Entry rule) throws ServiceException {
		try {
			service.addRule(topicPath, subscriptionName, ruleName, rule);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public void removeRule(String topicPath, String subscriptionName,
			String ruleName) throws ServiceException {
		try {
			service.removeRule(topicPath, subscriptionName, ruleName);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public Entry getRule(String topicPath, String subscriptionName,
			String ruleName) throws ServiceException {
		try {
			return service.getRule(topicPath, subscriptionName, ruleName);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}


	public Feed getRules(String topicPath, String subscriptionName)
			throws ServiceException {
		try {
			return service.getRules(topicPath, subscriptionName);
		} catch (UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		} catch (ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}
	

}
