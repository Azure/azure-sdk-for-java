package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.nio.channels.*;
import java.time.Duration;
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
 * Manage reconnect
 * TODO: Bring all Create's here - so that it can manage recreate/close scenario's
 */
public class MessagingFactory extends ClientEntity
{
	
	public static final Duration DefaultOperationTimeout = Duration.ofSeconds(60); 
	
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.ServiceBusClientTrace);
	
	private Reactor reactor;
	private Thread reactorThread;
	private final Object connectionLock = new Object();
	
	private ConnectionHandler connectionHandler;
	private Connection connection;
	private boolean waitingConnectionOpen;
	
	private Duration operationTimeout;
	private RetryPolicy retryPolicy;
	private CompletableFuture<MessagingFactory> open;
	private CompletableFuture<Connection> openConnection;
	
	public LinkedList<Link> links;
	
	/**
	 * @param reactor parameter reactor is purely for testing purposes and the SDK code should always set it to null
	 */
	MessagingFactory(final ConnectionStringBuilder builder) throws IOException
	{
		super("MessagingFactory" + UUID.randomUUID().toString());
		
		this.startReactor(new ReactorHandler());
		
		this.operationTimeout = builder.getOperationTimeout();
		this.retryPolicy = builder.getRetryPolicy();
		this.links = new LinkedList<Link>();
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
		this.reactorThread = new Thread(new RunReactor(this, this.reactor));
		this.reactorThread.start();
	}
	
	// Todo: async
	Connection getConnection()
	{
		if (this.connection.getLocalState() == EndpointState.CLOSED)
		{
			synchronized (this.connectionLock)
			{
				if (this.connection.getLocalState() == EndpointState.CLOSED 
						&& !this.waitingConnectionOpen)
				{
					this.connection.free();
					try {
						this.startReactor(new ReactorHandler() {
							@Override
							public void onReactorInit(Event e)
							{
								super.onReactorInit(e);
								
								Reactor reactor = e.getReactor();
								MessagingFactory.this.connection = reactor.connection(MessagingFactory.this.connectionHandler);
							}
						});
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					this.openConnection = new CompletableFuture<Connection>();
					this.waitingConnectionOpen = true;
				}
			}
		}
		
		return this.connection;
	}
	
	CompletableFuture<Connection> getConnectionAsync()
	{
		this.getConnection();
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
	
	// Contract: ConnectionHandler - MessagingFactory
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
	
	public void onConnectionError(ErrorCondition error)
	{
		this.connection.close();
		
		// dispatch the TransportError to all dependent registered links
		for (Link link : this.links)
		{
			if (link instanceof Receiver)
			{
				Handler handler = BaseHandler.getHandler((Receiver) link);
				if (handler != null && handler instanceof ReceiveLinkHandler)
				{
					ReceiveLinkHandler recvLinkHandler = (ReceiveLinkHandler) handler;
					recvLinkHandler.processOnClose(link, error);
				}
			}
			else if(link instanceof Sender)
			{
				Handler handler = BaseHandler.getHandler((Sender) link);
				if (handler != null && handler instanceof ReceiveLinkHandler)
				{
					SendLinkHandler sendLinkHandler = (SendLinkHandler) handler;
					sendLinkHandler.processOnClose(link, error);
				}
			}
		}
	}

	public void close()
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
	public CompletableFuture<Void> closeAsync()
	{
		this.close();
		
		// TODO - hook up onRemoteClose & timeout 
		return CompletableFuture.completedFuture(null);
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
