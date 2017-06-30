package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.primitives.ExceptionUtil;
import com.microsoft.azure.servicebus.primitives.MessageLockLostException;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.OperationCancelledException;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.SessionLockLostException;
import com.microsoft.azure.servicebus.primitives.StringUtil;
import com.microsoft.azure.servicebus.primitives.TimeoutException;
import com.microsoft.azure.servicebus.primitives.Timer;
import com.microsoft.azure.servicebus.primitives.TimerType;

class MessageAndSessionPump extends InitializableEntity implements IMessageAndSessionPump
{
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(MessageAndSessionPump.class);
	// Larger value means few receive calls to the internal receiver and better.
	private static final Duration MESSAGE_RECEIVE_TIMEOUT = Duration.ofSeconds(60);
	private static final Duration MINIMUM_MESSAGE_LOCK_VALIDITY = Duration.ofSeconds(4);
	private static final Duration MAXIMUM_RENEW_LOCK_BUFFER = Duration.ofSeconds(10);	
	private static final Duration SLEEP_DURATION_ON_ACCEPT_SESSION_EXCEPTION = Duration.ofMinutes(1);
	
	private final ConcurrentHashMap<String, IMessageSession> openSessions;
	private final MessagingFactory factory;
	private final String entityPath;
	private final ReceiveMode receiveMode;
	private IMessageReceiver innerReceiver;
	
	private boolean handlerRegistered = false;
	private IMessageHandler messageHandler;
	private ISessionHandler sessionHandler;
	private MessageHandlerOptions messageHandlerOptions;
	private SessionHandlerOptions sessionHandlerOptions;	
	
	public MessageAndSessionPump(MessagingFactory factory, String entityPath, ReceiveMode receiveMode)
	{
		super(StringUtil.getShortRandomString(), null);
		this.factory = factory;
		this.entityPath = entityPath;
		this.receiveMode = receiveMode;
		this.openSessions = new ConcurrentHashMap<>();
	}

	@Override
	public void registerMessageHandler(IMessageHandler handler) throws InterruptedException, ServiceBusException{
		this.registerMessageHandler(handler, new MessageHandlerOptions());		
	}

	@Override
	public void registerMessageHandler(IMessageHandler handler, MessageHandlerOptions handlerOptions) throws InterruptedException, ServiceBusException{
	    TRACE_LOGGER.info("Registering message handler on entity '{}' with '{}'", this.entityPath, handlerOptions);
		this.setHandlerRegistered();
		this.messageHandler = handler;
		this.messageHandlerOptions = handlerOptions;
		
		this.innerReceiver =  ClientFactory.createMessageReceiverFromEntityPath(this.factory, this.entityPath, this.receiveMode);
		TRACE_LOGGER.info("Created MessageReceiver to entity '{}'", this.entityPath);
		for(int i=0; i<handlerOptions.getMaxConcurrentCalls(); i++)
		{
			this.receiveAndPumpMessage();
		}
	}

	@Override
	public void registerSessionHandler(ISessionHandler handler) throws InterruptedException, ServiceBusException
	{
		this.registerSessionHandler(handler, new SessionHandlerOptions());		
	}

	@Override
	public void registerSessionHandler(ISessionHandler handler, SessionHandlerOptions handlerOptions) throws InterruptedException, ServiceBusException
	{
	    TRACE_LOGGER.info("Registering session handler on entity '{}' with '{}'", this.entityPath, handlerOptions);
		this.setHandlerRegistered();
		this.sessionHandler = handler;
		this.sessionHandlerOptions = handlerOptions;
		
		for(int i=0; i<handlerOptions.getMaxConcurrentSessions(); i++)
		{
			this.acceptSessionsAndPumpMessage();
		}
	}
	
	private synchronized void setHandlerRegistered()
	{
		this.throwIfClosed(null);
		
		// Only one handler is allowed to be registered per client
		if(this.handlerRegistered)
		{
			throw new UnsupportedOperationException("MessageHandler or SessionHandler already registered.");
		}
		
		this.handlerRegistered = true;
	}

