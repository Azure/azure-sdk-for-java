package com.microsoft.azure.servicebus.primitives;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
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

import com.microsoft.azure.servicebus.amqp.AmqpConstants;
import com.microsoft.azure.servicebus.amqp.DispatchHandler;
import com.microsoft.azure.servicebus.amqp.IAmqpReceiver;
import com.microsoft.azure.servicebus.amqp.IAmqpSender;
import com.microsoft.azure.servicebus.amqp.ReceiveLinkHandler;
import com.microsoft.azure.servicebus.amqp.SendLinkHandler;
import com.microsoft.azure.servicebus.amqp.SessionHandler;

class RequestResponseLink extends ClientEntity{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	
	private MessagingFactory underlyingFactory;
	private String linkPath;
	private InternalReceiver amqpReceiver;
	private InternalSender amqpSender;	
	private CompletableFuture<RequestResponseLink> createFuture;
	private String replyTo;
	private ConcurrentHashMap<String, RequestResponseWorkItem> pendingRequests;
	private AtomicInteger requestCounter;
	
	public static CompletableFuture<RequestResponseLink> createAsync(MessagingFactory messagingFactory, String linkName, String linkPath)
	{
		final RequestResponseLink requestReponseLink = new RequestResponseLink(messagingFactory, linkName, linkPath);
		
		Timer.schedule(
				new Runnable()
				{
					public void run()
					{
						if (!requestReponseLink.createFuture.isDone())
						{
							Exception operationTimedout = new TimeoutException(
									String.format(Locale.US, "Open operation on RequestResponseLink(%s) on Entity(%s) timed out at %s.", requestReponseLink.getClientId(), requestReponseLink.linkPath, ZonedDateTime.now().toString()));

							if (TRACE_LOGGER.isLoggable(Level.WARNING))
							{
								TRACE_LOGGER.log(Level.WARNING, operationTimedout.getMessage());
							}

							requestReponseLink.createFuture.completeExceptionally(operationTimedout);
						}
					}
				}
				, messagingFactory.getOperationTimeout()
				, TimerType.OneTimeRun);
	
		try
		{
			messagingFactory.scheduleOnReactorThread(new DispatchHandler()
			{
				@Override
				public void onEvent()
				{
					requestReponseLink.createInternalLinks();					
					requestReponseLink.amqpSender.openFuture.runAfterBothAsync(requestReponseLink.amqpReceiver.openFuture, () -> requestReponseLink.createFuture.complete(requestReponseLink));
				}
			});
		}
		catch (IOException ioException)
		{
			requestReponseLink.createFuture.completeExceptionally(new ServiceBusException(false, "Failed to create internal links, see cause for more details.", ioException));
		}
		
		return requestReponseLink.createFuture;
	}
	
	public static String getRequestResponseLinkPath(String entityPath)
	{
		return entityPath + AmqpConstants.MANAGEMENT_ADDRESS_SEGMENT;
	}
	
	private RequestResponseLink(MessagingFactory messagingFactory, String linkName, String linkPath)
	{
		super(linkName, null);
		
		this.underlyingFactory = messagingFactory;
		this.linkPath = linkPath;
		this.amqpSender = new InternalSender(linkName + ":internalSender", this);
		this.amqpReceiver = new InternalReceiver(linkName + ":interalReceiver", this);
		this.pendingRequests = new ConcurrentHashMap<>();
		this.requestCounter = new AtomicInteger();
		
		this.createFuture = new CompletableFuture<RequestResponseLink>();
	}
	
	private void createInternalLinks()
	{
		this.replyTo = UUID.randomUUID().toString();
		
		Map commonLinkProperties = new HashMap();
		commonLinkProperties.put(ClientConstants.LINK_TIMEOUT_PROPERTY, Util.adjustServerTimeout(this.underlyingFactory.getOperationTimeout()).toMillis());
		
		// Create send link
		final Connection connection = this.underlyingFactory.getConnection();

		final Session session = connection.session();
		session.setOutgoingWindow(Integer.MAX_VALUE);
		session.open();
		BaseHandler.setHandler(session, new SessionHandler(this.linkPath));

		String sendLinkNamePrefix = StringUtil.getShortRandomString();
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
		sender.open();
		
		// Create receive link
		String receiveLinkNamePrefix = StringUtil.getShortRandomString();
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
		receiver.open();		
	}
	
