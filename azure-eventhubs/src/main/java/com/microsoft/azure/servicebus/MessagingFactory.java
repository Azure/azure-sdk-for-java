/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;

import com.microsoft.azure.servicebus.amqp.BaseLinkHandler;
import com.microsoft.azure.servicebus.amqp.ConnectionHandler;
import com.microsoft.azure.servicebus.amqp.DispatchHandler;
import com.microsoft.azure.servicebus.amqp.IAmqpConnection;
import com.microsoft.azure.servicebus.amqp.IOperationResult;
import com.microsoft.azure.servicebus.amqp.ProtonUtil;
import com.microsoft.azure.servicebus.amqp.ReactorHandler;
import com.microsoft.azure.servicebus.amqp.ReactorDispatcher;
import com.microsoft.azure.servicebus.amqp.SessionHandler;

/**
 * Abstracts all amqp related details and exposes AmqpConnection object
 * Manages connection life-cycle
 */
public class MessagingFactory extends ClientEntity implements IAmqpConnection, ISessionProvider
{
	public static final Duration DefaultOperationTimeout = Duration.ofSeconds(60); 

	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	private final String hostName;
	private final CompletableFuture<Void> closeTask;
	private final ConnectionHandler connectionHandler;
	private final LinkedList<Link> registeredLinks;
	private final Object reactorLock;
        private final Object cbsChannelCreateLock;
        private final SharedAccessSignatureTokenProvider tokenProvider;
	
	private Reactor reactor;
	private ReactorDispatcher reactorScheduler;
	private Connection connection;
        private CBSChannel cbsChannel;

	private Duration operationTimeout;
	private RetryPolicy retryPolicy;
	private CompletableFuture<MessagingFactory> open;
	private CompletableFuture<Connection> openConnection;
	
	/**
	 * @param reactor parameter reactor is purely for testing purposes and the SDK code should always set it to null
	 */
	MessagingFactory(final ConnectionStringBuilder builder, final RetryPolicy retryPolicy)
	{
            super("MessagingFactory".concat(StringUtil.getRandomString()), null);

            Timer.register(this.getClientId());
            this.hostName = builder.getEndpoint().getHost();

            this.operationTimeout = builder.getOperationTimeout();
            this.retryPolicy = retryPolicy; 
            this.registeredLinks = new LinkedList<>();
            this.reactorLock = new Object();
            this.connectionHandler = new ConnectionHandler(this);
            this.openConnection = new CompletableFuture<>();
            this.cbsChannelCreateLock = new Object();
            this.tokenProvider = builder.getSharedAccessSignature() == null
                    ? new SharedAccessSignatureTokenProvider(builder.getSasKeyName(), builder.getSasKey())
                    : new SharedAccessSignatureTokenProvider(builder.getSharedAccessSignature());

            this.closeTask = new CompletableFuture<>();
            this.closeTask.thenAccept(new Consumer<Void>()
            {
                @Override
                public void accept(Void arg0)
                {
                    Timer.unregister(getClientId());
                }
            });
	}

	String getHostName()
	{
		return this.hostName;
	}
	
	private Reactor getReactor()
	{
		synchronized (this.reactorLock)
		{
			return this.reactor;
		}
	}
	
	public ReactorDispatcher getReactorScheduler()
	{
		synchronized (this.reactorLock)
		{
			return this.reactorScheduler;
		}
	}
        
        public SharedAccessSignatureTokenProvider getTokenProvider()
        {
            return this.tokenProvider;
        }

	private void createConnection(ConnectionStringBuilder builder) throws IOException
	{
		this.open = new CompletableFuture<>();
		this.startReactor(new ReactorHandler()
		{
			@Override
			public void onReactorInit(Event e)
			{
				super.onReactorInit(e);

				final Reactor r = e.getReactor();
				connection = r.connectionToHost(hostName, ClientConstants.AMQPS_PORT, connectionHandler);
			}
		});
	}

	private void startReactor(final ReactorHandler reactorHandler) throws IOException
	{
		final Reactor newReactor = ProtonUtil.reactor(reactorHandler);
		synchronized (this.reactorLock)
		{
			this.reactor = newReactor;
			this.reactorScheduler = new ReactorDispatcher(newReactor);
                        reactorHandler.unsafeSetReactorDispatcher(this.reactorScheduler);
		}
		
		final Thread reactorThread = new Thread(new RunReactor(newReactor));
		reactorThread.start();
	}
        
