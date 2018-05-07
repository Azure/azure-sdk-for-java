package com.microsoft.azure.servicebus.primitives;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.microsoft.azure.servicebus.TransactionContext;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;
import com.microsoft.azure.servicebus.amqp.DispatchHandler;
import com.microsoft.azure.servicebus.amqp.IAmqpReceiver;
import com.microsoft.azure.servicebus.amqp.IAmqpSender;
import com.microsoft.azure.servicebus.amqp.ReceiveLinkHandler;
import com.microsoft.azure.servicebus.amqp.SendLinkHandler;
import com.microsoft.azure.servicebus.amqp.SessionHandler;

class RequestResponseLink extends ClientEntity{
	private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(RequestResponseLink.class);
	
	private final Object recreateLinksLock;
	private final MessagingFactory underlyingFactory;
	private final String linkPath;
	private final String sasTokenAudienceURI;
	private final String additionalAudienceURI;
	private final CompletableFuture<RequestResponseLink> createFuture;
	private final ConcurrentHashMap<String, RequestResponseWorkItem> pendingRequests;
    private final AtomicInteger requestCounter;
    private final String replyTo;
    
    private ScheduledFuture<?> sasTokenRenewTimerFuture;
	private InternalReceiver amqpReceiver;
	private InternalSender amqpSender;
	private boolean isRecreateLinksInProgress;
	private Map<Symbol, Object> additionalProperties;
	
	public static CompletableFuture<RequestResponseLink> createAsync(
			MessagingFactory messagingFactory,
			String linkName,
			String linkPath,
			String sasTokenAudienceURI,
			String additionalAudience,
			Map<Symbol, Object> additionalProperties)
	{
		final RequestResponseLink requestReponseLink = new RequestResponseLink(
				messagingFactory,
				linkName,
				linkPath,
				sasTokenAudienceURI,
				additionalAudience,
				additionalProperties);
		
		Timer.schedule(
				new Runnable()
				{
					public void run()
					{
						if (!requestReponseLink.createFuture.isDone())
						{
						    requestReponseLink.amqpSender.closeInternals(false);
						    requestReponseLink.amqpReceiver.closeInternals(false);
						    requestReponseLink.cancelSASTokenRenewTimer();
						    
							Exception operationTimedout = new TimeoutException(
									String.format(Locale.US, "Open operation on RequestResponseLink(%s) on Entity(%s) timed out at %s.", requestReponseLink.getClientId(), requestReponseLink.linkPath, ZonedDateTime.now().toString()));
							TRACE_LOGGER.error("RequestResponseLink open timed out.", operationTimedout);
							requestReponseLink.createFuture.completeExceptionally(operationTimedout);
						}
					}
				}
				, messagingFactory.getOperationTimeout()
				, TimerType.OneTimeRun);
		
		requestReponseLink.sendTokenAndSetRenewTimer(false).handleAsync((v, sasTokenEx) -> {
            if(sasTokenEx != null)
            {
                Throwable cause = ExceptionUtil.extractAsyncCompletionCause(sasTokenEx);
                TRACE_LOGGER.error("Sending SAS Token failed. RequestResponseLink path:{}", requestReponseLink.linkPath, cause);
                requestReponseLink.createFuture.completeExceptionally(cause);
            }
            else
            {
                try
                {
                    messagingFactory.scheduleOnReactorThread(new DispatchHandler()
                    {
                        @Override
                        public void onEvent()
                        {
                            requestReponseLink.createInternalLinks();
                        }
                    });
                }
                catch (IOException ioException)
                {
                    requestReponseLink.cancelSASTokenRenewTimer();
                    requestReponseLink.createFuture.completeExceptionally(new ServiceBusException(false, "Failed to create internal links, see cause for more details.", ioException));
                }
            }
            
            return null;
        });
		
		CompletableFuture.allOf(requestReponseLink.amqpSender.openFuture, requestReponseLink.amqpReceiver.openFuture).handleAsync((v, ex) -> 
        {
            if(ex == null)
            {
                TRACE_LOGGER.info("Opened requestresponselink to {}", requestReponseLink.linkPath);
                requestReponseLink.createFuture.complete(requestReponseLink);
            }
            else
            {
                requestReponseLink.cancelSASTokenRenewTimer();
                requestReponseLink.createFuture.completeExceptionally(ex);
            }
            
            return null;
        });
		
		return requestReponseLink.createFuture;
	}
	
	public static String getManagementNodeLinkPath(String entityPath)
	{
		return String.format("%s/%s", entityPath,  AmqpConstants.MANAGEMENT_NODE_ADDRESS_SEGMENT);
	}
	
	public static String getCBSNodeLinkPath()
    {
        return AmqpConstants.CBS_NODE_ADDRESS_SEGMENT;
    }
	
