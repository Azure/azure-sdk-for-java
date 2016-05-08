/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.extensions.batchSend;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.servicebus.amqp.AmqpConstants;

/**
 * Wraps up {@link EventHubClient} send APIs to provide Batching semantics.
 */
public class BatchSender {

	public final static int BATCH_FLUSH_INTERVAL_MS = 20;
	public final static int MAX_BATCH_SIZE = 5100;
	public final static int MAX_MSG_SIZE = 210000;
	public final static String NO_PARTITION_KEY = "---NO-PARTITION-KEY---";
	
	private final static Logger logger = Logger.getLogger(BatchSender.class.getName());
	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
	
	private final ISender sender;
	private final ConcurrentHashMap<String, ConcurrentLinkedQueue<SendWork>> pendingSends;

	private BatchSender(final ISender sender, final int batchFlushInterval) {
		this.sender = sender;
		this.pendingSends = new ConcurrentHashMap<String, ConcurrentLinkedQueue<SendWork>>();
		this.pendingSends.put(NO_PARTITION_KEY, new ConcurrentLinkedQueue<SendWork>());
		scheduler.scheduleWithFixedDelay(new Sender(), 0, batchFlushInterval, TimeUnit.MILLISECONDS);
	}
	
	public static BatchSender create(final EventHubClient eventHubClient) {
		return new BatchSender(new EventHubClientSender(eventHubClient), BATCH_FLUSH_INTERVAL_MS);
	}
	
	public static BatchSender create(final EventHubClient eventHubClient, final int batchFlushInterval) {
		return new BatchSender(new EventHubClientSender(eventHubClient), batchFlushInterval);
	}
	
	public CompletableFuture<Void> send(final EventData edata) {
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		this.pendingSends.get(NO_PARTITION_KEY).offer(new SendWork(edata, future));
		return future;
	}
	
	/**
	 * BatchSending using this {@link BatchSender#send(EventData, String)} will be useful only if the occurrence of a single partitionKey is high.
	 * If the chance of the {@link EventData}'s landing on the same PartitionKey within the batchFlushInterval is very less - using
	 * {@link BatchSender#send(EventData, String)} to send adds an extra overhead and is not recommended.
	 * <p>
	 * known limitations:
	 * <ul>
	 * <li>{@link BatchSender#send(EventData, String)} doesn't work with this specific partitionKey value: {@link BatchSender#NO_PARTITION_KEY}
	 * <li>Using random non-repetitive partitionKey's will result in a memoryLeak - as the PartitonKey cache is never cleared - as this is not meant to be used in such a case.
	 * </ul>
	 * @param edata EventData to send to
	 * @param partitionKey partitionKey
	 * @return
	 */
	public CompletableFuture<Void> send(final EventData edata, final String partitionKey) {
		CompletableFuture<Void> future = new CompletableFuture<Void>();

		ConcurrentLinkedQueue<SendWork> pendingSendsPerPartitionKey = this.pendingSends.computeIfAbsent(partitionKey, new Function<String, ConcurrentLinkedQueue<SendWork>>(){
			@Override
			public ConcurrentLinkedQueue<SendWork> apply(String t) {
				return new ConcurrentLinkedQueue<SendWork>();
			}});
		
		pendingSendsPerPartitionKey.offer(new SendWork(edata, future));
		return future;
	}
	
	private class Sender implements Runnable {

		@Override
		public void run() {
			
			for (Map.Entry<String, ConcurrentLinkedQueue<SendWork>> partitionKeyWork: pendingSends.entrySet()) {
				
				final String partitionKey = partitionKeyWork.getKey();
				final ConcurrentLinkedQueue<SendWork> pendingSendsPerPKey = partitionKeyWork.getValue();
				
				final LinkedList<EventData> events = new LinkedList<EventData>();
				final LinkedList<CompletableFuture<Void>> futures = new LinkedList<CompletableFuture<Void>>();
	
				int batchSize = 0;
				int aggregatedSize = 0;
				while ((pendingSendsPerPKey).peek() != null
						&& batchSize <= MAX_BATCH_SIZE 
						&& (aggregatedSize + pendingSendsPerPKey.peek().getEventData().getBody().length) < MAX_MSG_SIZE)
				{
					SendWork work = pendingSendsPerPKey.poll();
					events.add(work.getEventData());
					futures.add(work.getSendFuture());
					batchSize++;
					aggregatedSize += work.getEventData().getBody().length;
					Map<String, String> properties = work.getEventData().getProperties();
					if (properties != null)
					{
						for (Map.Entry<String, String> property : properties.entrySet())
						{
							aggregatedSize += ((property.getKey().length() + property.getValue().length()) * 2); 
						}
					}
					
					if (partitionKey.compareTo(NO_PARTITION_KEY) != 0) {
						aggregatedSize += (AmqpConstants.PARTITION_KEY.toString().length() * 2);
						aggregatedSize += (partitionKey.length() * 2);
					}
				}
				
				if(!events.isEmpty())
				{
					CompletableFuture<Void> realSend = (partitionKey.compareTo(NO_PARTITION_KEY) == 0)
							? sender.send(events) 
							: sender.send(events, partitionKey);

					if (logger.isLoggable(Level.FINE))
						logger.log(Level.FINE, String.format(Locale.US, "Sending batchSize: %s, total messages Size: %s, partitionKey: %s", batchSize, aggregatedSize, partitionKey));
					
					realSend
					.thenApplyAsync(new Function<Void, Void>() {
						@Override
						public Void apply(Void t) {
							for (CompletableFuture<Void> work: futures)
								work.complete(t);
							return null;
						}})
					.exceptionally(new Function<Throwable, Void>() {
						@Override
						public Void apply(Throwable t) {
							for (CompletableFuture<Void> work: futures)
								work.completeExceptionally(t);
							return null;
						}});
				}
			}
		}
		
	}
	
	private static class SendWork {
		
		private final EventData eventData;
		private final CompletableFuture<Void> pendingSend;
		
		private SendWork(final EventData edata, final CompletableFuture<Void> pendingSend) {
			this.eventData = edata;
			this.pendingSend = pendingSend;
		}
		
		public EventData getEventData() {
			return this.eventData;
		}
		
		public CompletableFuture<Void> getSendFuture() {
			return this.pendingSend;
		}
		
	}
	
}