	private void recreateInternalLinks() throws InterruptedException, ExecutionException
	{
		try
		{
			this.amqpSender.close();
		}
		catch(Exception ex)
		{
			// Ignore exceptions here
		}
		
		try
		{
			this.amqpReceiver.close();
		}
		catch(Exception ex)
		{
			// Ignore exceptions here
		}
		
		this.createInternalLinks();
		this.amqpSender.openFuture.thenComposeAsync((v) -> this.amqpReceiver.openFuture).get();
	}
	
	private void handleConnectionError(Exception exception)
	{
		// Complete all pending requests with exception, if any
		this.completeAllPendingRequestsWithException(exception);
		// Connection error. Recreate links.
		if((this.amqpSender.sendLink.getLocalState() == EndpointState.CLOSED || this.amqpSender.sendLink.getRemoteState() == EndpointState.CLOSED)
				|| (this.amqpReceiver.receiveLink.getLocalState() == EndpointState.CLOSED || this.amqpReceiver.receiveLink.getRemoteState() == EndpointState.CLOSED))
		try {
			this.recreateInternalLinks();
		} catch (InterruptedException | ExecutionException e) {
			this.closeAsync();
		}
	}
	
	private void completeAllPendingRequestsWithException(Exception exception)
	{
		for(RequestResponseWorkItem workItem : this.pendingRequests.values())
		{			
			workItem.getWork().completeExceptionally(exception);
			workItem.cancelTimeoutTask(true);
		}
		
		this.pendingRequests.clear();
	}
	
	public CompletableFuture<Message> requestAysnc(Message requestMessage, Duration timeout)
	{
		CompletableFuture<Message> responseFuture = new CompletableFuture<Message>();
		
		if(this.getIsClosingOrClosed())
		{			
			responseFuture.completeExceptionally(new ServiceBusException(false, "RequestResponseLink is closing or closed."));			
		}
		else
		{
			RequestResponseWorkItem workItem = new RequestResponseWorkItem(requestMessage, responseFuture, timeout);
			String requestId = "request:" +  this.requestCounter.incrementAndGet();
			requestMessage.setMessageId(requestId);
			requestMessage.setReplyTo(this.replyTo);
			this.pendingRequests.put(requestId, workItem);
			workItem.setTimeoutTask(this.scheduleRequestTimeout(requestId, timeout));
			this.amqpSender.sendRequest(requestMessage, false);			
		}
		
		return responseFuture;
	}
	
