/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.reactor.Reactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.microsoft.azure.servicebus.amqp.BaseLinkHandler;
import com.microsoft.azure.servicebus.amqp.ConnectionHandler;
import com.microsoft.azure.servicebus.amqp.DispatchHandler;
import com.microsoft.azure.servicebus.amqp.IAmqpConnection;
import com.microsoft.azure.servicebus.amqp.ProtonUtil;
import com.microsoft.azure.servicebus.amqp.ReactorHandler;
import com.microsoft.azure.servicebus.amqp.ReactorDispatcher;

/**
 * Abstracts all AMQP related details and encapsulates an AMQP connection and manages its life cycle. Each instance of this class represent one AMQP connection to the namespace.
 * If an application creates multiple senders, receivers or clients using the same MessagingFacotry instance, all those senders, receivers or clients will share the same connection to the namespace.
 * @since 1.0
 */
public class MessagingFactory extends ClientEntity implements IAmqpConnection
{
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(MessagingFactory.class);
	
    private static final String REACTOR_THREAD_NAME_PREFIX = "ReactorThread";
	private static final int MAX_CBS_LINK_CREATION_ATTEMPTS = 3;
	private final ConnectionStringBuilder builder;
	private final String hostName;
	private final CompletableFuture<Void> connetionCloseFuture;
	private final ConnectionHandler connectionHandler;
	private final ReactorHandler reactorHandler;
	private final LinkedList<Link> registeredLinks;
	private final Object reactorLock;
	private final RequestResponseLinkcache managementLinksCache;
	
	private Reactor reactor;
	private ReactorDispatcher reactorScheduler;
	private Connection connection;

	private Duration operationTimeout;
	private RetryPolicy retryPolicy;
	private CompletableFuture<MessagingFactory> factoryOpenFuture;
	private CompletableFuture<Void> cbsLinkCreationFuture;
	private RequestResponseLink cbsLink;
	private int cbsLinkCreationAttempts = 0;
	private Throwable lastCBSLinkCreationException = null;
	