	private RequestResponseLink(
			MessagingFactory messagingFactory,
			String linkName,
			String linkPath,
			String sasTokenAudienceURI,
			String additionalAudience,
			Map<Symbol, Object> additionalProperties)
	{
		super(linkName);
		
		this.recreateLinksLock = new Object();
		this.isRecreateLinksInProgress = false;
		this.underlyingFactory = messagingFactory;
		this.linkPath = linkPath;
		this.sasTokenAudienceURI = sasTokenAudienceURI;
		this.additionalAudienceURI = additionalAudience;
		this.additionalProperties = additionalProperties;
		this.amqpSender = new InternalSender(linkName + ":internalSender", this, null);
		this.amqpReceiver = new InternalReceiver(linkName + ":interalReceiver", this);
		this.pendingRequests = new ConcurrentHashMap<>();
		this.requestCounter = new AtomicInteger();
		this.replyTo = UUID.randomUUID().toString();
		this.createFuture = new CompletableFuture<RequestResponseLink>();
	}
	
	public String getLinkPath()
	{
	    return this.linkPath;
	}
	
	private CompletableFuture<Void> sendTokenAndSetRenewTimer(boolean retryOnFailure)
    {
        if(this.getIsClosingOrClosed() || this.sasTokenAudienceURI == null)
        {
            return CompletableFuture.completedFuture(null);
        }
        else
        {
            CompletableFuture<ScheduledFuture<?>> sendTokenFuture = this.underlyingFactory.sendSecurityTokenAndSetRenewTimer(this.sasTokenAudienceURI, retryOnFailure, () -> this.sendTokenAndSetRenewTimer(true));
			CompletableFuture<Void> sasTokenFuture = sendTokenFuture.thenAccept((f) -> {this.sasTokenRenewTimerFuture = f; TRACE_LOGGER.debug("Set SAS Token renew timer");});

			if (additionalAudienceURI != null) {
				CompletableFuture<Void> transferSendTokenFuture = this.underlyingFactory.sendSecurityToken(this.additionalAudienceURI);
				return CompletableFuture.allOf(sasTokenFuture, transferSendTokenFuture);
			}

            return sasTokenFuture;
        }
    }
	
	private void cancelSASTokenRenewTimer()
    {
        if(this.sasTokenRenewTimerFuture != null && !this.sasTokenRenewTimerFuture.isDone())
        {
            TRACE_LOGGER.debug("Cancelling SAS Token renew timer");
            this.sasTokenRenewTimerFuture.cancel(true);
        }
    }
	
	private void createInternalLinks()
	{
		Map<Symbol, Object> commonLinkProperties = new HashMap<>();
		// ServiceBus expects timeout to be of type unsignedint
		commonLinkProperties.put(ClientConstants.LINK_TIMEOUT_PROPERTY, UnsignedInteger.valueOf(Util.adjustServerTimeout(this.underlyingFactory.getOperationTimeout()).toMillis()));
		if (this.additionalProperties != null) {
			commonLinkProperties.putAll(this.additionalProperties);
		}

		// Create send link
		final Connection connection = this.underlyingFactory.getConnection();

		Session session = connection.session();
		session.setOutgoingWindow(Integer.MAX_VALUE);
		session.open();
		BaseHandler.setHandler(session, new SessionHandler(this.linkPath));

		String sendLinkNamePrefix = "RequestResponseLink-Sender".concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(StringUtil.getShortRandomString());
		String sendLinkName = !StringUtil.isNullOrEmpty(connection.getRemoteContainer()) ?
				sendLinkNamePrefix.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(connection.getRemoteContainer()) :
				sendLinkNamePrefix;
		
		Sender sender = session.sender(sendLinkName);
		Target sednerTarget = new Target();
		sednerTarget.setAddress(this.linkPath);
		sender.setTarget(sednerTarget);
		Source senderSource = new Source();
		senderSource.setAddress(this.replyTo);
		sender.setSource(senderSource);
		sender.setSenderSettleMode(SenderSettleMode.SETTLED);
		sender.setProperties(commonLinkProperties);
		SendLinkHandler sendLinkHandler = new SendLinkHandler(this.amqpSender);
		BaseHandler.setHandler(sender, sendLinkHandler);
		this.amqpSender.setSendLink(sender);
		TRACE_LOGGER.debug("RequestReponseLink - opening send link to {}", this.linkPath);
		sender.open();
		
		// Create receive link
		session = connection.session();
        session.setOutgoingWindow(Integer.MAX_VALUE);
        session.open();
        BaseHandler.setHandler(session, new SessionHandler(this.linkPath));
        
		String receiveLinkNamePrefix = "RequestResponseLink-Receiver".concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(StringUtil.getShortRandomString());
		String receiveLinkName = !StringUtil.isNullOrEmpty(connection.getRemoteContainer()) ? 
				receiveLinkNamePrefix.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(connection.getRemoteContainer()) :
				receiveLinkNamePrefix;
		Receiver receiver = session.receiver(receiveLinkName);
		Source receiverSource = new Source();
		receiverSource.setAddress(this.linkPath);
		receiver.setSource(receiverSource);
		Target receiverTarget = new Target();
		receiverTarget.setAddress(this.replyTo);
		receiver.setTarget(receiverTarget);

		// Set settle modes
		receiver.setSenderSettleMode(SenderSettleMode.SETTLED);
		receiver.setReceiverSettleMode(ReceiverSettleMode.FIRST);
		receiver.setProperties(commonLinkProperties);

		final ReceiveLinkHandler receiveLinkHandler = new ReceiveLinkHandler(this.amqpReceiver);
		BaseHandler.setHandler(receiver, receiveLinkHandler);	
		this.amqpReceiver.setReceiveLink(receiver);
		TRACE_LOGGER.debug("RequestReponseLink - opening receive link to {}", this.linkPath);
		receiver.open();
	}
	
