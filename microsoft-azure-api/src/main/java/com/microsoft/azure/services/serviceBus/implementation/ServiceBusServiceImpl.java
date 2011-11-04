package com.microsoft.azure.services.serviceBus.implementation;

import java.io.InputStream;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.azure.services.serviceBus.Message;
import com.microsoft.azure.services.serviceBus.Queue;
import com.microsoft.azure.services.serviceBus.QueueList;
import com.microsoft.azure.services.serviceBus.ReceiveMode;
import com.microsoft.azure.services.serviceBus.ServiceBusService;
import com.microsoft.azure.services.serviceBus.implementation.Entry;
import com.microsoft.azure.services.serviceBus.implementation.Feed;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.auth.wrap.WrapFilter;
import com.microsoft.azure.utils.ServiceExceptionFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class ServiceBusServiceImpl implements ServiceBusService {

	private Client channel;
	private String uri;
	private BrokerPropertiesMapper mapper;
	static Log log = LogFactory.getLog(ServiceBusService.class);

	@Inject
	public ServiceBusServiceImpl(
			Client channel, 
			@Named("serviceBus") WrapFilter authFilter,
			@Named("serviceBus.uri") String uri,
			BrokerPropertiesMapper mapper) {

		this.channel = channel;
		this.uri = uri;
		this.mapper = mapper;
		channel.addFilter(authFilter);
	}

	public Client getChannel() {
		return channel;
	}

	public void setChannel(Client channel) {
		this.channel = channel;
	}

	private WebResource getResource() {
		return getChannel()
			.resource(uri);
	}

	private ServiceException processCatch(ServiceException e) {
		log.warn(e.getMessage(), e.getCause());
		return ServiceExceptionFactory.process("serviceBus", e);
	}

	public void sendMessage(String path, Message message) throws ServiceException {
		try {
			WebResource resource = getResource()
				.path(path)
				.path("messages");
			
			if (message.getContentType() != null)
				resource.type(message.getContentType());

			if (message.getProperties() != null)
				resource.header("BrokerProperties", mapper.toString(message.getProperties()));

			resource.post(message.getBody());
		}
		catch(UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		}
		catch(ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}

	public Message receiveMessage(String queuePath, Integer timeout,
			ReceiveMode receiveMode) throws ServiceException {

		WebResource resource = getResource()
			.path(queuePath)
			.path("messages")
			.path("head");

		if (timeout != null) {
			resource = resource.queryParam("timeout", Integer.toString(timeout));
		}

		ClientResponse clientResult;
		if (receiveMode == ReceiveMode.RECEIVE_AND_DELETE) {
			try {
				clientResult = resource.delete(ClientResponse.class);
			}
			catch(UniformInterfaceException e) {
				throw processCatch(new ServiceException(e));
			}
			catch(ClientHandlerException e) {
				throw processCatch(new ServiceException(e));
			}
		}
		else if (receiveMode == ReceiveMode.PEEK_LOCK) {
			try {
				clientResult = resource.post(ClientResponse.class, "");
			}
			catch(UniformInterfaceException e) {
				throw processCatch(new ServiceException(e));
			}
			catch(ClientHandlerException e) {
				throw processCatch(new ServiceException(e));
			}
		}
		else {
			throw new RuntimeException("Unknown ReceiveMode");
		}

		String brokerProperties = clientResult.getHeaders().getFirst("BrokerProperties");
		String location = clientResult.getHeaders().getFirst("Location");
		MediaType contentType = clientResult.getType();
		Date date = clientResult.getResponseDate();


		Message result = new Message();
		if (brokerProperties != null)
		{
			result.setProperties(mapper.fromString(brokerProperties));
		}
		if (contentType != null)
		{
			result.setContentType(clientResult.toString());
		}
		if (location != null)
		{
			result.getProperties().setLockLocation(location);
		}
		result.setDate(date);
		result.setBody(clientResult.getEntityInputStream());
		return result;
	}

	public Message receiveMessage(String topicPath, String subscriptionName,
			int timeout, ReceiveMode receiveMode) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	public void abandonMessage(Message message) throws ServiceException {
		try {
			getChannel()
				.resource(message.getLockLocation())
				.delete();
		}
		catch(UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		}
		catch(ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}

	public void completeMessage(Message message) throws ServiceException {
		// TODO Auto-generated method stub
		
	}

	public Queue createQueue(Queue entry) throws ServiceException {
		try {
			return getResource()
				.path(entry.getName())
				.type("application/atom+xml")//;type=entry;charset=utf-8")
				.put(Queue.class, entry);
		}
		catch(UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		}
		catch(ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}

	public void deleteQueue(String queuePath) throws ServiceException {
		try {
			getResource()
				.path(queuePath)
				.delete();
		}
		catch(UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		}
		catch(ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}

	public Queue getQueue(String queuePath) throws ServiceException {
		try {
			return getResource()
					.path(queuePath)
					.get(Queue.class);
		}
		catch(UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		}
		catch(ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}

	
	public QueueList getQueueList() throws ServiceException {
		try {
			Feed feed = getResource()
					.path("$Resources/Queues")
					.get(Feed.class);
			ArrayList<Queue> queues = new ArrayList<Queue>();
			for(Entry entry : feed.getEntries()){
				queues.add(new Queue(entry));
			}
			QueueList result = new QueueList();
			result.setQueues(queues);
			return result;
		}
		catch(UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		}
		catch(ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}
	public Entry createTopic(Entry entry) throws ServiceException {
		try {
			return getResource()
				.path(entry.getTitle())
				.type("application/atom+xml")//;type=entry;charset=utf-8")
				.put(Entry.class, entry);
		}
		catch(UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		}
		catch(ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}

	public void deleteTopic(String topicPath) throws ServiceException {
		try {
			getResource()
				.path(topicPath)
				.delete();
		}
		catch(UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		}
		catch(ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}

	public Entry getTopic(String topicPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public Feed getTopics() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addSubscription(String topicPath, String subscriptionName,
			Entry subscription) {
		// TODO Auto-generated method stub

	}

	public void removeSubscription(String topicPath, String subscriptionName) {
		// TODO Auto-generated method stub

	}

	public Entry getSubscription(String topicPath, String subscriptionName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Feed getSubscriptions(String topicPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addRule(String topicPath, String subscriptionName,
			String ruleName, Entry rule) {
		// TODO Auto-generated method stub

	}

	public void removeRule(String topicPath, String subscriptionName,
			String ruleName) {
		// TODO Auto-generated method stub

	}

	public Entry getRule(String topicPath, String subscriptionName,
			String ruleName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Feed getRules(String topicPath, String subscriptionName) {
		// TODO Auto-generated method stub
		return null;
	}



}