	private void receiveAndPumpMessage()
	{
		if(!this.getIsClosingOrClosed())
		{
			CompletableFuture<IMessage> receiveMessageFuture = this.innerReceiver.receiveAsync(MessageAndSessionPump.MESSAGE_RECEIVE_TIMEOUT);
			receiveMessageFuture.handleAsync((message, receiveEx) -> {
				if(receiveEx != null)
				{
					receiveEx = ExceptionUtil.extractAsyncCompletionCause(receiveEx);
					TRACE_LOGGER.error("Receiving message from entity '{}' failed.", this.entityPath, receiveEx);
					this.notifyExceptionToMessageHandler(receiveEx, ExceptionPhase.RECEIVE);
					this.receiveAndPumpMessage();
				}
				else
				{
					if(message == null)
					{
					    TRACE_LOGGER.debug("Receive from entity '{}' returned no messages.", this.entityPath);
						this.receiveAndPumpMessage();
					}
					else
					{
					    TRACE_LOGGER.trace("Message with sequence number '{}' received from entity '{}'.", message.getSequenceNumber(), this.entityPath);
						// Start renew lock loop
						final MessgeRenewLockLoop renewLockLoop;													
						if(this.innerReceiver.getReceiveMode() == ReceiveMode.PEEKLOCK)
						{
							Instant stopRenewMessageLockAt = Instant.now().plus(this.messageHandlerOptions.getMaxAutoRenewDuration());
							renewLockLoop = new MessgeRenewLockLoop(this.innerReceiver, this, message, stopRenewMessageLockAt);
							renewLockLoop.startLoop();
							TRACE_LOGGER.trace("Started loop to renew lock on message with sequence number '{}' until '{}'", message.getSequenceNumber(), stopRenewMessageLockAt);
						}
						else
						{
							renewLockLoop = null;
						}
						
						CompletableFuture<Void> onMessageFuture;
						try
						{
						    TRACE_LOGGER.debug("Invoking onMessage with message containing sequence number '{}'", message.getSequenceNumber());
							onMessageFuture = this.messageHandler.onMessageAsync(message);
						}
						catch(Exception onMessageSyncEx)
						{
						    TRACE_LOGGER.error("Invocation of onMessage with message containing sequence number '{}' threw unexpected exception", message.getSequenceNumber(), onMessageSyncEx);
							onMessageFuture = new CompletableFuture<Void>();
							onMessageFuture.completeExceptionally(onMessageSyncEx);			
						}						
						
						onMessageFuture.handleAsync((v, onMessageEx) -> {
							if(onMessageEx != null)
							{
								onMessageEx = ExceptionUtil.extractAsyncCompletionCause(onMessageEx);
								TRACE_LOGGER.error("onMessage with message containing sequence number '{}' threw exception", message.getSequenceNumber(), onMessageEx);
								this.notifyExceptionToMessageHandler(onMessageEx, ExceptionPhase.USERCALLBACK);
							}
							if(this.innerReceiver.getReceiveMode() == ReceiveMode.PEEKLOCK)
							{
								if(renewLockLoop != null)
								{								    
									renewLockLoop.cancelLoop();
									TRACE_LOGGER.trace("Cancelled loop to renew lock on message with sequence number '{}'", message.getSequenceNumber());
								}
								CompletableFuture<Void> updateDispositionFuture;
								ExceptionPhase dispositionPhase;
								if(onMessageEx == null)
								{
									// Complete message
									dispositionPhase = ExceptionPhase.COMPLETE;
									if(this.messageHandlerOptions.isAutoComplete())
									{
									    TRACE_LOGGER.debug("Completing message with sequence number '{}'", message.getSequenceNumber());
										updateDispositionFuture = this.innerReceiver.completeAsync(message.getLockToken());
									}
									else
									{
										updateDispositionFuture = CompletableFuture.completedFuture(null);
									}
								}
								else
								{									
									// Abandon message
								    TRACE_LOGGER.debug("Abandoning message with sequence number '{}'", message.getSequenceNumber());
									dispositionPhase = ExceptionPhase.ABANDON;
									updateDispositionFuture = this.innerReceiver.abandonAsync(message.getLockToken());
								}
								
								updateDispositionFuture.handleAsync((u, updateDispositionEx) -> {
									if(updateDispositionEx != null)
									{									    
										updateDispositionEx = ExceptionUtil.extractAsyncCompletionCause(updateDispositionEx);
										TRACE_LOGGER.error("{} message with sequence number '{}' failed", dispositionPhase == ExceptionPhase.COMPLETE ? "Completing" : "Abandoning", message.getSequenceNumber(), updateDispositionEx);
										this.notifyExceptionToMessageHandler(updateDispositionEx, dispositionPhase);
									}
									this.receiveAndPumpMessage();
									return null;
								});
							}
							else
							{
								this.receiveAndPumpMessage();
							}
							
							return null;
						});
					}
					
				}
				
				return null;
			});
		}		
	}
	