	private CompletableFuture<Void> recreateInternalLinks()
	{
	    TRACE_LOGGER.info("RequestResponseLink - recreating internal send and receive links to {}", this.linkPath);
		this.amqpSender.closeInternals(false);
		this.amqpReceiver.closeInternals(false);
		this.cancelSASTokenRenewTimer();
		
		// Create new internal sender and receiver objects, as old ones are closed
        this.amqpSender = new InternalSender(this.getClientId() + ":internalSender", this, this.amqpSender);
        this.amqpReceiver = new InternalReceiver(this.getClientId() + ":interalReceiver", this);
        CompletableFuture<Void> recreateInternalLinksFuture = new CompletableFuture<Void>();
        this.sendTokenAndSetRenewTimer(false).handleAsync((v, sasTokenEx) -> {
            if(sasTokenEx != null)
            {
                Throwable cause = ExceptionUtil.extractAsyncCompletionCause(sasTokenEx);
                TRACE_LOGGER.error("Sending SAS Token failed. RequestResponseLink path:{}", this.linkPath, cause);
                recreateInternalLinksFuture.completeExceptionally(cause);
            }
            else
            {
                try
                {
                    this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler()
                    {
                        @Override
                        public void onEvent()
                        {
                            RequestResponseLink.this.createInternalLinks();
                        }
                    });
                }
                catch (IOException ioException)
                {
                    this.cancelSASTokenRenewTimer();
                    recreateInternalLinksFuture.completeExceptionally(new ServiceBusException(false, "Failed to create internal links, see cause for more details.", ioException));
                }
            }
            
            return null;
        });
        
        CompletableFuture.allOf(this.amqpSender.openFuture, this.amqpReceiver.openFuture).handleAsync((v, ex) -> 
        {
            if(ex == null)
            {
                TRACE_LOGGER.info("Recreated internal links to {}", this.linkPath);
                recreateInternalLinksFuture.complete(null);
            }
            else
            {
                this.cancelSASTokenRenewTimer();
                recreateInternalLinksFuture.completeExceptionally(ex);
            }
            
            return null;
        });
		
		Timer.schedule(
            new Runnable()
            {
                public void run()
                {
                    if (!recreateInternalLinksFuture.isDone())
                    {
                        Exception operationTimedout = new TimeoutException(
                                String.format(Locale.US, "Recreating internal links of requestresponselink to %s.", RequestResponseLink.this.linkPath));
                        TRACE_LOGGER.warn("Recreating internal links of requestresponselink timed out.", operationTimedout);
                        RequestResponseLink.this.cancelSASTokenRenewTimer();
                        recreateInternalLinksFuture.completeExceptionally(operationTimedout);
                    }
                }
            }
            , this.underlyingFactory.getOperationTimeout()
            , TimerType.OneTimeRun);
		
