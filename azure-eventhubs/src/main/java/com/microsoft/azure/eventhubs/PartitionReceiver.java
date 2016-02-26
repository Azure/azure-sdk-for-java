/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import org.apache.qpid.proton.message.Message;
import com.microsoft.azure.servicebus.*;

public final class PartitionReceiver extends ClientEntity
{
	private static final int MINIMUM_PREFETCH_COUNT = 10;
	private static final int MAXIMUM_PREFETCH_COUNT = 999;
	
	static final int DEFAULT_PREFETCH_COUNT = 300;
	static final long NULL_EPOCH = 0;
	
	public static final String START_OF_STREAM = "-1";
	
	private final String partitionId;
	private final MessagingFactory underlyingFactory;
	private final String eventHubName;
	private final String consumerGroupName;
	private final Object receiveHandlerSync;

	private String startingOffset;
	private boolean offsetInclusive;
	private Instant startingDateTime;
	private MessageReceiver internalReceiver; 
	private Long epoch;
	private boolean isEpochReceiver;
	private PartitionReceiveHandler onReceiveHandler;
	private boolean isOnReceivePumpRunning;
	
	private PartitionReceiver(MessagingFactory factory, 
			final String eventHubName, 
			final String consumerGroupName, 
			final String partitionId, 
			final String startingOffset, 
			final boolean offsetInclusive,
			final Instant dateTime,
			final Long epoch,
			final boolean isEpochReceiver)
					throws ServiceBusException
	{
		super(null);
		this.underlyingFactory = factory;
		this.eventHubName = eventHubName;
		this.consumerGroupName = consumerGroupName;
		this.partitionId = partitionId;
		this.startingOffset = startingOffset;
		this.offsetInclusive = offsetInclusive;
		this.startingDateTime = dateTime;
		this.epoch = epoch;
		this.isEpochReceiver = isEpochReceiver;
		this.receiveHandlerSync = new Object();
		this.isOnReceivePumpRunning = false;
	}
	
	static CompletableFuture<PartitionReceiver> create(MessagingFactory factory, 
			final String eventHubName, 
			final String consumerGroupName, 
			final String partitionId, 
			final String startingOffset, 
			final boolean offsetInclusive,
			final Instant dateTime,
			final long epoch,
			final boolean isEpochReceiver) 
					throws ServiceBusException
	{
		if (StringUtil.isNullOrWhiteSpace(consumerGroupName))
		{
			throw new IllegalArgumentException("specify valid string for argument - 'consumerGroupName'");
		}
			
		final PartitionReceiver receiver = new PartitionReceiver(factory, eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive, dateTime, epoch, isEpochReceiver);
		return receiver.createInternalReceiver().thenApplyAsync(new Function<Void, PartitionReceiver>()
		{
			public PartitionReceiver apply(Void a)
			{
				return receiver;
			}
		});
	}
	
	private CompletableFuture<Void> createInternalReceiver() throws ServiceBusException
	{
		return MessageReceiver.create(this.underlyingFactory, StringUtil.getRandomString(), 
				String.format("%s/ConsumerGroups/%s/Partitions/%s", this.eventHubName, this.consumerGroupName, this.partitionId), 
				this.startingOffset, this.offsetInclusive, this.startingDateTime, PartitionReceiver.DEFAULT_PREFETCH_COUNT, this.epoch, this.isEpochReceiver)
				.thenAcceptAsync(new Consumer<MessageReceiver>()
				{
					public void accept(MessageReceiver r) { PartitionReceiver.this.internalReceiver = r;}
				});
	}
	
	/**
	 * @return The Cursor from which this Receiver started receiving from
	 */
	final String getStartingOffset()
	{
		return this.startingOffset;
	}
	
	final boolean getOffsetInclusive()
	{
		return this.offsetInclusive;
	}
	
	/**
	 * @return The Partition from which this Receiver is fetching data
	 */
	public final String getPartitionId()
	{
		return this.partitionId;
	}
	
	public final int getPrefetchCount()
	{
		return this.internalReceiver.getPrefetchCount();
	}
	