	private void acceptSessionsAndPumpMessage()
	{
		if(!this.getIsClosingOrClosed())
		{
		    TRACE_LOGGER.debug("Accepting a session from entity '{}'", this.entityPath);
			CompletableFuture<IMessageSession> acceptSessionFuture = ClientFactory.acceptSessionFromEntityPathAsync(this.factory, this.entityPath, null, this.receiveMode);
			acceptSessionFuture.handleAsync((session, acceptSessionEx) -> {
				if(acceptSessionEx != null)
				{
					acceptSessionEx = ExceptionUtil.extractAsyncCompletionCause(acceptSessionEx);
					
					// Timeout exception means no session available.. it is expected so no need to notify client
					if(!(acceptSessionEx instanceof TimeoutException))
					{
					    TRACE_LOGGER.error("Accepting a session from entity '{}' failed.", this.entityPath, acceptSessionEx);
						this.notifyExceptionToSessionHandler(acceptSessionEx, ExceptionPhase.ACCEPTSESSION);
					}
					
					if(!(acceptSessionEx instanceof OperationCancelledException))
					{
						// don't retry if OperationCancelled by service.. may be entity itself is deleted
						// In case of any other exception, sleep and retry
					    TRACE_LOGGER.debug("AcceptSession from entity '{}' will be retried after '{}'.", this.entityPath, SLEEP_DURATION_ON_ACCEPT_SESSION_EXCEPTION);
						Timer.schedule(() -> {MessageAndSessionPump.this.acceptSessionsAndPumpMessage();}, SLEEP_DURATION_ON_ACCEPT_SESSION_EXCEPTION, TimerType.OneTimeRun);
					}					
				}
				else
				{
					// Received a session.. Now pump messages..
				    TRACE_LOGGER.debug("Accepted a session '{}' from entity '{}'", session.getSessionId(), this.entityPath);
					this.openSessions.put(session.getSessionId(), session);
					SessionRenewLockLoop sessionRenewLockLoop = new SessionRenewLockLoop(session, this);
					sessionRenewLockLoop.startLoop();
					TRACE_LOGGER.debug("Started loop to renew lock on session '{}'", session.getSessionId());
					SessionTracker sessionTracker = new SessionTracker(this, session, sessionRenewLockLoop);
					for(int i=0; i<this.sessionHandlerOptions.getMaxConcurrentCallsPerSession(); i++)
					{
						this.receiveFromSessionAndPumpMessage(sessionTracker);
					}					
				}
				
				return null;
			});
		}
	}
	