	private ScheduledFuture<?> scheduleRequestTimeout(String requestId, Duration timeout)
	{
		return Timer.schedule(new Runnable() {
				public void run()
				{
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
			// Retry on server busy and other retry-able status codes (what are other codes??)
			if(statusCode == ClientConstants.REQUEST_RESPONSE_SERVER_BUSY_STATUS_CODE)
			{			
				// error response
				Exception responseException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
				Duration retryInterval = this.underlyingFactory.getRetryPolicy().getNextRetryInterval(requestId, responseException, workItem.getTimeoutTracker().remaining());
				if (retryInterval == null)
				{
					// Either not retry-able or not enough time to retry
					this.exceptionallyCompleteRequest(requestId, responseException, false);
				}
				else
				{
					// Retry
					workItem.setLastKnownException(responseException);
					try {
						this.underlyingFactory.scheduleOnReactorThread((int) retryInterval.toMillis(),
								new DispatchHandler()
								{
									@Override
									public void onEvent()
									{
										RequestResponseLink.this.amqpSender.sendRequest(workItem.getRequest(), true);
									}
								});
					} catch (IOException e) {
						this.exceptionallyCompleteRequest(requestId, responseException, false);
					}
				}
			}
			else
			{
				this.pendingRequests.remove(requestId);
				workItem.getWork().complete(responseMessage);
				workItem.cancelTimeoutTask(true);
			}
		}
		else
		{
			System.out.println("Request with id:" + requestId + " not found in the requestresponse link.");
		}		
		
		return workItem;
	}

	@Override
	protected CompletableFuture<Void> onClose() {
		return this.amqpSender.closeAsync().thenCompose((v) -> this.amqpReceiver.closeAsync());
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
							if (TRACE_LOGGER.isLoggable(Level.WARNING))
							{
								TRACE_LOGGER.log(Level.WARNING, 
										String.format(Locale.US, "linkName[%s], %s call timedout", linkName, "Close"), operationTimedout);
							}
							
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
			super(clientId, parent);
			this.parent = parent;
			this.openFuture = new CompletableFuture<Void>();
			this.closeFuture = new CompletableFuture<Void>();
		}		

		@Override
		protected CompletableFuture<Void> onClose() {					
			if (!this.getIsClosed())
			{				
				if (this.receiveLink != null && this.receiveLink.getLocalState() != EndpointState.CLOSED)
				{
					this.receiveLink.close();
					this.parent.underlyingFactory.deregisterForConnectionError(this.receiveLink);
					RequestResponseLink.scheduleLinkCloseTimeout(this.closeFuture, this.parent.underlyingFactory.getOperationTimeout(), this.receiveLink.getName());
				}
				else
				{
					this.closeFuture.complete(null);
				}
			}
			
			return this.closeFuture;
		}

		@Override
		public void onOpenComplete(Exception completionException) {
			if(completionException == null)
			{
				this.openFuture.complete(null);
				
				// Send unlimited credit
				this.receiveLink.flow(Integer.MAX_VALUE);
			}
			else
			{
				this.openFuture.completeExceptionally(completionException);
			}			
		}

		@Override
		public void onError(Exception exception) {
			if(!this.openFuture.isDone())
			{
				this.openFuture.completeExceptionally(exception);
			}
			
			if(this.getIsClosingOrClosed())
			{
				if(!this.closeFuture.isDone())
				{
					this.closeFuture.completeExceptionally(exception);
				}
			}
			
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
						this.closeFuture.complete(null);
					}
					else
					{
						Exception exception = ExceptionUtil.toException(condition);
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
						this.openFuture.completeExceptionally(exception);
					}
					else
					{
						this.parent.handleConnectionError(exception);
					}
				}
			}						
		}

		@Override
		public void onReceiveComplete(Delivery delivery)
		{
			Message responseMessage = null;			
			int msgSize = delivery.pending();
			byte[] buffer = new byte[msgSize];			
			int read = this.receiveLink.recv(buffer, 0, msgSize);			
			responseMessage = Proton.message();
			responseMessage.decode(buffer, 0, read);
			
			delivery.disposition(Accepted.getInstance());
			delivery.settle();
			
			String requestMessageId = (String)responseMessage.getCorrelationId();
			if(requestMessageId != null)
			{
				this.parent.completeRequestWithResponse(requestMessageId, responseMessage);
			}
			else
			{
				System.out.println("RequestRespnseLink received a message with null correlationId.");
			}
		}		

		public void setReceiveLink(Receiver receiveLink) {
			if (this.receiveLink != null)
			{
				Receiver oldReceiver = this.receiveLink;
				this.parent.underlyingFactory.deregisterForConnectionError(oldReceiver);
			}
			
			this.parent.underlyingFactory.registerForConnectionError(receiveLink);
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
		private LinkedList<Message> pendingFreshSends;
		private LinkedList<Message> pendingRetrySends;
		private Object syncLock;
		private AtomicBoolean isSendLoopRunning;

		protected InternalSender(String clientId, RequestResponseLink parent) {
			super(clientId, parent);			
			this.parent = parent;
			this.availableCredit = new AtomicInteger(0);
			this.syncLock = new Object();
			this.isSendLoopRunning = new AtomicBoolean(false);
			this.pendingFreshSends = new LinkedList<>();
			this.pendingRetrySends = new LinkedList<>();
			this.openFuture = new CompletableFuture<Void>();
			this.closeFuture = new CompletableFuture<Void>();
		}

		@Override
		protected CompletableFuture<Void> onClose() {
			if (!this.getIsClosed())
			{				
				if (this.sendLink != null && this.sendLink.getLocalState() != EndpointState.CLOSED)
				{
					this.sendLink.close();
					this.parent.underlyingFactory.deregisterForConnectionError(this.sendLink);
					RequestResponseLink.scheduleLinkCloseTimeout(this.closeFuture, this.parent.underlyingFactory.getOperationTimeout(), this.sendLink.getName());
				}
				else
				{
					this.closeFuture.complete(null);
				}
			}
			
			return this.closeFuture;
		}

		@Override
		public void onOpenComplete(Exception completionException) {
			if(completionException == null)
			{
				this.openFuture.complete(null);
			}
			else
			{
				this.openFuture.completeExceptionally(completionException);
			}			
		}

		@Override
		public void onError(Exception exception) {
			if(!this.openFuture.isDone())
			{
				this.openFuture.completeExceptionally(exception);
			}
			
			if(this.getIsClosingOrClosed())
			{
				if(!this.closeFuture.isDone())
				{
					this.closeFuture.completeExceptionally(exception);
				}
			}
			
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
						this.closeFuture.complete(null);
					}
					else
					{
						Exception exception = ExceptionUtil.toException(condition);
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
						this.openFuture.completeExceptionally(exception);
					}
					else
					{
						this.parent.handleConnectionError(exception);
					}
				}
			}
		}
		
		public void sendRequest(Message requestMessage, boolean isRetry)
		{
			synchronized(this.syncLock)
			{
				if(isRetry)
				{
					this.pendingRetrySends.add(requestMessage);
				}
				else
				{
					this.pendingFreshSends.add(requestMessage);
				}				
			}
							
			if(!this.isSendLoopRunning.get())
			{
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
		}
		
		public void removeEnqueuedRequest(Message requestMessage, boolean isRetry)
		{
			synchronized(this.syncLock)
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
			this.availableCredit.addAndGet(creditIssued);			
			this.runSendLoop();
		}

		@Override
		public void onSendComplete(Delivery delivery) {
			// Doesn't happen as sends are settled on send			
		}		

		public void setSendLink(Sender sendLink) {
			if (this.sendLink != null)
			{
				Sender oldSender = this.sendLink;
				this.parent.underlyingFactory.deregisterForConnectionError(oldSender);
			}
			
			this.parent.underlyingFactory.registerForConnectionError(sendLink);
			this.sendLink = sendLink;			
		}
		
		private void runSendLoop()
		{
			if(this.isSendLoopRunning.compareAndSet(false, true))	
			{		
				try
				{
					while(this.availableCredit.get() > 0)
					{
						Message requestToBeSent = null;
						synchronized(syncLock)
						{
							// First send retries and then fresh ones
							requestToBeSent = this.pendingRetrySends.poll();
							if(requestToBeSent == null)
							{
								requestToBeSent = this.pendingFreshSends.poll();
								if(requestToBeSent == null)
								{
									break;
								}
							}							
						}
						
						Delivery delivery = this.sendLink.delivery(UUID.randomUUID().toString().getBytes());
						delivery.setMessageFormat(DeliveryImpl.DEFAULT_MESSAGE_FORMAT);
						
						Pair<byte[], Integer> encodedPair = null;
						try
						{
							encodedPair = Util.encodeMessageToOptimalSizeArray(requestToBeSent);						
						}
						catch(PayloadSizeExceededException exception)
						{
							this.parent.exceptionallyCompleteRequest((String)requestToBeSent.getMessageId(), new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s kb", ClientConstants.MAX_MESSAGE_LENGTH_BYTES / 1024), exception), false);
						}
						
						try
						{
							int sentMsgSize = this.sendLink.send(encodedPair.getFirstItem(), 0, encodedPair.getSecondItem());
							assert sentMsgSize == encodedPair.getSecondItem() : "Contract of the ProtonJ library for Sender.Send API changed";
							delivery.settle();
							this.availableCredit.decrementAndGet();
						}
						catch(Exception e)
						{
							this.parent.exceptionallyCompleteRequest((String)requestToBeSent.getMessageId(), e, false);
						}						
					}
				}
				finally
				{
					this.isSendLoopRunning.set(false);
				}
			}			
		}
	}	 
}
