/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.reactor.Reactor;

import com.microsoft.azure.servicebus.amqp.BaseLinkHandler;
import com.microsoft.azure.servicebus.amqp.ConnectionHandler;
import com.microsoft.azure.servicebus.amqp.IAmqpConnection;
import com.microsoft.azure.servicebus.amqp.ProtonUtil;
import com.microsoft.azure.servicebus.amqp.ReactorHandler;

/**
 * Abstracts all amqp related details and exposes AmqpConnection object
 * Manages connection life-cycle
 */
public class MessagingFactory extends ClientEntity implements IAmqpConnection, IConnectionFactory, ITimeoutErrorHandler
{
	public static final Duration DefaultOperationTimeout = Duration.ofSeconds(60); 
	
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	private static final int TIMEOUT_ERROR_THRESHOLD_IN_SECS = 100000;
	private final Object connectionLock = new Object();
	private final String hostName;
	private final CompletableFuture<Void> closeTask;
	private final ConnectionHandler connectionHandler;
	private final ReactorHandler reactorHandler;
	
	private Reactor reactor;
	private Thread reactorThread;
	private Connection connection;
	private boolean waitingConnectionOpen;
	
	private Duration operationTimeout;
	private RetryPolicy retryPolicy;
	private CompletableFuture<MessagingFactory> open;
	private CompletableFuture<Connection> openConnection;
	private LinkedList<Link> registeredLinks;
	private TimeoutTracker connectionCreateTracker;
	private Instant timeoutErrorStart;
	private Object resetConnectionSync;
	
	/**
	 * @param reactor parameter reactor is purely for testing purposes and the SDK code should always set it to null
	 */
	MessagingFactory(final ConnectionStringBuilder builder)
	{
		super("MessagingFactory".concat(StringUtil.getRandomString()), null);
		
		Timer.register(this.getClientId());
		this.hostName = builder.getEndpoint().getHost();
		this.timeoutErrorStart = Instant.MAX;
		this.operationTimeout = builder.getOperationTimeout();
		this.retryPolicy = builder.getRetryPolicy();
		this.registeredLinks = new LinkedList<Link>();
		this.resetConnectionSync = new Object();
		this.closeTask = new CompletableFuture<Void>();
		this.connectionHandler = new ConnectionHandler(this, 
				builder.getEndpoint().getHost(), builder.getSasKeyName(), builder.getSasKey());
		
		this.reactorHandler = new ReactorHandler()
		{
			@Override
			public void onReactorInit(Event e)
			{
				super.onReactorInit(e);
				
				final Reactor reactor = e.getReactor();
				MessagingFactory.this.connection = reactor.connection(MessagingFactory.this.connectionHandler);
			}
		};
	}
	
	String getHostName()
	{
		return this.hostName;
	}
	
	private void createConnection(ConnectionStringBuilder builder) throws IOException
	{
		this.open = new CompletableFuture<MessagingFactory>();
		this.waitingConnectionOpen = true;
		this.startReactor(this.reactorHandler);
	}

	private void startReactor(ReactorHandler reactorHandler) throws IOException
	{
		this.reactor = ProtonUtil.reactor(reactorHandler);
		this.reactorThread = new Thread(new RunReactor(this.reactor));
		this.reactorThread.start();
	}
	
	@Override
	public CompletableFuture<Connection> getConnection()
	{
		if (this.connection.getLocalState() == EndpointState.CLOSED
				|| (this.connectionCreateTracker != null && !this.connectionCreateTracker.remaining().minus(ClientConstants.TIMER_TOLERANCE).isNegative()))
		{
			synchronized (this.connectionLock)
			{
				if ((this.connection.getLocalState() == EndpointState.CLOSED && !this.waitingConnectionOpen)
						|| (this.connectionCreateTracker != null && !this.connectionCreateTracker.remaining().minus(ClientConstants.TIMER_TOLERANCE).isNegative()))
				{
					try
					{
						this.startReactor(this.reactorHandler);
					}
					catch (IOException e)
					{
						MessagingFactory.this.onReactorError(new ServiceBusException(true, e));
					}
					
					if(this.openConnection != null && !this.openConnection.isDone())
					{
						this.openConnection.completeExceptionally(new TimeoutException(String.format(Locale.US, "Connection creation timedout, %s", ExceptionUtil.getTrackingIDAndTimeToLog())));
					}

					this.openConnection = new CompletableFuture<Connection>();
					
					this.connectionCreateTracker = TimeoutTracker.create(this.operationTimeout);
					this.waitingConnectionOpen = true;
				}
			}
		}
		
		return this.openConnection == null ? CompletableFuture.completedFuture(this.connection): this.openConnection;
	}
	
