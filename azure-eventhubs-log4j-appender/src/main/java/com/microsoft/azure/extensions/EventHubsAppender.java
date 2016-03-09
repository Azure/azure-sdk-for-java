/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.extensions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.*;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.*;
import org.apache.logging.log4j.core.util.StringEncoder;

/**
 * Sends {@link LogEvent}'s to Microsoft Azure EventHubs.
 * By default, tuned for high performance and hence, pushes a batch of Events. 
 */
@Plugin(name = "EventHub", category = "Core" , elementType = "appender", printObject = true)
public final class EventHubsAppender extends AbstractAppender
{
	private static final int MAX_BATCH_SIZE_BYTES = 200 * 1024;
	
	// this constant is tuned to use the MaximumAllowedMessageSize(256K) including Amqp-Headers for a LogEvent of 1Char
	private static final int MAX_BATCH_SIZE = 21312;
	private static final long serialVersionUID = 1L;
	
	private final EventHubsManager eventHubsManager;
	private final boolean immediateFlush;
	private final AtomicInteger currentBufferedSizeBytes;
	private final ConcurrentLinkedQueue<byte[]> logEvents;
	
	private EventHubsAppender(
			final String name,
			final Filter filter,
			final Layout<? extends Serializable> layout,
			final boolean ignoreExceptions,
			final EventHubsManager eventHubsManager,
			final boolean immediateFlush)
	{
		super(name, filter, layout, ignoreExceptions);

		this.eventHubsManager = eventHubsManager;
		this.immediateFlush = immediateFlush;
		this.logEvents = new ConcurrentLinkedQueue<byte[]>();
		this.currentBufferedSizeBytes = new AtomicInteger();
	}
	
	@PluginFactory
	public static EventHubsAppender createAppender(
			@Required(message = "Provide a Name for EventHubs Log4j Appender") @PluginAttribute("name") final String name,
			@PluginElement("Filter") final Filter filter,
			@PluginElement("Layout") final Layout<? extends Serializable> layout,
			@PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions,
			@Required(message = "Provide EventHub connection string to append the events to") @PluginAttribute("eventHubConnectionString") final String connectionString,
			@PluginAttribute(value = "immediateFlush", defaultBoolean = false) final boolean immediateFlush)
	{
		final EventHubsManager eventHubsManager = new EventHubsManager(name, connectionString);
		return new EventHubsAppender(name, filter, layout, ignoreExceptions, eventHubsManager, immediateFlush);
	}

	@Override
	public void append(LogEvent logEvent)
	{
		byte[] serializedLogEvent = null;
        
		try
		{
			Layout<? extends Serializable> layout = getLayout();
            
			if (layout != null)
			{
				serializedLogEvent = layout.toByteArray(logEvent);
			}
			else
			{
				serializedLogEvent = StringEncoder.toBytes(logEvent.getMessage().getFormattedMessage(), StandardCharsets.UTF_8);
			}

			if (serializedLogEvent != null)
			{
				if (this.immediateFlush)
				{
					this.eventHubsManager.send(serializedLogEvent);
					return;
				}
				else
				{
					int currentSize = this.currentBufferedSizeBytes.addAndGet(serializedLogEvent.length);
					this.logEvents.offer(serializedLogEvent);

					if (currentSize < EventHubsAppender.MAX_BATCH_SIZE_BYTES 
							&& this.logEvents.size() < EventHubsAppender.MAX_BATCH_SIZE
							&& !logEvent.isEndOfBatch())
					{
						return;
					}

					logEvent.setEndOfBatch(true);

					this.eventHubsManager.send(this.logEvents);

					this.logEvents.clear();
					this.currentBufferedSizeBytes.set(0);
				}
			}
		}
		catch (final Throwable exception)
		{
			AppenderLoggingException appenderLoggingException = exception instanceof AppenderLoggingException
					? (AppenderLoggingException) exception
					: new AppenderLoggingException("Appending logEvent to EventHubs failed: " + exception.getMessage(), exception);
			
			LOGGER.error(String.format(Locale.US, "[%s] Appender failed to logEvent to EventHub.", this.getName()));
			
			// to avoid replay
			if (this.logEvents.remove(logEvent) && serializedLogEvent != null)
			{
				this.currentBufferedSizeBytes.addAndGet(-1 * serializedLogEvent.length);
			}
			
			throw appenderLoggingException;
		}
	};

	@Override
	public void start()
	{
		super.start();

		try
		{
			this.eventHubsManager.startup();
		}
		catch(Throwable exception)
		{
			final String errMsg = String.format(Locale.US, "[%s] Appender initialization failed with error: [%s]", this.getName(), exception.getMessage());
			
			LOGGER.error(errMsg);
			throw new AppenderLoggingException(errMsg, exception);
		}
	}
	
	@Override
	public void stop()
	{
		super.stop();
		this.eventHubsManager.release();
	}
}