        public CBSChannel getCBSChannel()
        {
            synchronized (this.cbsChannelCreateLock)
            {
                if (this.cbsChannel == null)
                {
                    this.cbsChannel = new CBSChannel(this, this, "cbs-link");
                }
            }
            
            return this.cbsChannel;
        }

	@Override
	public Session getSession(final String path, final Consumer<Session> onRemoteSessionOpen, final BiConsumer<ErrorCondition, Exception> onRemoteSessionOpenError)
	{
                if (this.getIsClosingOrClosed()) {
                    
                    onRemoteSessionOpenError.accept(null, new OperationCancelledException("underlying messagingFactory instance is closed"));
                }
            
		if (this.connection == null || this.connection.getLocalState() == EndpointState.CLOSED || this.connection.getRemoteState() == EndpointState.CLOSED)
		{
			this.connection = this.getReactor().connectionToHost(this.hostName, ClientConstants.AMQPS_PORT, this.connectionHandler);
		}
                
                final Session session = this.connection.session();
                BaseHandler.setHandler(session, new SessionHandler(path, onRemoteSessionOpen, onRemoteSessionOpenError));
                session.open();
                
		return session;
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
		return createFromConnectionString(connectionString, RetryPolicy.getDefault());
	}

	public static CompletableFuture<MessagingFactory> createFromConnectionString(final String connectionString, final RetryPolicy retryPolicy) throws IOException
	{
		final ConnectionStringBuilder builder = new ConnectionStringBuilder(connectionString);
		final MessagingFactory messagingFactory = new MessagingFactory(builder, (retryPolicy != null) ? retryPolicy : RetryPolicy.getDefault());

		messagingFactory.createConnection(builder);
		return messagingFactory.open;
	}

	@Override
	public void onOpenComplete(Exception exception)
	{
		if (exception == null)
		{
			this.open.complete(this);
			this.openConnection.complete(this.connection);
                        if (this.getIsClosingOrClosed())
                            this.connection.close();
		}
		else
		{
			this.open.completeExceptionally(exception);
			this.openConnection.completeExceptionally(exception);
		}
	}

	@Override
	public void onConnectionError(ErrorCondition error)
	{
            	if (!this.open.isDone())
		{
			this.onOpenComplete(ExceptionUtil.toException(error));
		}
		else
		{
                        final Connection currentConnection = this.connection;
			for (Link link: this.registeredLinks)
			{
				if (link.getLocalState() != EndpointState.CLOSED && link.getRemoteState() != EndpointState.CLOSED)
				{
					link.close();
				}
			}
			
			this.openConnection = new CompletableFuture<>();

			if (currentConnection.getLocalState() != EndpointState.CLOSED && currentConnection.getRemoteState() != EndpointState.CLOSED)
			{
				currentConnection.close();
			}
                        
                        // Clone of the registeredLinks is needed here
                        // onClose of link will lead to un-register - which will result into iteratorCollectionModified error
                        final List<Link> registeredLinksCopy = new LinkedList<>(this.registeredLinks);
			for (Link link: registeredLinksCopy)
			{
                                final Handler handler = BaseHandler.getHandler(link);
				if (handler != null && handler instanceof BaseLinkHandler)
				{
					final BaseLinkHandler linkHandler = (BaseLinkHandler) handler;
					linkHandler.processOnClose(link, error);
				}
			}
		}

		if (this.getIsClosingOrClosed() && !this.closeTask.isDone())
		{
                    this.getReactor().stop();
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
			
			try
			{
				if (this.getIsClosingOrClosed())
				{
					return;
				}
				else
				{
					this.startReactor(new ReactorHandler());
				}
			}
			catch (IOException e)
			{
				TRACE_LOGGER.log(Level.SEVERE, ExceptionUtil.toStackTraceString(e, "Re-starting reactor failed with error"));
				
				this.onReactorError(cause);
			}

			if (currentConnection.getLocalState() != EndpointState.CLOSED && currentConnection.getRemoteState() != EndpointState.CLOSED)
			{
				currentConnection.close();
			}

			for (Link link: this.registeredLinks)
			{
				if (link.getLocalState() != EndpointState.CLOSED && link.getRemoteState() != EndpointState.CLOSED)
				{
					link.close();
				}
				
				final Handler handler = BaseHandler.getHandler(link);
				if (handler != null && handler instanceof BaseLinkHandler)
				{
					final BaseLinkHandler linkHandler = (BaseLinkHandler) handler;
					linkHandler.processOnClose(link, cause);
				}
			}
		}
	}

