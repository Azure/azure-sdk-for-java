package com.microsoft.azure.services.serviceBus.contract;

import java.io.InputStream;
import java.rmi.UnexpectedException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3._2005.atom.Entry;
import org.w3._2005.atom.Feed;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.auth.wrap.WrapFilter;
import com.microsoft.azure.utils.ServiceExceptionFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class ServiceBusContractImpl implements ServiceBusContract {

	private Client channel;
	private String uri;
	private BrokerPropertiesMapper mapper;
	static Log log = LogFactory.getLog(ServiceBusContract.class);

	@Inject
	public ServiceBusContractImpl(
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

	// REVIEW: contentType will be needed
	public void sendMessage(String path, BrokerProperties properties, InputStream body) throws ServiceException {
		try {
			getResource()
				.path(path)
				.path("messages")
				.header("BrokerProperties", mapper.toString(properties))
				.post(body);
		}
		catch(UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		}
		catch(ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}

	public MessageResult receiveMessage(String queuePath, Integer timeout,
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

		// REVIEW: harden this - it's much too brittle. throws null exceptions very easily
		MessageResult result = new MessageResult();
		result.setBrokerProperties(mapper.fromString(clientResult.getHeaders().getFirst("BrokerProperties")));
		result.setBody(clientResult.getEntityInputStream());
		return result;
	}


	public Entry createQueue(Entry entry) throws ServiceException {
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

	public Entry getQueue(String queuePath) throws ServiceException {
		try {
			return getResource()
					.path(queuePath)
					.get(Entry.class);
		}
		catch(UniformInterfaceException e) {
			throw processCatch(new ServiceException(e));
		}
		catch(ClientHandlerException e) {
			throw processCatch(new ServiceException(e));
		}
	}

	public Feed getQueues() throws ServiceException {
		try {
			return getResource()
					.path("$Resources/Queues")
					.get(Feed.class);
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
