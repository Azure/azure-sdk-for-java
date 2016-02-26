/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.util.concurrent.*;
import java.util.function.*;

import com.microsoft.azure.servicebus.*;

/**
 * This sender class is a logical representation of sending events to a specific EventHub partition. Do not use this class 
 * if you do not care about sending events to specific partitions. Instead, use {@link EventHubClient#send} method.
 * @see To create an instance of PartitionSender refer to {@link EventHubClient#createPartitionSender(String)}.
 * @see To create an instance of EventHubClient refer to {@link EventHubClient#createFromConnectionString(String)}. 
 */
public final class PartitionSender extends ClientEntity
{
	private final String partitionId;
	private final String eventHubName;
	private final MessagingFactory factory;
	
	private MessageSender internalSender;
		
	private PartitionSender(MessagingFactory factory, String eventHubName, String partitionId)
	{
		super(null);
		this.partitionId = partitionId;
		this.eventHubName = eventHubName;
		this.factory = factory;
	}
	
	/**
	 * Internal-Only: factory pattern to Create EventHubSender
	 */
	static CompletableFuture<PartitionSender> Create(MessagingFactory factory, String eventHubName, String partitionId) throws ServiceBusException
	{
		final PartitionSender sender = new PartitionSender(factory, eventHubName, partitionId);
		return sender.createInternalSender()
				.thenApplyAsync(new Function<Void, PartitionSender>()
				{
					public PartitionSender apply(Void a)
					{
						return sender;
					}
				});
	}
	
	private CompletableFuture<Void> createInternalSender() throws ServiceBusException
	{
		return MessageSender.create(this.factory, StringUtil.getRandomString(), 
				String.format("%s/Partitions/%s", this.eventHubName, this.partitionId))
				.thenAcceptAsync(new Consumer<MessageSender>()
				{
					public void accept(MessageSender a) { PartitionSender.this.internalSender = a;}
				});
	}
	
    /**
	 * Synchronous version of {@link #send(EventData)} Api. 
	 */
    public final Void sendSync(final EventData data) 
			throws ServiceBusException
	{
        try
        {
            this.send(data).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread’s interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
                if (!(throwable instanceof RuntimeException) && 
                    !(throwable instanceof ServiceBusException))
                {
                    throwable = new ServiceBusException(true, throwable);
                }
                
				throw throwable;
			}
		}
    }
    
	/**
	 * Send {@link EventData} to a specific EventHub partition. The partition to send is pre-determined when this PartitionSender was created.
	 * @param data the {@link EventData} to be sent.
	 * @return     a CompletableFuture that can be completed when the send operations is done..
	 * @throws PayloadSizeExceededException    if the total size of the {@link EventData} exceeds a pre-defined limit set by the service. Default is 256k bytes.
	 * @throws ServiceBusException             if Service Bus service encountered problems during the operation.
	 * @throws UnresolvedAddressException      if there are Client to Service network connectivity issues, if the Azure DNS resolution of the ServiceBus Namespace fails (ex: namespace deleted etc.)
	 */
	public final CompletableFuture<Void> send(EventData data) 
			throws ServiceBusException
	{
		return this.internalSender.send(data.toAmqpMessage());
	}
	
    /**
	 * Synchronous version of {@link #send(Iterable<EventData>)} Api. 
	 */
    public final Void sendSync(final Iterable<EventData> eventDatas) 
			throws ServiceBusException
	{
        try
        {
            this.send(eventDatas).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread’s interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
                if (!(throwable instanceof RuntimeException) && 
                    !(throwable instanceof ServiceBusException))
                {
                    throwable = new ServiceBusException(true, throwable);
                }
                
				throw throwable;
			}
		}
    }
	
	/**
	 * Send {@link EventData} to a specific EventHub partition. The partition to send is pre-determined when this PartitionSender was created.
     * <p>
     * Sample code (sample uses sync version of the api but concept are identical):
     * <code>
     *         Gson gson = new GsonBuilder().create();
     *         EventHubClient client = EventHubClient.createFromConnectionStringSync("__connection__");
     *         PartitionSender senderToPartitionOne = client.createPartitionSenderSync("1");
     *         
     *         while (true)
     *         {
     *             LinkedList<EventData> events = new LinkedList<EventData>();
     *             for (int count = 1; count < 11; count++)
     *             {
     *                 PayloadEvent payload = new PayloadEvent(count);
     *                 byte[] payloadBytes = gson.toJson(payload).getBytes(Charset.defaultCharset());
     *                 EventData sendEvent = new EventData(payloadBytes);
     *                 Map<String, String> applicationProperties = new HashMap<String, String>();
     *                 applicationProperties.put("from", "javaClient");
     *                 sendEvent.setProperties(applicationProperties);
     *                 events.add(sendEvent);
     *             }
     *         
     *             senderToPartitionOne.sendSync(events);
     *             System.out.println(String.format("Sent Batch... Size: %s", events.size()));
     *         }		
     * </code>
	 * @param eventDatas batch of events to send to EventHub
	 * @return     a CompletableFuture that can be completed when the send operations is done..
	 * @throws PayloadSizeExceededException    if the total size of the {@link EventData} exceeds a pre-defined limit set by the service. Default is 256k bytes.
	 * @throws ServiceBusException             if Service Bus service encountered problems during the operation.
	 * @throws UnresolvedAddressException      if there are Client to Service network connectivity issues, if the Azure DNS resolution of the ServiceBus Namespace fails (ex: namespace deleted etc.)
	 */
	public final CompletableFuture<Void> send(Iterable<EventData> eventDatas) 
			throws ServiceBusException
	{
		if (eventDatas == null || IteratorUtil.sizeEquals(eventDatas, 0))
		{
			throw new IllegalArgumentException("EventData batch cannot be empty.");
		}
        
		return this.internalSender.send(EventDataUtil.toAmqpMessages(eventDatas));
	}

	@Override
	public CompletableFuture<Void> close()
	{
		if (this.internalSender == null)
		{
			return CompletableFuture.completedFuture(null);
		}
		else
		{
			return this.internalSender.close();
		}
	}
}
