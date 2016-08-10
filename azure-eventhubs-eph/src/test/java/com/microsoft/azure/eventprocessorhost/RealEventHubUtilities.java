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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;
import com.microsoft.azure.servicebus.SharedAccessSignatureTokenProvider;

class RealEventHubUtilities
{
	private ConnectionStringBuilder hubConnectionString;
	private String hubName;
	private EventHubClient client;
	private HashMap<String, PartitionSender> partitionSenders = new HashMap<String, PartitionSender>();
	
	RealEventHubUtilities()
	{
	}
	
	ArrayList<String> setup() throws ServiceBusException, IOException
	{
		// Get the connection string from the environment
		this.hubConnectionString = new ConnectionStringBuilder(System.getenv("EPHTESTHUB"));
		this.hubName = this.hubConnectionString.getEntityPath();
		
		// Get the partition ids in part to verify that the eventhub actually exists
		ArrayList<String> partitionIds = getPartitionIds();
		
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
		return this.hubConnectionString;
	}
	
	String getHubName()
	{
		return this.hubName;
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
	
	// Code borrowed from PartitionManager, requires Manage claim
	// Replace when PartitionManager is upgraded with better code
    ArrayList<String> getPartitionIds()
    {
    	ArrayList<String> partitionIds = null;
    	
    	try
    	{
        	String contentEncoding = StandardCharsets.UTF_8.name();
        	URI namespaceUri = new URI("https", this.hubConnectionString.getEndpoint().getHost(), null, null);
        	String resourcePath = String.join("/", namespaceUri.toString(), this.hubName);
        	
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
        	Node partitionIdsNode = ((NodeList) xpath.evaluate("//entry/content/EventHubDescription/PartitionIds", doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        	NodeList partitionIdsNodes = partitionIdsNode.getChildNodes();
        	
        	partitionIds = new ArrayList<String>();
            for (int partitionIndex = 0; partitionIndex < partitionIdsNodes.getLength(); partitionIndex++)
        	{
        		partitionIds.add(partitionIdsNodes.item(partitionIndex).getTextContent());    		
        	}
    	}
    	catch(XPathExpressionException|ParserConfigurationException|IOException|InvalidKeyException|NoSuchAlgorithmException|URISyntaxException|SAXException exception)
    	{
    		throw new EPHConfigurationException("Encountered error while fetching the list of EventHub PartitionIds", exception);
    	}

    	return partitionIds;
    }
	
}
