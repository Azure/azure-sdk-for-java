/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.IllegalEntityException;
import com.microsoft.azure.servicebus.ServiceBusException;
import com.microsoft.azure.servicebus.SharedAccessSignatureTokenProvider;

class RealEventHubUtilities
{
	private ConnectionStringBuilder hubConnectionString = null;
	private String hubName = null;
	private String consumerGroup = EventHubClient.DEFAULT_CONSUMER_GROUP_NAME;
	private EventHubClient client;
	private ArrayList<String> partitionIds = null;
	private HashMap<String, PartitionSender> partitionSenders = new HashMap<String, PartitionSender>();
	
	static int QUERY_ENTITY_FOR_PARTITIONS = -1;
	
	RealEventHubUtilities()
	{
	}
	
	ArrayList<String> setup(int fakePartitions) throws ServiceBusException, IOException
	{
		// Get the connection string from the environment
		ehCacheCheck();
		
		// Get the consumer group from the environment, if present.
		String tempConsumerGroup = System.getenv("EVENT_HUB_CONSUMER_GROUP");
		if (tempConsumerGroup != null)
		{
			this.consumerGroup = tempConsumerGroup;
		}
		
		ArrayList<String> partitionIds = null;
		
		if (fakePartitions == RealEventHubUtilities.QUERY_ENTITY_FOR_PARTITIONS)
		{
			partitionIds = getPartitionIdsForTest();
		}
		else
		{
			partitionIds = new ArrayList<String>();
			for (int i = 0; i < fakePartitions; i++)
			{
				partitionIds.add(Integer.toString(i));
			}
		}
		
		// EventHubClient is source of all senders
		this.client = EventHubClient.createFromConnectionStringSync(this.hubConnectionString.toString());
		
		return partitionIds;
	}
	
	void shutdown() throws ServiceBusException
	{
		for (PartitionSender sender : this.partitionSenders.values())
		{
			sender.closeSync();
		}
		this.client.closeSync();
	}
	
	ConnectionStringBuilder getConnectionString()
	{
		ehCacheCheck();
		return this.hubConnectionString;
	}
	
	String getHubName()
	{
		ehCacheCheck();
		return this.hubName;
	}
	
	private void ehCacheCheck()
	{
		if (this.hubName == null)
		{
			this.hubConnectionString = new ConnectionStringBuilder(System.getenv("EVENT_HUB_CONNECTION_STRING"));
			this.hubName = this.hubConnectionString.getEntityPath();
		}
	}
	
	String getConsumerGroup()
	{
		return this.consumerGroup;
	}
	
	void sendToAny(String body, int count) throws ServiceBusException
	{
		for (int i = 0; i < count; i++)
		{
			sendToAny(body);
		}
	}
	
	void sendToAny(String body) throws ServiceBusException
	{
		EventData event = new EventData(body.getBytes());
		this.client.sendSync(event);
	}
	
	void sendToPartition(String partitionId, String body) throws IllegalArgumentException, ServiceBusException
	{
		EventData event = new EventData(body.getBytes());
		PartitionSender sender = null;
		if (this.partitionSenders.containsKey(partitionId))
		{
			sender = this.partitionSenders.get(partitionId);
		}
		else
		{
			sender = this.client.createPartitionSenderSync(partitionId);
			this.partitionSenders.put(partitionId, sender);
		}
		sender.sendSync(event);
	}
	
    ArrayList<String> getPartitionIdsForTest() throws IllegalEntityException
    {
    	if (this.partitionIds == null)
    	{
	    	this.partitionIds = new ArrayList<String>();
	    	ehCacheCheck();
	    	
	    	try
	    	{
	        	String contentEncoding = StandardCharsets.UTF_8.name();
	        	URI namespaceUri = new URI("https", this.hubConnectionString.getEndpoint().getHost(), null, null);
	        	String resourcePath = String.join("/", 
	        			namespaceUri.toString(),
	        			this.hubConnectionString.getEntityPath(),
	        			"consumergroups",
	        			this.consumerGroup,
	        			"partitions");
	        	
	        	final String authorizationToken = SharedAccessSignatureTokenProvider.generateSharedAccessSignature(
	        			this.hubConnectionString.getSasKeyName(), this.hubConnectionString.getSasKey(), 
	        			resourcePath, Duration.ofMinutes(20));
	        	        	
	            URLConnection connection = new URL(resourcePath).openConnection();
	        	connection.addRequestProperty("Authorization", authorizationToken);
	        	connection.setRequestProperty("Content-Type", "application/atom+xml;type=entry");
	        	connection.setRequestProperty("charset", contentEncoding);
	        	InputStream responseStream = connection.getInputStream();
	        	
	        	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	        	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	        	Document doc = docBuilder.parse(responseStream);
	        	
	        	XPath xpath = XPathFactory.newInstance().newXPath();
	        	NodeList partitionIdsNodes = (NodeList) xpath.evaluate("//feed/entry/title", doc.getDocumentElement(), XPathConstants.NODESET);
	        	if (partitionIdsNodes.getLength() == 0)
	        	{
	        		throw new IllegalEntityException("EventHub does not exist");
	        	}
	        	
	        	for (int partitionIndex = 0; partitionIndex < partitionIdsNodes.getLength(); partitionIndex++)
	        	{
	        		this.partitionIds.add(partitionIdsNodes.item(partitionIndex).getTextContent());    		
	        	}
	    	}
	    	catch(XPathExpressionException|ParserConfigurationException|IOException|InvalidKeyException|NoSuchAlgorithmException|URISyntaxException|SAXException exception)
	    	{
	    		final String errorMessage = String.format(Locale.US, "Encountered error while fetching the list of EventHub PartitionIds: %s", exception.getMessage());
	    		throw new EPHConfigurationException(errorMessage, exception);
	    	}
    	}

    	return this.partitionIds;
    }
	
}