	private void receiveFromSessionAndPumpMessage(SessionTracker sessionTracker)
	{
		if(!this.getIsClosingOrClosed())
		{
			IMessageSession session = sessionTracker.getSession();
			CompletableFuture<IMessage> receiverFuture = session.receiveAsync(MessageAndSessionPump.MESSAGE_RECEIVE_TIMEOUT);
			receiverFuture.handleAsync((message, receiveEx) -> {
				if(receiveEx != null)
				{
					receiveEx = ExceptionUtil.extractAsyncCompletionCause(receiveEx);
					TRACE_LOGGER.error("Receiving message from session '{}' on entity '{}' failed.", session.getSessionId(), this.entityPath, receiveEx);
					this.notifyExceptionToSessionHandler(receiveEx, ExceptionPhase.RECEIVE);
					sessionTracker.shouldRetryOnNoMessageOrException().thenAcceptAsync((shouldRetry) -> {
						if(shouldRetry)
						{
							this.receiveFromSessionAndPumpMessage(sessionTracker);
						}
					});
				}
				else
				{
					if(message == null)
					{
					    TRACE_LOGGER.debug("Receive from from session '{}' on entity '{}' returned no messages.", session.getSessionId(), this.entityPath);
						sessionTracker.shouldRetryOnNoMessageOrException().thenAcceptAsync((shouldRetry) -> {
							if(shouldRetry)
							{
								this.receiveFromSessionAndPumpMessage(sessionTracker);
							}
						});						
					}
					else
					{
					    TRACE_LOGGER.trace("Message with sequence number '{}' received from session '{}' on entity '{}'.", message.getSequenceNumber(), session.getSessionId(), this.entityPath);
						sessionTracker.notifyMessageReceived();
						// There is no need to renew message locks as session messages are locked for a day
						ScheduledFuture<?> renewCancelTimer = Timer.schedule(() -> {
						        TRACE_LOGGER.warn("onMessage task timed out. Cancelling loop to renew lock on session '{}'", session.getSessionId());
							    sessionTracker.sessionRenewLockLoop.cancelLoop();
							},
							this.sessionHandlerOptions.getMaxAutoRenewDuration(),
							TimerType.OneTimeRun);
						TRACE_LOGGER.debug("Invoking onMessage with message containing sequence number '{}'", message.getSequenceNumber());
						CompletableFuture<Void> onMessageFuture;
						try
						{
							onMessageFuture = this.sessionHandler.onMessageAsync(session, message);
						}
						catch(Exception onMessageSyncEx)
						{
						    TRACE_LOGGER.error("Invocation of onMessage with message containing sequence number '{}' threw unexpected exception", message.getSequenceNumber(), onMessageSyncEx);
							onMessageFuture = new CompletableFuture<Void>();
							onMessageFuture.completeExceptionally(onMessageSyncEx);
						}						
						
						onMessageFuture.handleAsync((v, onMessageEx) -> {
							renewCancelTimer.cancel(true);
							if(onMessageEx != null)
							{
								onMessageEx = ExceptionUtil.extractAsyncCompletionCause(onMessageEx);
								TRACE_LOGGER.error("onMessage with message containing sequence number '{}' threw exception", message.getSequenceNumber(), onMessageEx);
								this.notifyExceptionToSessionHandler(onMessageEx, ExceptionPhase.USERCALLBACK);
							}
							if(this.receiveMode == ReceiveMode.PEEKLOCK)
							{								
								CompletableFuture<Void> updateDispositionFuture;
								ExceptionPhase dispositionPhase;
								if(onMessageEx == null)
								{
									// Complete message
									dispositionPhase = ExceptionPhase.COMPLETE;
									if(this.sessionHandlerOptions.isAutoComplete())
									{
									    TRACE_LOGGER.debug("Completing message with sequence number '{}'", message.getSequenceNumber());
										updateDispositionFuture = session.completeAsync(message.getLockToken());
									}
									else
									{
										updateDispositionFuture = CompletableFuture.completedFuture(null);
									}									
								}
								else
								{									
									// Abandon message
								    TRACE_LOGGER.debug("Abandoning message with sequence number '{}'", message.getSequenceNumber());
									dispositionPhase = ExceptionPhase.ABANDON;
									updateDispositionFuture = session.abandonAsync(message.getLockToken());
								}
								
								updateDispositionFuture.handleAsync((u, updateDispositionEx) -> {
									if(updateDispositionEx != null)
									{
										updateDispositionEx = ExceptionUtil.extractAsyncCompletionCause(updateDispositionEx);
										TRACE_LOGGER.error("{} message with sequence number '{}' failed", dispositionPhase == ExceptionPhase.COMPLETE ? "Completing" : "Abandoning", message.getSequenceNumber(), updateDispositionEx);
										this.notifyExceptionToSessionHandler(updateDispositionEx, dispositionPhase);
									}
									this.receiveFromSessionAndPumpMessage(sessionTracker);
									return null;
								});
							}
							else
							{
								this.receiveFromSessionAndPumpMessage(sessionTracker);
							}
							
							return null;
						});
					}
					
				}
				
				return null;
			});
		}		
	}