	public Duration getOperationTimeout()
	{
		return this.operationTimeout;
	}
	
	public RetryPolicy getRetryPolicy()
	{
		return this.retryPolicy;
	}
	
	public static CompletableFuture<MessagingFactory> createFromConnectionString(final String connectionString) throws IOException
	{
		ConnectionStringBuilder builder = new ConnectionStringBuilder(connectionString);
		MessagingFactory messagingFactory = new MessagingFactory(builder);
		
		messagingFactory.createConnection(builder);
		return messagingFactory.open;
	}
	
	@Override
	public void onOpenComplete(Exception exception)
	{
		synchronized (this.connectionLock)
		{
			this.waitingConnectionOpen = false;
		}
		
		if (exception == null)
		{
			this.open.complete(this);
			if(this.openConnection != null)
			{
				this.openConnection.complete(this.connection);
			}
		}
		else
		{
			this.open.completeExceptionally(exception);
			if (this.openConnection != null)
			{
				this.openConnection.completeExceptionally(exception);
			}
		}
	}
	
	@Override
	public void onConnectionError(ErrorCondition error)
	{
		if (this.reactorThread != null && !this.reactorThread.isInterrupted())
		{
			this.reactorThread.interrupt();
		}
		
		if (!this.open.isDone())
		{
			this.onOpenComplete(ExceptionUtil.toException(error));
		}
		else
		{
			final Connection currentConnection = this.connection;
			Iterator<Link> literator = this.registeredLinks.iterator();
			
			while (literator.hasNext())
			{
				Link link = literator.next();
				if (link.getLocalState() != EndpointState.CLOSED)
				{
					link.close();
				}
			}
			
			if (currentConnection.getLocalState() != EndpointState.CLOSED)
			{
				currentConnection.close();
			}
			
			literator = this.registeredLinks.iterator();
			while (literator.hasNext())
			{
				Link link = literator.next();
				Handler handler = BaseHandler.getHandler(link);
				if (handler != null && handler instanceof BaseLinkHandler)
				{
					BaseLinkHandler linkHandler = (BaseLinkHandler) handler;
					linkHandler.processOnClose(link, error);
				}
			}
		}
		
		if (this.getIsClosingOrClosed() && !this.closeTask.isDone())
		{
			this.closeTask.complete(null);
			Timer.unregister(this.getClientId());
		}
	}
	
	private void onReactorError(Exception cause)
	{
		if (!this.open.isDone())
		{
			this.onOpenComplete(cause);
		}
		else
		{
			final Connection currentConnection = this.connection;
			
			Iterator<Link> literator = this.registeredLinks.iterator();
			while (literator.hasNext())
			{
				Link link = literator.next();
				if (link.getLocalState() != EndpointState.CLOSED)
				{
					link.close();
				}
			}
			
			if (currentConnection.getLocalState() != EndpointState.CLOSED)
			{
				currentConnection.close();
			}

			literator = this.registeredLinks.iterator();
			while (literator.hasNext())
			{
				Link link = literator.next();
				Handler handler = BaseHandler.getHandler(link);
				if (handler != null && handler instanceof BaseLinkHandler)
				{
					BaseLinkHandler linkHandler = (BaseLinkHandler) handler;
					linkHandler.processOnClose(link, cause);
				}
			}
		}
	}
	
