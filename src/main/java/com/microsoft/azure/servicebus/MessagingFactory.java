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
	
	MessagingFactory(ConnectionStringBuilder builder) {
		startReactor();
		this.connection = getConnection(builder);
	}
	
	private Connection getConnection(ConnectionStringBuilder builder){
		ConnectionHandler handler = new ConnectionHandler(builder.getHostName(), builder.getSasKeyName(), builder.getSasKey());
		return reactor.connection(handler);
	}

	private void startReactor()
	{
		if (reactor == null) {
			try {
				reactor = Proton.reactor();
				new Thread(new RunReactor(reactor)).start();
			}catch(IOException ioException){
				// TODO: throw - cannot start Reactor
			}
		}
	}
	
	Connection getConnection() {
		return this.connection;
	}
	
	public static MessagingFactory createFromConnectionString(final String connectionString) {
		ConnectionStringBuilder builder = new ConnectionStringBuilder(connectionString);
		return new MessagingFactory(builder);
	}
	
	public static class RunReactor implements Runnable {
		private Reactor r;
		
		public RunReactor(Reactor r) {
			this.r = r;
		}
		
		public void run() {
			this.r.run();

			if(TRACE_LOGGER.isLoggable(Level.FINE))
		    {
				TRACE_LOGGER.log(Level.FINE, "running instance started...");
		    }
		}
	}
}