	@Override
	CompletableFuture<Void> initializeAsync(){
		return CompletableFuture.completedFuture(null);
	}

	@Override
	protected CompletableFuture<Void> onClose() {
	    TRACE_LOGGER.info("Closing message and session pump on entity '{}'", this.entityPath);
		CompletableFuture[] closeFutures = new CompletableFuture[this.openSessions.size() + 1];
		int arrayIndex = 0;
		for(IMessageSession session : this.openSessions.values())
		{
			closeFutures[arrayIndex++] = session.closeAsync();
		}
		closeFutures[arrayIndex] = this.innerReceiver == null ? CompletableFuture.completedFuture(null) : this.innerReceiver.closeAsync();		
		return CompletableFuture.allOf(closeFutures);		 
	}
	
	private static class SessionTracker
	{
		private final int numberReceivingThreads;
		private final IMessageSession session;
		private final MessageAndSessionPump messageAndSessionPump;
		private final SessionRenewLockLoop sessionRenewLockLoop;
		private int waitingRetryThreads;
		private CompletableFuture<Boolean> retryFuture;		
		 
		SessionTracker(MessageAndSessionPump messageAndSessionPump, IMessageSession session, SessionRenewLockLoop sessionRenewLockLoop)
		{
			this.messageAndSessionPump = messageAndSessionPump;
			this.session = session;
			this.sessionRenewLockLoop = sessionRenewLockLoop;
			this.numberReceivingThreads = messageAndSessionPump.sessionHandlerOptions.getMaxConcurrentCallsPerSession();
			this.waitingRetryThreads = 0;
		}
		
		public IMessageSession getSession()
		{
			return this.session;
		}
		
		synchronized void notifyMessageReceived()
		{
		    TRACE_LOGGER.trace("Message received from session '{}'", this.session.getSessionId());
			if(this.retryFuture != null && !this.retryFuture.isDone())
			{
				this.waitingRetryThreads = 0;
				this.retryFuture.complete(true);		
			}
		}
		
