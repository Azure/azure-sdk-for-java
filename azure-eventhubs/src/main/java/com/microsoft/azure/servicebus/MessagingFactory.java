/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.nio.channels.*;
import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.*;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.reactor.*;

import com.microsoft.azure.servicebus.amqp.*;

/**
 * Abstracts all amqp related details and exposes AmqpConnection object
 * Manages connection life-cycle
 */
public class MessagingFactory extends ClientEntity implements IAmqpConnection, IConnectionFactory
{
	
	public static final Duration DefaultOperationTimeout = Duration.ofSeconds(60); 
	
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	
	private final Object connectionLock = new Object();
	private final String hostName;
	
	private Reactor reactor;
	private Thread reactorThread;
	private ConnectionHandler connectionHandler;
	private Connection connection;
	private boolean waitingConnectionOpen;
	
	private Duration operationTimeout;
	private RetryPolicy retryPolicy;
	private CompletableFuture<MessagingFactory> open;
	private CompletableFuture<Connection> openConnection;
	private LinkedList<Link> registeredLinks;
	
	/**
	 * @param reactor parameter reactor is purely for testing purposes and the SDK code should always set it to null
	 */
	MessagingFactory(final ConnectionStringBuilder builder) throws IOException
	{
		super("MessagingFactory".concat(StringUtil.getRandomString()));
		this.hostName = builder.getEndpoint().getHost();
		
		this.startReactor(new ReactorHandler()
		{
			@Override
			public void onReactorFinal(Event e)
		    {
				super.onReactorFinal(e);
				MessagingFactory.this.onReactorError(new ServiceBusException(true, "Reactor finalized."));
		    }
		});
		
		this.operationTimeout = builder.getOperationTimeout();
		this.retryPolicy = builder.getRetryPolicy();
		this.registeredLinks = new LinkedList<Link>();
	}
	
	String getHostName()
	{
		return this.hostName;
	}
	
	private void createConnection(ConnectionStringBuilder builder)
	{
		assert this.reactor != null;
		
		this.connectionHandler = new ConnectionHandler(this, 
				builder.getEndpoint().getHost(), builder.getSasKeyName(), builder.getSasKey());
		this.waitingConnectionOpen = true;
		this.connection = reactor.connection(this.connectionHandler);
		this.open = new CompletableFuture<MessagingFactory>();
	}

	private void startReactor(ReactorHandler reactorHandler) throws IOException
	{
		this.reactor = Proton.reactor(reactorHandler);
		this.reactorThread = new Thread(new RunReactor(this.reactor));
		this.reactorThread.start();
	}
	
	@Override
	public CompletableFuture<Connection> getConnection()
	{
		if (this.connection.getLocalState() == EndpointState.CLOSED)
		{
			synchronized (this.connectionLock)
			{
				if (this.connection.getLocalState() == EndpointState.CLOSED 
						&& !this.waitingConnectionOpen)
				{
					this.connection.free();
					try
					{
						this.startReactor(new ReactorHandler()
						{
							@Override
							public void onReactorInit(Event e)
							{
								super.onReactorInit(e);
								
								Reactor reactor = e.getReactor();
								MessagingFactory.this.connection = reactor.connection(MessagingFactory.this.connectionHandler);
							}
							
							@Override
							public void onReactorFinal(Event e)
						    {
								super.onReactorFinal(e);
								MessagingFactory.this.onReactorError(new ServiceBusException(true, "Reactor finalized."));
						    }
						});
					}
					catch (IOException e)
					{
						MessagingFactory.this.onReactorError(new ServiceBusException(true, e));
					}
					
					this.openConnection = new CompletableFuture<Connection>();
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
		this.connection.close();
		
		Iterator<Link> literator = this.registeredLinks.iterator();
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
	
	private void onReactorError(Exception cause)
	{
		if (!this.open.isDone())
		{
			this.onOpenComplete(cause);
			return;
		}
		
		if (this.connection != null)
		{
			this.connection.close();
		}
		
		Iterator<Link> literator = this.registeredLinks.iterator();
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
	
	void resetConnection()
	{
		this.onReactorError(new ServiceBusException(false, "Client invoked connection reset."));
	}
	
	public void closeSync()
	{
		if (this.connection != null)
		{
			if (this.connection.getLocalState() != EndpointState.CLOSED)
			{
				this.connection.close();
			}
			
			this.connection.free();
		}
	}

	@Override
	public CompletableFuture<Void> close()
	{
		this.close();
		
		// hook up onRemoteClose & timeout 
		return CompletableFuture.completedFuture(null);
	}

	public class RunReactor implements Runnable
	{
		private Reactor r;
		
		public RunReactor(Reactor r)
		{
			this.r = r;
		}
		
		public void run()
		{
			if(TRACE_LOGGER.isLoggable(Level.FINE))
		    {
				TRACE_LOGGER.log(Level.FINE, "starting reactor instance.");
		    }
			
			try
			{
				this.r.run();
			}
			catch (HandlerException handlerException)
			{
				Exception cause = handlerException;
				
				if(TRACE_LOGGER.isLoggable(Level.WARNING))
			    {
					TRACE_LOGGER.log(Level.WARNING, "UnHandled exception while processing events in reactor:");
					TRACE_LOGGER.log(Level.WARNING, handlerException.getMessage());
					if (handlerException.getStackTrace() != null)
						for (StackTraceElement ste: handlerException.getStackTrace())
						{
							TRACE_LOGGER.log(Level.WARNING, ste.toString());
						}
			    }
				
				MessagingFactory.this.onReactorError(cause);
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
}