	@Override
	protected CompletableFuture<Void> onClose()
	{
            if (!this.getIsClosed())
            {
                try
                {
                    this.scheduleOnReactorThread(new CloseWork());
                }
                catch (IOException ioException)
                {
                    this.closeTask.completeExceptionally(new ServiceBusException(false, "Failed to Close MessagingFactory, see cause for more details.", ioException));
                }
            }		

            return this.closeTask;
	}
        
        private class CloseWork extends DispatchHandler
        {
            @Override
            public void onEvent()
            {
                final ReactorDispatcher dispatcher = getReactorScheduler();
                synchronized (cbsChannelCreateLock) {
                    
                    if (cbsChannel != null) {
                    
                        cbsChannel.close(
                                dispatcher,
                                new IOperationResult<Void, Exception>() {
                                    
                                    private void closeConnection() {
                                        
                                        if (connection != null && connection.getRemoteState() != EndpointState.CLOSED) {
                                            
                                            if (connection.getLocalState() != EndpointState.CLOSED) {
                                                connection.close();
                                            }
                                        }
                                    }
                                    
                                    @Override
                                    public void onComplete(Void result) {
                                        this.closeConnection();
                                    }
                                    @Override
                                    public void onError(Exception error) {
                                        this.closeConnection();
                                    }
                                });
                    }
                    
                    else {
                        
                        if (connection != null && connection.getRemoteState() != EndpointState.CLOSED && connection.getLocalState() != EndpointState.CLOSED)
                            connection.close();
                    }
                }
                
                if (connection != null && connection.getRemoteState() != EndpointState.CLOSED)
                {
                    Timer.schedule(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (!closeTask.isDone())
                            {
                                closeTask.completeExceptionally(new TimeoutException("Closing MessagingFactory timed out."));
                            }
                        }
                    },
                    operationTimeout, TimerType.OneTimeRun);
                }
            }
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
				Throwable cause = handlerException.getCause();
				if (cause == null)
				{
					cause = handlerException;
				}

				if(TRACE_LOGGER.isLoggable(Level.WARNING))
				{
					TRACE_LOGGER.log(Level.WARNING,
							ExceptionUtil.toStackTraceString(handlerException, "UnHandled exception while processing events in reactor:"));
				}

				String message = !StringUtil.isNullOrEmpty(cause.getMessage()) ? 
						cause.getMessage():
						!StringUtil.isNullOrEmpty(handlerException.getMessage()) ? 
							handlerException.getMessage() :
							"Reactor encountered unrecoverable error";
				
				ServiceBusException sbException = new ServiceBusException(
						true,
						String.format(Locale.US, "%s, %s", message, ExceptionUtil.getTrackingIDAndTimeToLog()),
						cause);
				
				if (cause instanceof UnresolvedAddressException)
				{
					sbException = new CommunicationException(
							String.format(Locale.US, "%s. This is usually caused by incorrect hostname or network configuration. Please check to see if namespace information is correct. %s", message, ExceptionUtil.getTrackingIDAndTimeToLog()),
							cause);
				}
				
				MessagingFactory.this.onReactorError(sbException);
			}
			finally
			{
                            this.rctr.free();
                        
                            if (getIsClosingOrClosed() && !closeTask.isDone())
                            {
                                closeTask.complete(null);
                            }
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
	
	public void scheduleOnReactorThread(final DispatchHandler handler) throws IOException
	{
		this.getReactorScheduler().invoke(handler);
	}

	public void scheduleOnReactorThread(final int delay, final DispatchHandler handler) throws IOException
	{
		this.getReactorScheduler().invoke(delay, handler);
	}	
}
