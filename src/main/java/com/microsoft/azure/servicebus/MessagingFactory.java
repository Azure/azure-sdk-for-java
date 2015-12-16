package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.reactor.*;

/**
 * Abstracts all amqp related details and exposes AmqpConnection object
 * TODO: Manage reconnect?
 */
public class MessagingFactory {
	
	public static final Duration DefaultOperationTimeout = Duration.ofSeconds(60); 
	
	private static final Logger TRACE_LOGGER = Logger.getLogger("eventhub.trace");
	
	private static Reactor reactor;
	
	private final Connection connection;
	
	private Duration operationTimeout;
	private RetryPolicy retryPolicy;
	
	MessagingFactory(ConnectionStringBuilder builder) throws IOException {
		startReactor();
		this.connection = getConnection(builder);
		this.operationTimeout = builder.getOperationTimeout();
		this.retryPolicy = builder.getRetryPolicy();
	}
	
	private Connection getConnection(ConnectionStringBuilder builder){
		ConnectionHandler handler = new ConnectionHandler(builder.getEndpoint().getHost(), builder.getSasKeyName(), builder.getSasKey());
		return reactor.connection(handler);
	}

	private void startReactor() throws IOException
	{
		if (reactor == null) {
			reactor = Proton.reactor();
			new Thread(new RunReactor(reactor)).start();
		}
	}
	
	Connection getConnection() {
		return this.connection;
	}
	
	public Duration getOperationTimeout() {
		return this.operationTimeout;
	}
	
	public static MessagingFactory createFromConnectionString(final String connectionString) throws IOException {
		ConnectionStringBuilder builder = new ConnectionStringBuilder(connectionString);
		return new MessagingFactory(builder);
	}
	
	public static class RunReactor implements Runnable {
		private Reactor r;
		
		public RunReactor(Reactor r) {
			this.r = r;
		}
		
		public void run() {

			if(TRACE_LOGGER.isLoggable(Level.FINE))
		    {
				TRACE_LOGGER.log(Level.FINE, "starting reactor instance.");
		    }
			
			this.r.run();
		}
	}
}