	void resetConnection()
	{		
		this.reactor.free();
		this.onReactorError(new ServiceBusException(true, String.format(Locale.US, "Client invoked connection reset, %s", ExceptionUtil.getTrackingIDAndTimeToLog())));
	}
	
	@Override
	protected CompletableFuture<Void> onClose()
	{
		if (!this.getIsClosed())
		{
			if (this.connection != null && this.connection.getRemoteState() != EndpointState.CLOSED)
			{
				if (this.connection.getLocalState() != EndpointState.CLOSED)
				{
					this.connection.close();
				}
				
				Timer.schedule(new Runnable()
				{
					@Override
					public void run()
					{
						if (!MessagingFactory.this.closeTask.isDone())
						{
							MessagingFactory.this.closeTask.completeExceptionally(new TimeoutException("Closing MessagingFactory timed out."));
						}
					}
				},
				this.operationTimeout, TimerType.OneTimeRun);
			}
		}		
		
		return this.closeTask;
	}

	private class RunReactor implements Runnable
	{
		final private Reactor rctr;
		
		public RunReactor(final Reactor reactor)
		{
			this.rctr = reactor;
		}
		
		public void run()
		{
			if(TRACE_LOGGER.isLoggable(Level.FINE))
		    {
				TRACE_LOGGER.log(Level.FINE, "starting reactor instance.");
		    }
			
			try
			{
				this.rctr.setTimeout(3141);
		        this.rctr.start();
		        while(!Thread.interrupted() && this.rctr.process()) {}
		        this.rctr.stop();
			}
			catch (HandlerException handlerException)
			{
				Exception cause = handlerException;
				
				if(TRACE_LOGGER.isLoggable(Level.WARNING))
			    {
					StringBuilder builder = new StringBuilder();
					builder.append("UnHandled exception while processing events in reactor:");
					builder.append(System.lineSeparator());
					builder.append(handlerException.getMessage());
					if (handlerException.getStackTrace() != null)
						for (StackTraceElement ste: handlerException.getStackTrace())
						{
							builder.append(System.lineSeparator());
							builder.append(ste.toString());
						}
					
					Throwable innerException = handlerException.getCause();
					if (innerException != null)
					{
						builder.append("Cause: " + innerException.getMessage());
						if (innerException.getStackTrace() != null)
							for (StackTraceElement ste: innerException.getStackTrace())
							{
								builder.append(System.lineSeparator());
								builder.append(ste.toString());
							}
					}
					
					TRACE_LOGGER.log(Level.WARNING, builder.toString());
			    }
				
				MessagingFactory.this.onReactorError(new ServiceBusException(
						true,
						String.format(Locale.US, "%s, %s", StringUtil.isNullOrEmpty(cause.getMessage()) ? "Reactor encountered unrecoverable error" : cause.getMessage(), ExceptionUtil.getTrackingIDAndTimeToLog()),
						cause));
			}
			finally
			{
				this.rctr.free();
			}
		}
	}

	@Override
	public void registerForConnectionError(Link link)
	{
		this.registeredLinks.add(link);	
	}

	@Override
	public void deregisterForConnectionError(Link link)
	{
		this.registeredLinks.remove(link);	
	}

	@Override
	public void reportTimeoutError()
	{
		if (this.timeoutErrorStart.equals(Instant.MAX))
		{
			this.timeoutErrorStart = Instant.now();
		}
		else if (this.timeoutErrorStart.isBefore(Instant.now().minus(TIMEOUT_ERROR_THRESHOLD_IN_SECS, ChronoUnit.SECONDS)))
		{
			synchronized (this.resetConnectionSync)
			{
				if (this.timeoutErrorStart.isBefore(Instant.now().minus(TIMEOUT_ERROR_THRESHOLD_IN_SECS, ChronoUnit.SECONDS)))
				{
					this.resetTimeoutErrorTracking();
					this.resetConnection();
				}
			}
		}
	}

	@Override
	public void resetTimeoutErrorTracking()
	{
		this.timeoutErrorStart = Instant.MAX;
	}	
}
