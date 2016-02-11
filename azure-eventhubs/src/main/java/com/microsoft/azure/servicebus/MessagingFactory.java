package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.nio.channels.*;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.*;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.reactor.*;

import com.microsoft.azure.servicebus.amqp.ConnectionHandler;
import com.microsoft.azure.servicebus.amqp.ReactorHandler;

/**
 * Abstracts all amqp related details and exposes AmqpConnection object
 * Manage reconnect
 * TODO: Bring all Create's here - so that it can manage recreate/close scenario's
 */
public class MessagingFactory extends ClientEntity
{
	
	public static final Duration DefaultOperationTimeout = Duration.ofSeconds(60); 
	
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.ServiceBusClientTrace);
	
	private Reactor reactor;
	private Thread reactorThread;
	private final Object reactorLock = new Object();
	
	private ConnectionHandler connectionHandler;
	private Connection connection;
	private boolean waitingConnectionOpen;
	
	private Duration operationTimeout;
	private RetryPolicy retryPolicy;
	private CompletableFuture<MessagingFactory> open;
	
	/**
	 * @param reactor parameter reactor is purely for testing purposes and the SDK code should always set it to null
	 */
	MessagingFactory(final ConnectionStringBuilder builder) throws IOException
	{
		super("MessagingFactory" + UUID.randomUUID().toString());
		
		this.startReactor();
		
		this.operationTimeout = builder.getOperationTimeout();
		this.retryPolicy = builder.getRetryPolicy();
	}
	
	private void createConnection(ConnectionStringBuilder builder)
	{
		synchronized (this.reactorLock)
		{
			assert this.reactor != null;
			
			ConnectionHandler connectionHandler = new ConnectionHandler(this, builder.getEndpoint().getHost(), builder.getSasKeyName(), builder.getSasKey());
			this.waitingConnectionOpen = true;
			this.connection = reactor.connection(connectionHandler);
			this.open = new CompletableFuture<MessagingFactory>();
		}
	}

	private void startReactor() throws IOException
	{
		synchronized (this.reactorLock)
		{
			if (this.reactor == null)
			{
				this.reactor = Proton.reactor(new ReactorHandler());
				
				this.reactorThread = new Thread(new RunReactor(this, this.reactor));
				this.reactorThread.start();
			}
		}
	}
	
	// Todo: async
	Connection getConnection()
	{
		if (this.connection.getLocalState() != EndpointState.ACTIVE)
		{
			synchronized (this.connection)
			{
				if (this.connection.getLocalState() != EndpointState.ACTIVE && 
						this.connection.getLocalState() != EndpointState.UNINITIALIZED && 
						!this.waitingConnectionOpen)
				{
					this.connection.free();
					this.connection = reactor.connection(connectionHandler);
					this.waitingConnectionOpen = true;
				}
			}
		}
		
		return this.connection;
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
	
	// Contract: ConnectionHandler - MessagingFactory
	public void onOpenComplete(Exception exception)
	{
		synchronized (this.connection)
		{
			this.waitingConnectionOpen = false;
			if (exception == null)
			{
				this.open.complete(this);
			}
			else
			{
				this.open.completeExceptionally(exception);
			}
		}
	}

	public void close()
	{
		if (this.connection != null && this.connection.getLocalState() != EndpointState.CLOSED)
		{
			this.connection.close();
			this.connection.free();
		}
	}

	@Override
	public CompletableFuture<Void> closeAsync()
	{
		return null;
	}

	public static class RunReactor implements Runnable
	{
		private Reactor r;
		private MessagingFactory messagingFactory;
		
		public RunReactor(MessagingFactory owner, Reactor r)
		{
			this.r = r;
			this.messagingFactory = owner;
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
				if (handlerException.getCause() != null && handlerException.getCause() instanceof UnresolvedAddressException)
				{
					UnresolvedAddressException unresolvedAddressException = (UnresolvedAddressException) handlerException.getCause();
					this.messagingFactory.onOpenComplete(unresolvedAddressException);
					return;
				}
				
				if(TRACE_LOGGER.isLoggable(Level.WARNING))
			    {
					TRACE_LOGGER.log(Level.WARNING, "UnHandled exception while processing events in reactor: " + handlerException.toString());
					handlerException.printStackTrace();
			    }
			}
		}
	}
}