		return recreateInternalLinksFuture;
	}
	
	private void completeAllPendingRequestsWithException(Exception exception)
	{
	    TRACE_LOGGER.warn("Completing all pending requests with exception in request response link to {}", this.linkPath);
		for(RequestResponseWorkItem workItem : this.pendingRequests.values())
		{			
			workItem.getWork().completeExceptionally(exception);
			workItem.cancelTimeoutTask(true);
		}
		
		this.pendingRequests.clear();
	}
	
	public CompletableFuture<Message> requestAysnc(Message requestMessage, TransactionContext transaction, Duration timeout)
	{	    
		this.throwIfClosed(null);
		// Check and recreate links if necessary
        if((this.amqpSender.sendLink.getLocalState() == EndpointState.CLOSED || this.amqpSender.sendLink.getRemoteState() == EndpointState.CLOSED)
                || (this.amqpReceiver.receiveLink.getLocalState() == EndpointState.CLOSED || this.amqpReceiver.receiveLink.getRemoteState() == EndpointState.CLOSED))
        {
            synchronized (this.recreateLinksLock) {
                if(!this.isRecreateLinksInProgress)
                {
                    this.isRecreateLinksInProgress = true;
                    this.recreateInternalLinks().handleAsync((v, recreationEx) ->
                    {
                        if(recreationEx != null)
                        {
                            TRACE_LOGGER.warn("Recreating internal links of reqestresponselink '{}' failed.", this.linkPath, ExceptionUtil.extractAsyncCompletionCause(recreationEx));
                        }
                        
                        synchronized (this.recreateLinksLock)
                        {
                            this.isRecreateLinksInProgress = false;
                        }
                        
                        return null;
                    });
                }
            }
        }
        
		CompletableFuture<Message> responseFuture = new CompletableFuture<Message>();
		RequestResponseWorkItem workItem = new RequestResponseWorkItem(requestMessage, transaction, responseFuture, timeout);
		String requestId = "request:" +  this.requestCounter.incrementAndGet();
		requestMessage.setMessageId(requestId);
		requestMessage.setReplyTo(this.replyTo);
		this.pendingRequests.put(requestId, workItem);
		workItem.setTimeoutTask(this.scheduleRequestTimeout(requestId, timeout));
		TRACE_LOGGER.debug("Sending request with id:{}", requestId);
		this.amqpSender.sendRequest(requestMessage, transaction, false);
		return responseFuture;
	}
	
	private ScheduledFuture<?> scheduleRequestTimeout(String requestId, Duration timeout)
	{
		return Timer.schedule(new Runnable() {
				public void run()
				{
				    TRACE_LOGGER.warn("Request with id:{} timed out", requestId);
					RequestResponseWorkItem completedWorkItem = RequestResponseLink.this.exceptionallyCompleteRequest(requestId, new TimeoutException("Request timed out."), true);
					boolean isRetriedWorkItem = completedWorkItem.getLastKnownException() != null;
					RequestResponseLink.this.amqpSender.removeEnqueuedRequest(completedWorkItem.request, isRetriedWorkItem);
				}
			}, timeout, TimerType.OneTimeRun);
	}
	
	
	private RequestResponseWorkItem exceptionallyCompleteRequest(String requestId, Exception exception, boolean useLastKnownException)
	{
		RequestResponseWorkItem workItem = this.pendingRequests.remove(requestId);
		if(workItem != null)
		{
			Exception exceptionToReport = exception;
			if(useLastKnownException && workItem.getLastKnownException() != null)
			{
				exceptionToReport = workItem.getLastKnownException();
			}
			
			workItem.getWork().completeExceptionally(exceptionToReport);
			workItem.cancelTimeoutTask(true);
		}
		
		return workItem;
	}
	
	private RequestResponseWorkItem completeRequestWithResponse(String requestId, Message responseMessage)
	{
		RequestResponseWorkItem workItem = this.pendingRequests.get(requestId);
		if(workItem != null)
		{
			int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
			TRACE_LOGGER.debug("Response for request with id:{} has status code:{}", requestId, statusCode);
			// Retry on server busy and other retry-able status codes (what are other codes??)
			if(statusCode == ClientConstants.REQUEST_RESPONSE_SERVER_BUSY_STATUS_CODE)
			{
			    TRACE_LOGGER.warn("Request with id:{} received ServerBusy response from '{}'", requestId, this.linkPath);
				// error response
				Exception responseException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
				this.underlyingFactory.getRetryPolicy().incrementRetryCount(this.getClientId());
				Duration retryInterval = this.underlyingFactory.getRetryPolicy().getNextRetryInterval(this.getClientId(), responseException, workItem.getTimeoutTracker().remaining());
				if (retryInterval == null)
				{
					// Either not retry-able or not enough time to retry
				    TRACE_LOGGER.error("Request with id:{} cannot be retried. So completing with excetion.", requestId, responseException);
					this.exceptionallyCompleteRequest(requestId, responseException, false);
				}
				else
				{
					// Retry
				    TRACE_LOGGER.info("Request with id:{} will be retried after {}.", requestId, retryInterval);
					workItem.setLastKnownException(responseException);
					try {
						this.underlyingFactory.scheduleOnReactorThread((int) retryInterval.toMillis(),
								new DispatchHandler()
								{
									@Override
									public void onEvent()
									{
										RequestResponseLink.this.amqpSender.sendRequest(workItem.getRequest(), workItem.getTransaction(), true);
									}
								});
					} catch (IOException e) {
						this.exceptionallyCompleteRequest(requestId, responseException, false);
					}
				}
			}
			else
			{
			    TRACE_LOGGER.debug("Completing request with id:{}", requestId);
				this.underlyingFactory.getRetryPolicy().resetRetryCount(this.getClientId());
				this.pendingRequests.remove(requestId);
				workItem.getWork().complete(responseMessage);
				workItem.cancelTimeoutTask(true);
			}
		}
		else
		{
			TRACE_LOGGER.warn("Request with id:{} not found in the requestresponse link.", requestId);
		}		
		
		return workItem;
	}

	@Override
	protected CompletableFuture<Void> onClose() {
	    TRACE_LOGGER.info("Closing requestresponselink to {} by closing both internal sender and receiver links.", this.linkPath);
	    this.cancelSASTokenRenewTimer();
		return this.amqpSender.closeAsync().thenComposeAsync((v) -> this.amqpReceiver.closeAsync());
	}
	
	private static void scheduleLinkCloseTimeout(CompletableFuture<Void> closeFuture, Duration timeout, String linkName)
	{		
		Timer.schedule(
				new Runnable()
				{
					public void run()
					{
						if (!closeFuture.isDone())
						{
							Exception operationTimedout = new TimeoutException(String.format(Locale.US, "%s operation on Link(%s) timed out at %s", "Close", linkName, ZonedDateTime.now()));
							TRACE_LOGGER.warn("Closing link timed out", operationTimedout);
							
							closeFuture.completeExceptionally(operationTimedout);
						}
					}
				}
				, timeout
				, TimerType.OneTimeRun);
	}
	
	private class InternalReceiver extends ClientEntity implements IAmqpReceiver
	{
		private RequestResponseLink parent;
		private Receiver receiveLink;
		private CompletableFuture<Void> openFuture;
		private CompletableFuture<Void> closeFuture;
		
		protected InternalReceiver(String clientId, RequestResponseLink parent) {
			super(clientId);
			this.parent = parent;
			this.openFuture = new CompletableFuture<Void>();
			this.closeFuture = new CompletableFuture<Void>();
		}		

		@Override		
		protected CompletableFuture<Void> onClose() {
			this.closeInternals(true);
			return this.closeFuture;
		}
		
		void closeInternals(boolean waitForCloseCompletion)
		{
		    if (!this.getIsClosed())
            {
                if (this.receiveLink != null && this.receiveLink.getLocalState() != EndpointState.CLOSED)
                {
                    try {
                        this.parent.underlyingFactory.scheduleOnReactorThread(new DispatchHandler() {
                            
                            @Override
                            public void onEvent() {
                                if (InternalReceiver.this.receiveLink != null && InternalReceiver.this.receiveLink.getLocalState() != EndpointState.CLOSED)
                                {
                                    TRACE_LOGGER.debug("Closing internal receive link of requestresponselink to {}", RequestResponseLink.this.linkPath);
                                    InternalReceiver.this.receiveLink.close();
                                    InternalReceiver.this.parent.underlyingFactory.deregisterForConnectionError(InternalReceiver.this.receiveLink);
                                    if(waitForCloseCompletion)
                                    {
                                        RequestResponseLink.scheduleLinkCloseTimeout(InternalReceiver.this.closeFuture, InternalReceiver.this.parent.underlyingFactory.getOperationTimeout(), InternalReceiver.this.receiveLink.getName());
                                    }
                                    else
                                    {
                                        InternalReceiver.this.closeFuture.complete(null);
                                    }
                                }
                            }
                        });
                    } catch (IOException e) {
                        this.closeFuture.completeExceptionally(e);
                    }
                }
                else
                {
                    this.closeFuture.complete(null);
                }
            }
		}

		@Override
		public void onOpenComplete(Exception completionException) {
			if(completionException == null)
			{
			    TRACE_LOGGER.debug("Opened internal receive link of requestresponselink to {}", parent.linkPath);
			    this.parent.underlyingFactory.registerForConnectionError(this.receiveLink);
				this.openFuture.complete(null);
				
				// Send unlimited credit
				this.receiveLink.flow(Integer.MAX_VALUE);
			}
			else
			{
			    TRACE_LOGGER.error("Opening internal receive link of requestresponselink to {} failed.", parent.linkPath, completionException);
			    this.setClosed();
			    this.closeFuture.complete(null);
				this.openFuture.completeExceptionally(completionException);
			}
		}

		@Override
		public void onError(Exception exception) {
			if(!this.openFuture.isDone())
			{
			    this.onOpenComplete(exception);
			}
			
			if(this.getIsClosingOrClosed())
			{
				if(!this.closeFuture.isDone())
				{
				    TRACE_LOGGER.error("Closing internal receive link of requestresponselink to {} failed.", parent.linkPath, exception);
					this.closeFuture.completeExceptionally(exception);
				}
			}
			
			TRACE_LOGGER.warn("Internal receive link of requestresponselink to '{}' encountered error.", this.parent.linkPath, exception);
			this.parent.underlyingFactory.deregisterForConnectionError(this.receiveLink);
			if(this.parent.amqpSender.sendLink != null)
            {
                this.parent.amqpSender.sendLink.close();
                this.parent.underlyingFactory.deregisterForConnectionError(this.parent.amqpSender.sendLink);
            }
            this.parent.cancelSASTokenRenewTimer();
			this.parent.completeAllPendingRequestsWithException(exception);
		}

		@Override
		public void onClose(ErrorCondition condition) {
			if(this.getIsClosingOrClosed())
			{
				if(!this.closeFuture.isDone())
				{
					if(condition == null || condition.getCondition() == null)
					{
					    TRACE_LOGGER.info("Closed internal receive link of requestresponselink to {}", parent.linkPath);
						this.closeFuture.complete(null);
					}
					else
					{
						Exception exception = ExceptionUtil.toException(condition);
						TRACE_LOGGER.error("Closing internal receive link of requestresponselink to {} failed.", parent.linkPath, exception);
						this.closeFuture.completeExceptionally(exception);
					}
				}
			}
			else
			{
				if(condition != null)
				{
					Exception exception = ExceptionUtil.toException(condition);
					if(!this.openFuture.isDone())
					{
					    this.onOpenComplete(exception);
					}
					else
					{
					    TRACE_LOGGER.warn("Internal receive link of requestresponselink to '{}' closed with error.", this.parent.linkPath, exception);
					    this.parent.underlyingFactory.deregisterForConnectionError(this.receiveLink);
					    if(this.parent.amqpSender.sendLink != null)
			            {
			                this.parent.amqpSender.sendLink.close();
			                this.parent.underlyingFactory.deregisterForConnectionError(this.parent.amqpSender.sendLink);
			            }
			            this.parent.cancelSASTokenRenewTimer();
					    this.parent.completeAllPendingRequestsWithException(exception);
					}
				}
			}
		}

		@Override
		public void onReceiveComplete(Delivery delivery)
		{
		    Message responseMessage = null;
		    try
		    {
		        responseMessage = Util.readMessageFromDelivery(this.receiveLink, delivery);
		        delivery.disposition(Accepted.getInstance());
		        delivery.settle();
		    }
			catch(Exception e)
		    {			    
			    TRACE_LOGGER.warn("Reading message from delivery failed with unexpected exception.", e);
			    
			    // release the delivery ??
			    delivery.disposition(Released.getInstance());
                delivery.settle();
			    return;
		    }
			
		    // Return response in a separate thread so reactor thread is free to handle reactor events
		    final Message finalResponseMessage = responseMessage;
		    AsyncUtil.executorService.submit(() -> {
		        String requestMessageId = (String)finalResponseMessage.getCorrelationId();
		        if(requestMessageId != null)
	            {
	                TRACE_LOGGER.debug("RequestRespnseLink received response for request with id :{}", requestMessageId);
	                this.parent.completeRequestWithResponse(requestMessageId, finalResponseMessage);
	            }
	            else
	            {
	                TRACE_LOGGER.warn("RequestRespnseLink received a message with null correlationId");
	            }
		    });
		}

		public void setReceiveLink(Receiver receiveLink) {
			this.receiveLink = receiveLink;
		}
	}
	
	private class InternalSender extends ClientEntity implements IAmqpSender
	{
		private Sender sendLink;
		private RequestResponseLink parent;
		private CompletableFuture<Void> openFuture;
		private CompletableFuture<Void> closeFuture;
		private AtomicInteger availableCredit;
		private LinkedList<InternalSenderWorkItem> pendingFreshSends;
		private LinkedList<InternalSenderWorkItem> pendingRetrySends;
		private Object pendingSendsSyncLock;
		private boolean isSendLoopRunning;

		protected InternalSender(String clientId, RequestResponseLink parent, InternalSender senderToBeCopied) {
			super(clientId);			
			this.parent = parent;
			this.availableCredit = new AtomicInteger(0);
			this.pendingSendsSyncLock = new Object();
			this.isSendLoopRunning = false;
			this.openFuture = new CompletableFuture<Void>();
			this.closeFuture = new CompletableFuture<Void>();
			
			if(senderToBeCopied == null)
			{
			    this.pendingFreshSends = new LinkedList<>();
	            this.pendingRetrySends = new LinkedList<>();
			}
			else
			{
			    this.pendingFreshSends = senderToBeCopied.pendingFreshSends;
                this.pendingRetrySends = senderToBeCopied.pendingRetrySends;
			}
		}		

		@Override
		protected CompletableFuture<Void> onClose() {
			this.closeInternals(true);
			return this.closeFuture;
		}
		
		void closeInternals(boolean waitForCloseCompletion)
		{
		    if (!this.getIsClosed())
            {
                if (this.sendLink != null && this.sendLink.getLocalState() != EndpointState.CLOSED)
                {
                    try {
                        this.parent.underlyingFactory.scheduleOnReactorThread(new DispatchHandler() {
                            
                            @Override
                            public void onEvent() {
                                if (InternalSender.this.sendLink != null && InternalSender.this.sendLink.getLocalState() != EndpointState.CLOSED)
                                {
                                    TRACE_LOGGER.debug("Closing internal send link of requestresponselink to {}", RequestResponseLink.this.linkPath);
                                    InternalSender.this.sendLink.close();
                                    InternalSender.this.parent.underlyingFactory.deregisterForConnectionError(InternalSender.this.sendLink);
                                    if(waitForCloseCompletion)
                                    {
                                        RequestResponseLink.scheduleLinkCloseTimeout(InternalSender.this.closeFuture, InternalSender.this.parent.underlyingFactory.getOperationTimeout(), InternalSender.this.sendLink.getName());
                                    }
                                    else
                                    {
                                        InternalSender.this.closeFuture.complete(null);
                                    }
                                }
                            }
                        });
                    } catch (IOException e) {
                        this.closeFuture.completeExceptionally(e);
                    }
                }
                else
                {
                    this.closeFuture.complete(null);
                }
            }
		}

		@Override
		public void onOpenComplete(Exception completionException) {
			if(completionException == null)
			{
			    TRACE_LOGGER.debug("Opened internal send link of requestresponselink to {}", parent.linkPath);
			    this.parent.underlyingFactory.registerForConnectionError(this.sendLink);
				this.openFuture.complete(null);	
				this.runSendLoop();
			}
			else
			{
			    TRACE_LOGGER.error("Opening internal send link of requestresponselink to {} failed.", parent.linkPath, completionException);
			    this.setClosed();
			    this.closeFuture.complete(null);
				this.openFuture.completeExceptionally(completionException);
			}
		}

		@Override
		public void onError(Exception exception) {
			if(!this.openFuture.isDone())
			{
			    this.onOpenComplete(exception);
			}
			
			if(this.getIsClosingOrClosed())
			{
				if(!this.closeFuture.isDone())
				{
				    TRACE_LOGGER.error("Closing internal send link of requestresponselink to {} failed.", parent.linkPath, exception);
					this.closeFuture.completeExceptionally(exception);
				}
			}
			
			TRACE_LOGGER.warn("Internal send link of requestresponselink to '{}' encountered error.", this.parent.linkPath, exception);
			this.parent.underlyingFactory.deregisterForConnectionError(this.sendLink);
			if(this.parent.amqpReceiver.receiveLink != null)
            {
                this.parent.amqpReceiver.receiveLink.close();
                this.parent.underlyingFactory.deregisterForConnectionError(this.parent.amqpReceiver.receiveLink);
            }
            this.parent.cancelSASTokenRenewTimer();
			this.parent.completeAllPendingRequestsWithException(exception);
		}

		@Override
		public void onClose(ErrorCondition condition) {
			if(this.getIsClosingOrClosed())
			{
				if(!this.closeFuture.isDone())
				{
					if(condition == null || condition.getCondition() == null)
					{
					    TRACE_LOGGER.info("Closed internal send link of requestresponselink to {}", parent.linkPath);
						this.closeFuture.complete(null);
					}
					else
					{
						Exception exception = ExceptionUtil.toException(condition);
						TRACE_LOGGER.error("Closing internal send link of requestresponselink to {} failed.", parent.linkPath, exception);
						this.closeFuture.completeExceptionally(exception);
					}
				}
			}
			else
			{
				if(condition != null)
				{
					Exception exception = ExceptionUtil.toException(condition);
					if(!this.openFuture.isDone())
					{
					    this.onOpenComplete(exception);
					}
					else
					{
					    TRACE_LOGGER.warn("Internal send link of requestresponselink to '{}' closed with error.", this.parent.linkPath, exception);
					    this.parent.underlyingFactory.deregisterForConnectionError(this.sendLink);
					    
					    if(this.parent.amqpReceiver.receiveLink != null)
					    {
					        this.parent.amqpReceiver.receiveLink.close();
	                        this.parent.underlyingFactory.deregisterForConnectionError(this.parent.amqpReceiver.receiveLink);
					    }
					    
					    this.parent.cancelSASTokenRenewTimer();
					    this.parent.completeAllPendingRequestsWithException(exception);
					}
				}
			}
		}
		
		public void sendRequest(Message requestMessage, TransactionContext transaction, boolean isRetry)
		{
			synchronized(this.pendingSendsSyncLock)
			{
				if(isRetry)
				{
					this.pendingRetrySends.add(new InternalSenderWorkItem(requestMessage, transaction));
				}
				else
				{
					this.pendingFreshSends.add(new InternalSenderWorkItem(requestMessage, transaction));
				}
				
				// This check must be done inside lock
				if(this.isSendLoopRunning)
	            {
	                return;
	            }
			}
			
			try {
                this.parent.underlyingFactory.scheduleOnReactorThread(new DispatchHandler() {
                    @Override
                    public void onEvent() {
                        InternalSender.this.runSendLoop();
                    }
                });
            } catch (IOException e) {
                this.parent.exceptionallyCompleteRequest((String)requestMessage.getMessageId(), e, true);
            }
		}
		
		public void removeEnqueuedRequest(Message requestMessage, boolean isRetry)
		{
			synchronized(this.pendingSendsSyncLock)
			{
				// Collections are more likely to be very small. So remove() shouldn't be a problem.
				if(isRetry)
				{
					this.pendingRetrySends.remove(requestMessage);
				}
				else
				{
					this.pendingFreshSends.remove(requestMessage);
				}				
			}
		}

		@Override
		public void onFlow(int creditIssued) {
		    TRACE_LOGGER.debug("RequestResonseLink {} internal sender received credit :{}", this.parent.linkPath, creditIssued);
			this.availableCredit.addAndGet(creditIssued);
			TRACE_LOGGER.debug("RequestResonseLink {} internal sender available credit :{}", this.parent.linkPath, this.availableCredit.get());
			this.runSendLoop();
		}

		@Override
		public void onSendComplete(Delivery delivery) {
			// Doesn't happen as sends are settled on send
		}		

		public void setSendLink(Sender sendLink) {			
			this.sendLink = sendLink;
			this.availableCredit = new AtomicInteger(0);
		}
		
		private void runSendLoop()
		{
		    synchronized(this.pendingSendsSyncLock)
		    {
		        if(this.isSendLoopRunning)
		        {
		            return;
		        }
		        else
		        {
		            this.isSendLoopRunning = true;
		        }
		    }
		    
		    TRACE_LOGGER.debug("Starting requestResponseLink {} internal sender send loop", this.parent.linkPath);
		    
		    try
            {
                while(this.sendLink != null && this.sendLink.getLocalState() == EndpointState.ACTIVE && this.sendLink.getRemoteState() == EndpointState.ACTIVE && this.availableCredit.get() > 0)
                {
					InternalSenderWorkItem requestToBeSent = null;
                    synchronized(pendingSendsSyncLock)
                    {
                        // First send retries and then fresh ones
                        requestToBeSent = this.pendingRetrySends.poll();
                        if(requestToBeSent == null)
                        {
                            requestToBeSent = this.pendingFreshSends.poll();
                            if(requestToBeSent == null)
                            {
                                // Set to false inside the synchronized block to avoid race condition
                                this.isSendLoopRunning = false;
                                TRACE_LOGGER.debug("RequestResponseLink {} internal sender send loop ending as there are no more requests enqueued.", this.parent.linkPath);
                                break;
                            }
                        }
                    }
                    
                    Delivery delivery = this.sendLink.delivery(UUID.randomUUID().toString().getBytes());
                    delivery.setMessageFormat(DeliveryImpl.DEFAULT_MESSAGE_FORMAT);
					TransactionContext transaction = requestToBeSent.getTransaction();
					if (transaction != TransactionContext.NULL_TXN) {
						TransactionalState transactionalState = new TransactionalState();
						transactionalState.setTxnId(new Binary(transaction.getTransactionId().array()));
						delivery.disposition(transactionalState);
					}

                    Pair<byte[], Integer> encodedPair = null;
                    try
                    {
                        encodedPair = Util.encodeMessageToOptimalSizeArray(requestToBeSent.getMessage());
                    }
                    catch(PayloadSizeExceededException exception)
                    {
                        this.parent.exceptionallyCompleteRequest((String)requestToBeSent.getMessage().getMessageId(), new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s kb", ClientConstants.MAX_MESSAGE_LENGTH_BYTES / 1024), exception), false);
                    }
                    
                    try
                    {
                        int sentMsgSize = this.sendLink.send(encodedPair.getFirstItem(), 0, encodedPair.getSecondItem());
                        assert sentMsgSize == encodedPair.getSecondItem() : "Contract of the ProtonJ library for Sender.Send API changed";
                        delivery.settle();
                        this.availableCredit.decrementAndGet();
                        TRACE_LOGGER.debug("RequestResonseLink {} internal sender sent a request. available credit :{}", this.parent.linkPath, this.availableCredit.get());
                    }
                    catch(Exception e)
                    {
                        TRACE_LOGGER.error("RequestResonseLink {} failed to send request with request id:{}.", this.parent.linkPath, requestToBeSent.getMessage().getMessageId(), e);
                        this.parent.exceptionallyCompleteRequest((String)requestToBeSent.getMessage().getMessageId(), e, false);
                    }
                }
            }
            finally
            {
                synchronized (this.pendingSendsSyncLock) {
                    if(this.isSendLoopRunning)
                    {
                        this.isSendLoopRunning = false;
                    }
                }
                
                TRACE_LOGGER.debug("RequestResponseLink {} internal sender send loop stopped.", this.parent.linkPath);
            }
		}
	}

	class InternalSenderWorkItem {
		private Message message;
		private TransactionContext transaction;

		public InternalSenderWorkItem(Message message, TransactionContext transaction) {
			this.message = message;
			this.transaction = transaction;
		}

		public Message getMessage() { return this.message; }

		public TransactionContext getTransaction() { return this.transaction; }
	}
}
