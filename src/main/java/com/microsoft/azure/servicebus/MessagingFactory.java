package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.reactor.*;

import com.microsoft.azure.servicebus.amqp.ConnectionHandler;

/**
 * Abstracts all amqp related details and exposes AmqpConnection object
 * TODO: Manage reconnect
 * TODO: Bring all Create's here - so that it can manage recreate/close scenario's
 */
public class MessagingFactory extends ClientEntity
{
	
	public static final Duration DefaultOperationTimeout = Duration.ofSeconds(60); 
	
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.ServiceBusClientTrace);
	
	// TODO: maintain refCount for reactor and close it if all MessagingFactory instances are closed
	private static Reactor reactor;
	
	private final Connection connection;
	
	private Duration operationTimeout;
	private RetryPolicy retryPolicy;
	
	MessagingFactory(final ConnectionStringBuilder builder, final Reactor reactor) throws IOException
	{
		super("MessagingFactory" + UUID.randomUUID().toString());
		
		if (reactor == null)
			this.startReactor();
		else if (MessagingFactory.reactor == null)
			MessagingFactory.reactor = reactor;
		/* else if (MessagingFactory.reactor != reactor)
			throw new IllegalArgumentException("argument 'reactor' is unexpected"); */
		
		this.connection = createConnection(builder);
		this.operationTimeout = builder.getOperationTimeout();
		this.retryPolicy = builder.getRetryPolicy();
	}
	
	private Connection createConnection(ConnectionStringBuilder builder)
	{
		ConnectionHandler handler = new ConnectionHandler(builder.getEndpoint().getHost(), builder.getSasKeyName(), builder.getSasKey());
		return reactor.connection(handler);
	}

	private void startReactor() throws IOException
	{
		if (reactor == null)
		{
			reactor = Proton.reactor();
			new Thread(new RunReactor(reactor)).start();
		}
	}
	
	Connection getConnection()
	{
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