		synchronized CompletableFuture<Boolean> shouldRetryOnNoMessageOrException()
		{			
			if(this.retryFuture == null || this.retryFuture.isDone())
			{
				this.retryFuture = new CompletableFuture<Boolean>();
			}
			this.waitingRetryThreads++;
			if(this.waitingRetryThreads == this.numberReceivingThreads)
			{
			    TRACE_LOGGER.info("No messages recevied by any receive call from session '{}'. Closing the session.", this.session.getSessionId());
				this.retryFuture.complete(false);
				
				// close current session and accept another session
				ScheduledFuture<?> renewCancelTimer = Timer.schedule(() -> {
				        TRACE_LOGGER.warn("Closing session timed out. Cancelling loop to renew lock on session '{}'", this.session.getSessionId());
					    SessionTracker.this.sessionRenewLockLoop.cancelLoop();
					},
					this.messageAndSessionPump.sessionHandlerOptions.getMaxAutoRenewDuration(),
					TimerType.OneTimeRun);
				CompletableFuture<Void> onCloseFuture;
				try
				{
					onCloseFuture = this.messageAndSessionPump.sessionHandler.OnCloseSessionAsync(session);					
				}
				catch(Exception onCloseSyncEx)
				{
				    TRACE_LOGGER.error("Invocation of onCloseSession on session '{}' threw unexpected exception", this.session.getSessionId(), onCloseSyncEx);
					onCloseFuture = new CompletableFuture<Void>();
					onCloseFuture.completeExceptionally(onCloseSyncEx);
				}			
				
				onCloseFuture.handleAsync((v, onCloseEx) -> {
					renewCancelTimer.cancel(true);
					if(onCloseEx != null)
					{
						onCloseEx = ExceptionUtil.extractAsyncCompletionCause(onCloseEx);
						TRACE_LOGGER.error("onCloseSession on session '{}' threw exception", session.getSessionId(), onCloseEx);
						this.messageAndSessionPump.notifyExceptionToSessionHandler(onCloseEx, ExceptionPhase.USERCALLBACK);
					}
					
					this.sessionRenewLockLoop.cancelLoop();
					TRACE_LOGGER.debug("Cancelled loop to renew lock on session '{}'", this.session.getSessionId());
					this.session.closeAsync().handleAsync((z, closeEx) ->
					{
						if(closeEx != null)
						{
							closeEx = ExceptionUtil.extractAsyncCompletionCause(closeEx);
							TRACE_LOGGER.info("Closing session '{}' from entity '{}' failed", this.session.getSessionId(), this.messageAndSessionPump.entityPath, closeEx);
							this.messageAndSessionPump.notifyExceptionToSessionHandler(closeEx, ExceptionPhase.SESSIONCLOSE);
						}
						else
						{
						    TRACE_LOGGER.info("Closed session '{}' from entity '{}'", this.session.getSessionId(), this.messageAndSessionPump.entityPath);
						}
						
						this.messageAndSessionPump.openSessions.remove(this.session.getSessionId());
						this.messageAndSessionPump.acceptSessionsAndPumpMessage();
						return null;
					});
					return null;
				});
				
			}
			
			return this.retryFuture;
		}		
	}
	
	private abstract static class RenewLockLoop
	{
		private boolean cancelled = false;
		
		protected RenewLockLoop()
		{			
		}
		
		protected abstract void loop();		
		
		protected abstract ScheduledFuture<?> getTimerFuture();
		
		protected boolean isCancelled()
		{
			return this.cancelled;
		}
		
		public void startLoop()
		{
			this.loop();
		}
		
		public void cancelLoop()
		{
			if(!this.cancelled)
			{
				this.cancelled = true;
				ScheduledFuture<?> timerFuture = this.getTimerFuture();
				if(timerFuture != null && !timerFuture.isDone())
				{
					timerFuture.cancel(true);
				}
			}			
		}
		
		protected static Duration getNextRenewInterval(Instant lockedUntilUtc)
		{			
			Duration remainingTime = Duration.between(Instant.now(), lockedUntilUtc);
			if(remainingTime.isNegative())
			{
			    
				// Lock likely expired. May be there is clock skew. Assume some minimum time
				remainingTime = MessageAndSessionPump.MINIMUM_MESSAGE_LOCK_VALIDITY;
				TRACE_LOGGER.warn("Lock already expired. May be there is clock skew. Still trying to renew lock");
			}
			
			Duration buffer = remainingTime.dividedBy(2).compareTo(MAXIMUM_RENEW_LOCK_BUFFER) > 0 ? MAXIMUM_RENEW_LOCK_BUFFER : remainingTime.dividedBy(2);
			TRACE_LOGGER.debug("Lock is valid for '{}'. It will be renewed '{}' before it expires.", remainingTime, buffer);
			return remainingTime.minus(buffer);
		}
	}
	
	private static class MessgeRenewLockLoop extends RenewLockLoop
	{
		private IMessageReceiver innerReceiver;
		private MessageAndSessionPump messageAndSessionPump;
		private IMessage message;
		private Instant stopRenewalAt;
		ScheduledFuture<?> timerFuture;
		