	MessagingFactory(final ConnectionStringBuilder builder)
	{
		super("MessagingFactory".concat(StringUtil.getShortRandomString()), null);

		Timer.register(this.getClientId());
		this.builder = builder;
		this.hostName = builder.getEndpoint().getHost();
		
		this.operationTimeout = builder.getOperationTimeout();
		this.retryPolicy = builder.getRetryPolicy();
		this.registeredLinks = new LinkedList<Link>();
		this.connetionCloseFuture = new CompletableFuture<Void>();
		this.reactorLock = new Object();
		this.connectionHandler = new ConnectionHandler(this);
		this.factoryOpenFuture = new CompletableFuture<MessagingFactory>();
		this.cbsLinkCreationFuture = new CompletableFuture<Void>();
		this.managementLinksCache = new RequestResponseLinkcache(this);
		this.reactorHandler = new ReactorHandler()
		{
			@Override
			public void onReactorInit(Event e)
			{
				super.onReactorInit(e);

				final Reactor r = e.getReactor();
				TRACE_LOGGER.info("Creating connection to host '{}:{}'", hostName, ClientConstants.AMQPS_PORT);
				connection = r.connectionToHost(hostName, ClientConstants.AMQPS_PORT, connectionHandler);
			}
		};
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
	
	private ReactorDispatcher getReactorScheduler()
	{
		synchronized (this.reactorLock)
		{
			return this.reactorScheduler;
		}
	}

	private void startReactor(ReactorHandler reactorHandler) throws IOException
	{
	    TRACE_LOGGER.info("Creating and starting reactor");
		Reactor newReactor = ProtonUtil.reactor(reactorHandler);
		synchronized (this.reactorLock)
		{
			this.reactor = newReactor;
			this.reactorScheduler = new ReactorDispatcher(newReactor);
		}
		
		String reactorThreadName = REACTOR_THREAD_NAME_PREFIX + UUID.randomUUID().toString();
		Thread reactorThread = new Thread(new RunReactor(), reactorThreadName);
		reactorThread.start();
		TRACE_LOGGER.info("Started reactor");
	}
	
	Connection getConnection()
	{
		if (this.connection == null || this.connection.getLocalState() == EndpointState.CLOSED || this.connection.getRemoteState() == EndpointState.CLOSED)
		{
		    TRACE_LOGGER.info("Creating connection to host '{}:{}'", hostName, ClientConstants.AMQPS_PORT);
			this.connection = this.getReactor().connectionToHost(this.hostName, ClientConstants.AMQPS_PORT, this.connectionHandler);
		}

		return this.connection;
	}

	/**
	 * Gets the operation timeout from the connections string.
	 * @return operation timeout specified in the connection string
	 */
	public Duration getOperationTimeout()
	{
		return this.operationTimeout;
	}

	/**
	 * Gets the retry policy from the connection string.
	 * @return retry policy specified in the connection string
	 */
	public RetryPolicy getRetryPolicy()
	{
		return this.retryPolicy;
	}

	/**
	 * Creates an instance of MessagingFactory from the given connection string builder. This is a non-blocking method.
	 * @param builder connection string builder to the  bus namespace or entity
	 * @return a <code>CompletableFuture</code> which completes when a connection is established to the namespace or when a connection couldn't be established.
	 * @see java.util.concurrent.CompletableFuture
	 */
	public static CompletableFuture<MessagingFactory> createFromConnectionStringBuilderAsync(final ConnectionStringBuilder builder)
	{	
	    if(TRACE_LOGGER.isInfoEnabled())
	    {
	        TRACE_LOGGER.info("Creating messaging factory from connection string '{}'", builder.toLoggableString());
	    }
	    
		MessagingFactory messagingFactory = new MessagingFactory(builder);
		try {			
			messagingFactory.startReactor(messagingFactory.reactorHandler);
		} catch (IOException e) {
		    Marker fatalMarker = MarkerFactory.getMarker(ClientConstants.FATAL_MARKER);
			TRACE_LOGGER.error(fatalMarker, "Starting reactor failed", e);
			messagingFactory.factoryOpenFuture.completeExceptionally(e);
		}
		return messagingFactory.factoryOpenFuture;
	}
	
	/**
	 * Creates an instance of MessagingFactory from the given connection string. This is a non-blocking method.
	 * @param connectionString connection string to the  bus namespace or entity
	 * @return a <code>CompletableFuture</code> which completes when a connection is established to the namespace or when a connection couldn't be established.
	 * @see java.util.concurrent.CompletableFuture
	 */
	public static CompletableFuture<MessagingFactory> createFromConnectionStringAsync(final String connectionString)
	{
		ConnectionStringBuilder builder = new ConnectionStringBuilder(connectionString);
		return createFromConnectionStringBuilderAsync(builder);
	}
	
	/**
	 * Creates an instance of MessagingFactory from the given connection string builder. This method blocks for a connection to the namespace to be established.
	 * @param builder connection string builder to the  bus namespace or entity
	 * @return an instance of MessagingFactory
	 * @throws InterruptedException if blocking thread is interrupted
	 * @throws ExecutionException if a connection couldn't be established to the namespace. Cause of the failure can be found by calling {@link Exception#getCause()}
	 */
	public static MessagingFactory createFromConnectionStringBuilder(final ConnectionStringBuilder builder) throws InterruptedException, ExecutionException
	{		
		return createFromConnectionStringBuilderAsync(builder).get();
	}
	
	/**
	 * Creates an instance of MessagingFactory from the given connection string. This method blocks for a connection to the namespace to be established.
	 * @param connectionString connection string to the  bus namespace or entity
	 * @return an instance of MessagingFactory
	 * @throws InterruptedException if blocking thread is interrupted
	 * @throws ExecutionException if a connection couldn't be established to the namespace. Cause of the failure can be found by calling {@link Exception#getCause()}
	 */
	public static MessagingFactory createFromConnectionString(final String connectionString) throws InterruptedException, ExecutionException
	{		
		return createFromConnectionStringAsync(connectionString).get();
	}

	/**
     * Internal method.&nbsp;Clients should not use this method.
     */
	@Override
	public void onConnectionOpen()
	{
	    if(!factoryOpenFuture.isDone())
	    {
	        TRACE_LOGGER.info("MessagingFactory opened.");
	        AsyncUtil.completeFuture(this.factoryOpenFuture, this);
	    }
	    
	    // Connection opened. Initiate new cbs link creation
	    TRACE_LOGGER.info("Connection opened to host.");
	    if(this.cbsLink == null)
	    {
	        this.createCBSLinkAsync();
	    }	    
	}

	/**
	 * Internal method.&nbsp;Clients should not use this method.
	 */
	@Override
	public void onConnectionError(ErrorCondition error)
	{
	    if(error != null && error.getCondition() != null)
	    {
	        TRACE_LOGGER.error("Connection error. '{}'", error);
	    }
	    
		if (!this.factoryOpenFuture.isDone())
		{		    
		    AsyncUtil.completeFutureExceptionally(this.factoryOpenFuture, ExceptionUtil.toException(error));
		}
		else
		{
		    this.closeConnection(error, null);
		}

		if (this.getIsClosingOrClosed() && !this.connetionCloseFuture.isDone())
		{
		    TRACE_LOGGER.info("Connection to host closed.");
		    this.connetionCloseFuture.complete(null);
			Timer.unregister(this.getClientId());
		}
	}

	private void onReactorError(Exception cause)
	{
	    TRACE_LOGGER.error("Reactor error occured", cause);
		if (!this.factoryOpenFuture.isDone())
		{
		    AsyncUtil.completeFutureExceptionally(this.factoryOpenFuture, cause);
		}
		else
		{
		    if(this.getIsClosingOrClosed())
            {
                return;
            }
			
			try
			{
				this.startReactor(this.reactorHandler);
			}
			catch (IOException e)
			{
			    Marker fatalMarker = MarkerFactory.getMarker(ClientConstants.FATAL_MARKER);
			    TRACE_LOGGER.error(fatalMarker, "Re-starting reactor failed with exception.", e);							
				this.onReactorError(cause);
			}
			
			this.closeConnection(null, cause);
		}
	}
	
	// One of the parameters must be null
	private void closeConnection(ErrorCondition error, Exception cause)
	{
	    TRACE_LOGGER.info("Closing connection to host");
	    // Important to copy the reference of the connection as a call to getConnection might create a new connection while we are still in this method
	    Connection currentConnection = this.connection;
	    if(connection != null)
	    {
	        Link[] links = this.registeredLinks.toArray(new Link[0]);
	        
	        TRACE_LOGGER.debug("Closing all links on the connection. Number of links '{}'", links.length);
	        for(Link link : links)
	        {
	            if (link.getLocalState() != EndpointState.CLOSED && link.getRemoteState() != EndpointState.CLOSED)
	            {
	                link.close();
	            }
	        }
	        
	        TRACE_LOGGER.debug("Closed all links on the connection. Number of links '{}'", links.length);

	        if (currentConnection.getLocalState() != EndpointState.CLOSED && currentConnection.getRemoteState() != EndpointState.CLOSED)
	        {
	            currentConnection.close();
	            currentConnection.free();	            
	        }
	        
	        for(Link link : links)
	        {
	            Handler handler = BaseHandler.getHandler(link);
	            if (handler != null && handler instanceof BaseLinkHandler)
	            {
	                BaseLinkHandler linkHandler = (BaseLinkHandler) handler;
	                if(error != null)
	                {
	                    linkHandler.processOnClose(link, error);
	                }
	                else
	                {
	                    linkHandler.processOnClose(link, cause);
	                }
	            }
	        }
	    }
	}

	@Override
	protected CompletableFuture<Void> onClose()
	{
		if (!this.getIsClosed())
		{
		    TRACE_LOGGER.info("Closing messaging factory");
		    CompletableFuture<Void> cbsLinkCloseFuture;
		    if(this.cbsLink == null)
		    {
		        cbsLinkCloseFuture = CompletableFuture.completedFuture(null);
		    }
		    else
		    {
		        TRACE_LOGGER.info("Closing CBS link");
		        cbsLinkCloseFuture = this.cbsLink.closeAsync();
		    }		    
		    
		    cbsLinkCloseFuture.thenRun(() -> this.managementLinksCache.freeAsync()).thenRun(() -> {
		        if(this.cbsLinkCreationFuture != null && !this.cbsLinkCreationFuture.isDone())
	            {
	                this.cbsLinkCreationFuture.completeExceptionally(new Exception("Connection closed."));
	            }
		        
		        if (this.connection != null && this.connection.getRemoteState() != EndpointState.CLOSED)
	            {
	                try {
	                    this.scheduleOnReactorThread(new DispatchHandler()
	                    {
	                        @Override
	                        public void onEvent()
	                        {
	                            if (MessagingFactory.this.connection != null && MessagingFactory.this.connection.getLocalState() != EndpointState.CLOSED)
	                            {
	                                TRACE_LOGGER.info("Closing connection to host");
	                                MessagingFactory.this.connection.close();
	                            }
	                        }
	                    });
	                } catch (IOException e) {
	                    AsyncUtil.completeFutureExceptionally(this.connetionCloseFuture, e);
	                }	                
	            }
	            else if(this.connection == null || this.connection.getRemoteState() == EndpointState.CLOSED)
	            {
	                this.connetionCloseFuture.complete(null);
	            }
		    });
		    
		    Timer.schedule(new Runnable()
            {
                @Override
                public void run()
                {
                    if (!MessagingFactory.this.connetionCloseFuture.isDone())
                    {
                        String errorMessage = "Closing MessagingFactory timed out.";
                        TRACE_LOGGER.warn(errorMessage);
                        MessagingFactory.this.connetionCloseFuture.completeExceptionally(new TimeoutException(errorMessage));
                    }
                }
            },
            this.operationTimeout, TimerType.OneTimeRun);
			
			return this.connetionCloseFuture;
		}
		else
		{
		    return CompletableFuture.completedFuture(null);
		}
	}

	private class RunReactor implements Runnable
	{
		final private Reactor rctr;

		public RunReactor()
		{
			this.rctr = MessagingFactory.this.getReactor();
		}

		public void run()
		{
		    TRACE_LOGGER.info("starting reactor instance.");
			try
			{
				this.rctr.setTimeout(3141);
				this.rctr.start();
				boolean continuteProcessing = true;
                while(!Thread.interrupted() && continuteProcessing)
                {
                    // If factory is closed, stop reactor too
                    if(MessagingFactory.this.getIsClosed())
                    {
                        TRACE_LOGGER.info("Gracefully releasing reactor thread as messaging factory is closed");
                        break;
                    }
                    continuteProcessing = this.rctr.process();
                }				
				TRACE_LOGGER.info("Stopping reactor");
				this.rctr.stop();
			}
			catch (HandlerException handlerException)
			{
				Throwable cause = handlerException.getCause();
				if (cause == null)
				{
					cause = handlerException;
				}
				
				TRACE_LOGGER.error("UnHandled exception while processing events in reactor:", handlerException);

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
			}
		}
	}

	/**
     * Internal method.&nbsp;Clients should not use this method.
     */
	@Override
	public void registerForConnectionError(Link link)
	{
		this.registeredLinks.add(link);
	}

	/**
     * Internal method.&nbsp;Clients should not use this method.
     */
	@Override
	public void deregisterForConnectionError(Link link)
	{
		this.registeredLinks.remove(link);
	}
	
	void scheduleOnReactorThread(final DispatchHandler handler) throws IOException
	{
		this.getReactorScheduler().invoke(handler);
	}

	void scheduleOnReactorThread(final int delay, final DispatchHandler handler) throws IOException
	{
		this.getReactorScheduler().invoke(delay, handler);
	}
	
	CompletableFuture<ScheduledFuture<?>> sendSASTokenAndSetRenewTimer(String sasTokenAudienceURI, boolean retryOnFailure, Runnable validityRenewer)
    {
	    TRACE_LOGGER.debug("Sending CBS Token for {}", sasTokenAudienceURI);
	    boolean isSasTokenGenerated = false;
	    String sasToken = this.builder.getSharedAccessSignatureToken();
        try
        {
            if(sasToken == null)
            {
                sasToken = SASUtil.generateSharedAccessSignatureToken(builder.getSasKeyName(), builder.getSasKey(), sasTokenAudienceURI, ClientConstants.DEFAULT_SAS_TOKEN_VALIDITY_IN_SECONDS);
                isSasTokenGenerated = true;
            }
        } catch (InvalidKeyException e) {
            CompletableFuture<ScheduledFuture<?>> exceptionFuture = new CompletableFuture<>();
            exceptionFuture.completeExceptionally(e);
            return exceptionFuture;
        }

        final String finalSasToken = sasToken;
        final boolean finalIsSasTokenGenerated = isSasTokenGenerated;

        CompletableFuture<Void> sendTokenFuture = this.cbsLinkCreationFuture.thenComposeAsync((v) -> {
            return CommonRequestResponseOperations.sendCBSTokenAsync(this.cbsLink, Util.adjustServerTimeout(this.operationTimeout), finalSasToken, ClientConstants.SAS_TOKEN_TYPE, sasTokenAudienceURI);
        });
        
        
        if(retryOnFailure)
        {
            return sendTokenFuture.handleAsync((v, sendTokenEx) -> {
                if(sendTokenEx == null)
                {
                    return MessagingFactory.scheduleRenewTimer(finalIsSasTokenGenerated, sasTokenAudienceURI, validityRenewer);
                }
                else
                {
                    TRACE_LOGGER.error("Sending CBS Token for {} failed.", sasTokenAudienceURI, sendTokenEx);
                    TRACE_LOGGER.info("Will retry sending CBS Token for {} after {} seconds.", sasTokenAudienceURI, ClientConstants.DEFAULT_SAS_TOKEN_SEND_RETRY_INTERVAL_IN_SECONDS);
                    return Timer.schedule(validityRenewer, Duration.ofSeconds(ClientConstants.DEFAULT_SAS_TOKEN_SEND_RETRY_INTERVAL_IN_SECONDS), TimerType.OneTimeRun);                
                }            
            });
        }
        else
        {
            // Let the exception of the sendToken state pass up to caller
            return sendTokenFuture.thenApply((v) -> {
                return MessagingFactory.scheduleRenewTimer(finalIsSasTokenGenerated, sasTokenAudienceURI, validityRenewer);
            });
        }        
    }
	
	private static ScheduledFuture<?> scheduleRenewTimer(boolean isSasTokenGenerated, String sasTokenAudienceURI, Runnable validityRenewer)
	{
	    TRACE_LOGGER.debug("Sent CBS Token for {}", sasTokenAudienceURI);
        if(isSasTokenGenerated)
        {
            // It will eventually expire. Renew it
            int renewInterval = SASUtil.getCBSTokenRenewIntervalInSeconds(ClientConstants.DEFAULT_SAS_TOKEN_VALIDITY_IN_SECONDS);
            return Timer.schedule(validityRenewer, Duration.ofSeconds(renewInterval), TimerType.OneTimeRun);
        }
        else
        {
            // User provided signature. We can't renew it.
            return null;
        }
	}
	
	CompletableFuture<RequestResponseLink> obtainRequestResponseLinkAsync(String entityPath)
	{
	    this.throwIfClosed(null);
	    return this.managementLinksCache.obtainRequestResponseLinkAsync(entityPath);
	}
	
	void releaseRequestResponseLink(String entityPath)
	{
	    if(!this.getIsClosed())
	    {
	        this.managementLinksCache.releaseRequestResponseLink(entityPath);
	    }	    
	}
	
	private CompletableFuture<Void> createCBSLinkAsync()
    {
	    if(++this.cbsLinkCreationAttempts > MAX_CBS_LINK_CREATION_ATTEMPTS )
	    {
	        Throwable completionEx = this.lastCBSLinkCreationException == null ? new Exception("CBS link creation failed multiple times.") : this.lastCBSLinkCreationException;	        
	        this.cbsLinkCreationFuture.completeExceptionally(completionEx);
	        return CompletableFuture.completedFuture(null);     
	    }
	    else
	    {	        
	        String requestResponseLinkPath = RequestResponseLink.getCBSNodeLinkPath();
	        TRACE_LOGGER.info("Creating CBS link to {}", requestResponseLinkPath);
	        CompletableFuture<Void> crateAndAssignRequestResponseLink =
	                        RequestResponseLink.createAsync(this, this.getClientId() + "-cbs", requestResponseLinkPath).handleAsync((cbsLink, ex) ->
	                        {
	                            if(ex == null)
	                            {
	                                TRACE_LOGGER.info("Created CBS link to {}", requestResponseLinkPath);
	                                this.cbsLink = cbsLink;	                                
	                                this.cbsLinkCreationFuture.complete(null);
	                            }
	                            else
	                            {	                                
	                                this.lastCBSLinkCreationException = ExceptionUtil.extractAsyncCompletionCause(ex);
	                                TRACE_LOGGER.error("Creating CBS link to {} failed. Attempts '{}'", requestResponseLinkPath, this.cbsLinkCreationAttempts);
	                                this.createCBSLinkAsync();
	                            }
	                            return null;
	                        });       
	        return crateAndAssignRequestResponseLink;
	    }	    
    }
}