	/**
	 * Set the no. of events that can be pre-fetched and Cached at the {@link PartitionReceiver).
	 * By default 
	 * @param prefetchCount
	 */
	public final void setPrefetchCount(final int prefetchCount)
	{
		if (prefetchCount < PartitionReceiver.MINIMUM_PREFETCH_COUNT && prefetchCount > PartitionReceiver.MAXIMUM_PREFETCH_COUNT)
		{
			throw new IllegalArgumentException(String.format(Locale.US, 
					"PrefetchCount has to be between %s and %s", PartitionReceiver.MINIMUM_PREFETCH_COUNT, PartitionReceiver.MAXIMUM_PREFETCH_COUNT));
		}
		
		this.internalReceiver.setPrefetchCount(prefetchCount);
	}
	
	public final long getEpoch()
	{
		return this.epoch;
	}
	
	/** 
	 * Receive a batch of {@link EventData}'s from an EventHub partition
	 * @return Batch of {@link EventData}'s from the partition on which this receiver is created. returns 'null' if no {@link EventData} is present.
	 * @throws ServerBusyException
	 * @throws AuthorizationFailedException
	 * @throws InternalServerException
	 */
	public CompletableFuture<Iterable<EventData>> receive()
	{
		return this.internalReceiver.receive().thenApply(new Function<Collection<Message>, Iterable<EventData>>()
		{
			@Override
			public Iterable<EventData> apply(Collection<Message> amqpMessages)
			{
				return EventDataUtil.toEventDataCollection(amqpMessages);
			}			
		});
	}

	public void setReceiveHandler(final PartitionReceiveHandler receiveHandler)
	{
		synchronized (this.receiveHandlerSync)
		{
			this.onReceiveHandler = receiveHandler;
		}
		
		this.startOnReceivePump();
	}

	public CompletableFuture<Void> close()
	{
		this.isOnReceivePumpRunning = false;
		
		if (this.internalReceiver != null)
		{
			return this.internalReceiver.close();
		}
		else
		{
			return CompletableFuture.completedFuture(null);
		}
	}
	
	private void startOnReceivePump()
	{
		if (!this.isOnReceivePumpRunning)
		{
			synchronized (this.receiveHandlerSync)
			{
				if (!this.isOnReceivePumpRunning)
				{
					this.isOnReceivePumpRunning = true;
					
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							while(PartitionReceiver.this.isOnReceivePumpRunning)
							{
								Iterable<EventData> receivedEvents = null;
		
								try
								{
									receivedEvents = PartitionReceiver.this.receive().get(PartitionReceiver.this.underlyingFactory.getOperationTimeout().getSeconds(), TimeUnit.SECONDS);
								}
								catch (InterruptedException|ExecutionException|TimeoutException clientException)
								{
									if (clientException instanceof TimeoutException)
									{
										continue;
									}
									
									Throwable cause = clientException.getCause();
									if (cause != null && 
											((cause instanceof ServiceBusException && ((ServiceBusException) cause).getIsTransient()) ||
													!(cause instanceof RuntimeException)))
									{
										try
										{
											PartitionReceiver.this.onReceiveHandler.onError(clientException.getCause());
											continue;
										}
										catch (Throwable userCodeError)
										{
											synchronized (PartitionReceiver.this.receiveHandlerSync)
											{
												PartitionReceiver.this.isOnReceivePumpRunning = false;
											}
											
											PartitionReceiver.this.onReceiveHandler.onClose(userCodeError);
										}
									}
									else
									{
										synchronized (PartitionReceiver.this.receiveHandlerSync)
										{
											PartitionReceiver.this.isOnReceivePumpRunning = false;
										}
										
										PartitionReceiver.this.onReceiveHandler.onClose(cause);
									}
									
									if (clientException instanceof InterruptedException)
									{
										Thread.currentThread().interrupt();
									}
									
									return;
								}
								
								if (receivedEvents != null && receivedEvents.iterator().hasNext())
								{
									try
									{
										PartitionReceiver.this.onReceiveHandler.onReceive(receivedEvents);
									}
									catch (Throwable userCodeError)
									{
										synchronized (PartitionReceiver.this.receiveHandlerSync)
										{
											PartitionReceiver.this.isOnReceivePumpRunning = false;
										}
										
										PartitionReceiver.this.onReceiveHandler.onClose(userCodeError);
										return;
									}
								}
							}						
						}
					}).start();
				}
			}
		}
	}
}