		MessgeRenewLockLoop(IMessageReceiver innerReceiver, MessageAndSessionPump messageAndSessionPump, IMessage message, Instant stopRenewalAt)
		{
			super();
			this.innerReceiver = innerReceiver;
			this.messageAndSessionPump = messageAndSessionPump;
			this.message = message;
			this.stopRenewalAt = stopRenewalAt;			
		}
		
		@Override
		protected ScheduledFuture<?> getTimerFuture()
		{
			return this.timerFuture;
		}
		
		@Override
		protected void loop()
		{
			if(!this.isCancelled())
			{
				Duration renewInterval = this.getNextRenewInterval();
				if(renewInterval != null && !renewInterval.isNegative())
				{
					this.timerFuture = Timer.schedule(() -> {
					    TRACE_LOGGER.debug("Renewing lock on message with sequence number '{}'", this.message.getSequenceNumber());
						this.innerReceiver.renewMessageLockAsync(message).handleAsync((v, renewLockEx) ->
						{
							if(renewLockEx != null)
							{
								renewLockEx = ExceptionUtil.extractAsyncCompletionCause(renewLockEx);
								TRACE_LOGGER.error("Renewing lock on message with sequence number '{}' failed", this.message.getSequenceNumber(), renewLockEx);
								this.messageAndSessionPump.notifyExceptionToMessageHandler(renewLockEx, ExceptionPhase.RENEWMESSAGELOCK);
								if(!(renewLockEx instanceof MessageLockLostException || renewLockEx instanceof OperationCancelledException))
								{
									this.loop();
								}
							}
							else
							{
							    TRACE_LOGGER.debug("Renewed lock on message with sequence number '{}'", this.message.getSequenceNumber());
								this.loop();
							}
							
							
							return null;
						});
					}, renewInterval, TimerType.OneTimeRun);
				}
			}
		}		
		
		private Duration getNextRenewInterval()
		{			
			if(this.message.getLockedUntilUtc().isBefore(stopRenewalAt))
			{
				return RenewLockLoop.getNextRenewInterval(this.message.getLockedUntilUtc());
			}
			else
			{
				return null;
			}			
		}
	}
	
	private static class SessionRenewLockLoop extends RenewLockLoop
	{
		private IMessageSession session;
		private MessageAndSessionPump messageAndSessionPump;
		ScheduledFuture<?> timerFuture;
		
		SessionRenewLockLoop(IMessageSession session, MessageAndSessionPump messageAndSessionPump)
		{
			super();
			this.session = session;
			this.messageAndSessionPump = messageAndSessionPump;
		}
		
		@Override
		protected ScheduledFuture<?> getTimerFuture()
		{
			return this.timerFuture;
		}
		
		@Override
		protected void loop()
		{
			if(!this.isCancelled())
			{
				Duration renewInterval = RenewLockLoop.getNextRenewInterval(this.session.getLockedUntilUtc());
				if(renewInterval != null && !renewInterval.isNegative())
				{
					this.timerFuture = Timer.schedule(() -> {
					    TRACE_LOGGER.debug("Renewing lock on session '{}'", this.session.getSessionId());
						this.session.renewSessionLockAsync().handleAsync((v, renewLockEx) ->
						{
							if(renewLockEx != null)
							{
								renewLockEx = ExceptionUtil.extractAsyncCompletionCause(renewLockEx);
								TRACE_LOGGER.error("Renewing lock on session '{}' failed", this.session.getSessionId(), renewLockEx);			
								this.messageAndSessionPump.notifyExceptionToSessionHandler(renewLockEx, ExceptionPhase.RENEWSESSIONLOCK);
								if(!(renewLockEx instanceof SessionLockLostException || renewLockEx instanceof OperationCancelledException))
								{
									this.loop();
								}
							}
							else
							{
							    TRACE_LOGGER.debug("Renewed lock on session '{}'", this.session.getSessionId());
								this.loop();
							}
							
							
							return null;
						});
					}, renewInterval, TimerType.OneTimeRun);
				}
			}
		}
	}

