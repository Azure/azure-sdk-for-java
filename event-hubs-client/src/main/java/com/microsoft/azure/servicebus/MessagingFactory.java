package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.logging.*;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.reactor.*;

import com.microsoft.azure.servicebus.amqp.ConnectionHandler;

/**
 * Abstracts all amqp related details and exposes AmqpConnection object
 * Manage reconnect
 * TODO: Bring all Create's here - so that it can manage recreate/close scenario's
 */
public class MessagingFactory extends ClientEntity
{
	
	public static final Duration DefaultOperationTimeout = Duration.ofSeconds(60); 
	
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.ServiceBusClientTrace);
	private static final Object reactorLock = new Object();
	
	// TODO: maintain refCount for reactor and close it if all MessagingFactory instances are closed
	private static Reactor reactor;
	
	private ConnectionHandler connectionHandler;
	private Connection connection;
	private boolean waitingConnectionOpen;
	
	private Duration operationTimeout;
	private RetryPolicy retryPolicy;
	
	/**
	 * @param reactor parameter reactor is purely for testing purposes and the SDK code should always set it to null
	 */
	MessagingFactory(final ConnectionStringBuilder builder, final Reactor reactor) throws IOException
	{
		super("MessagingFactory" + UUID.randomUUID().toString());
		
		if (reactor == null)
			this.startReactor();
		else
		{
			synchronized (MessagingFactory.reactorLock)
			{
				if (MessagingFactory.reactor == null)
					MessagingFactory.reactor = reactor;
			}
		}
		
		this.connection = this.createConnection(builder);
		this.operationTimeout = builder.getOperationTimeout();
		this.retryPolicy = builder.getRetryPolicy();
	}
	
	private Connection createConnection(ConnectionStringBuilder builder)
	{
		synchronized (MessagingFactory.reactorLock)
		{
			assert MessagingFactory.reactor != null;
			
			ConnectionHandler connectionHandler = new ConnectionHandler(this, builder.getEndpoint().getHost(), builder.getSasKeyName(), builder.getSasKey());
			this.waitingConnectionOpen = true;
			return reactor.connection(connectionHandler);
		}
	}

	private void startReactor() throws IOException
	{
		synchronized (MessagingFactory.reactorLock)
		{
			if (MessagingFactory.reactor == null)
			{
				MessagingFactory.reactor = Proton.reactor();
				new Thread(new RunReactor(MessagingFactory.reactor)).start();
			}
		}
	}
	
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
	
	public static MessagingFactory createFromConnectionString(final String connectionString) throws IOException
	{
		ConnectionStringBuilder builder = new ConnectionStringBuilder(connectionString);
		return new MessagingFactory(builder, null);
	}
	
	public static MessagingFactory create(final ConnectionStringBuilder connectionStringBuilder, Reactor reactor) throws IOException
	{
		return new MessagingFactory(connectionStringBuilder, reactor);
	}
	
	// Contract: ConnectionHandler - MessagingFactory
	public void onOpenComplete()
	{
		synchronized (this.connection)
		{
			this.waitingConnectionOpen = false;
		}
	}
	
	public static class RunReactor implements Runnable
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
			
			this.r.run();
		}
	}

	@Override
	public void close()
	{
		// TODO: Close all dependent links
		this.connection.close();
		this.connection.free();		
	}

}