	@Override
	public void abandon(UUID lockToken) throws InterruptedException, ServiceBusException {
		this.checkInnerReceiveCreated();
		this.innerReceiver.abandon(lockToken);
	}

	@Override
	public void abandon(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
		this.checkInnerReceiveCreated();
		this.innerReceiver.abandon(lockToken, propertiesToModify);		
	}

	@Override
	public CompletableFuture<Void> abandonAsync(UUID lockToken) {
		this.checkInnerReceiveCreated();
		return this.innerReceiver.abandonAsync(lockToken);
	}

	@Override
	public CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
		this.checkInnerReceiveCreated();
		return this.innerReceiver.abandonAsync(lockToken, propertiesToModify);
	}

	@Override
	public void complete(UUID lockToken) throws InterruptedException, ServiceBusException {
		this.checkInnerReceiveCreated();
		this.innerReceiver.complete(lockToken);
	}

	@Override
	public CompletableFuture<Void> completeAsync(UUID lockToken) {
		this.checkInnerReceiveCreated();
		return this.innerReceiver.completeAsync(lockToken);
	}

//	@Override
	void defer(UUID lockToken) throws InterruptedException, ServiceBusException {
		this.checkInnerReceiveCreated();
		this.innerReceiver.defer(lockToken);
	}

//	@Override
	void defer(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
		this.checkInnerReceiveCreated();
		this.innerReceiver.defer(lockToken, propertiesToModify);	
	}

	@Override
	public CompletableFuture<Void> deferAsync(UUID lockToken) {
		this.checkInnerReceiveCreated();
		return this.innerReceiver.abandonAsync(lockToken);
	}

	@Override
	public CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
		this.checkInnerReceiveCreated();
		return this.innerReceiver.abandonAsync(lockToken, propertiesToModify);
	}

	@Override
	public void deadLetter(UUID lockToken) throws InterruptedException, ServiceBusException {
		this.innerReceiver.deadLetter(lockToken);		
	}

	@Override
	public void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
		this.innerReceiver.deadLetter(lockToken, propertiesToModify);		
	}

	@Override
	public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) throws InterruptedException, ServiceBusException {
		this.checkInnerReceiveCreated();
		this.innerReceiver.deadLetter(lockToken, deadLetterReason, deadLetterErrorDescription);		
	}

	@Override
	public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
		this.checkInnerReceiveCreated();
		this.innerReceiver.deadLetter(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify);		
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken) {
		this.checkInnerReceiveCreated();
		return this.innerReceiver.deadLetterAsync(lockToken);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
		this.checkInnerReceiveCreated();
		return this.innerReceiver.deadLetterAsync(lockToken, propertiesToModify);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason,	String deadLetterErrorDescription) {
		this.checkInnerReceiveCreated();
		return this.innerReceiver.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {
		this.checkInnerReceiveCreated();
		return this.innerReceiver.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify);
	}
	
	private void checkInnerReceiveCreated()
	{
		if(this.innerReceiver == null)
		{
			throw new UnsupportedOperationException("This operation is not supported on a message received from a session. Use the session to perform the operation.");
		}
	}
	
	// Don't notify handler if the pump is already closed
	private void notifyExceptionToSessionHandler(Throwable ex, ExceptionPhase phase)
	{
		if(!(ex instanceof IllegalStateException && this.getIsClosingOrClosed()))
		{
			this.sessionHandler.notifyException(ex, phase);
		}
	}
	
	private void notifyExceptionToMessageHandler(Throwable ex, ExceptionPhase phase)
	{
		if(!(ex instanceof IllegalStateException && this.getIsClosingOrClosed()))
		{
			this.messageHandler.notifyException(ex, phase);
		}
	}

    @Override
    public int getPrefetchCount() {
        this.checkInnerReceiveCreated();
        return this.innerReceiver.getPrefetchCount();
    }

    @Override
    public void setPrefetchCount(int prefetchCount) throws ServiceBusException {
        this.checkInnerReceiveCreated();
        this.innerReceiver.setPrefetchCount(prefetchCount);
    }
}
