/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnknownDescribedType;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.amqp.DispatchHandler;
import com.microsoft.azure.servicebus.amqp.IAmqpReceiver;
import com.microsoft.azure.servicebus.amqp.ReceiveLinkHandler;

/**
 * Common Receiver that abstracts all amqp related details
 * translates event-driven reactor model into async receive Api
 */
public final class MessageReceiver extends ClientEntity implements IAmqpReceiver, IErrorContextProvider
{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	private static final int MIN_TIMEOUT_DURATION_MILLIS = 20;

	private final ConcurrentLinkedQueue<ReceiveWorkItem> pendingReceives;
	private final MessagingFactory underlyingFactory;
	private final String receivePath;
	private final Runnable onOperationTimedout;
	private final Duration operationTimeout;
	private final CompletableFuture<Void> linkClose;
	private final Object prefetchCountSync;
	private final IReceiverSettingsProvider settingsProvider;
        private final String sessionId;
        private final String targetPath;
        private final SenderSettleMode serviceSettleMode;

	private int prefetchCount;
        private ConcurrentLinkedQueue<Message> prefetchedMessages;
	private Receiver receiveLink;
	private WorkItem<MessageReceiver> linkOpen;
	private Duration receiveTimeout;
        private Message lastReceivedMessage;
	private Exception lastKnownLinkError;
	private int nextCreditToFlow;
        private boolean creatingLink;

	private MessageReceiver(final MessagingFactory factory,
			final String name, 
			final String recvPath,
			final int prefetchCount,
			final IReceiverSettingsProvider settingsProvider,
                        final String sessionId,
                        final String receiveLinkTargetPath,
                        final SenderSettleMode serviceSettleMode,
                        final Duration openTimeout)
	{
		super(name, factory);

		this.underlyingFactory = factory;
		this.operationTimeout = factory.getOperationTimeout();
		this.receivePath = recvPath;
		this.prefetchCount = prefetchCount;
		this.prefetchedMessages = new ConcurrentLinkedQueue<>();
		this.linkClose = new CompletableFuture<>();
		this.lastKnownLinkError = null;
		this.receiveTimeout = factory.getOperationTimeout();
		this.prefetchCountSync = new Object();
                this.settingsProvider = settingsProvider;
                this.sessionId = sessionId == null ? StringUtil.getRandomString() : sessionId;
                this.targetPath = receiveLinkTargetPath;
                this.serviceSettleMode = serviceSettleMode;
                this.linkOpen = new WorkItem<>(
                        new CompletableFuture<>(),
                        openTimeout == null ? factory.getOperationTimeout() : openTimeout);
		
		this.pendingReceives = new ConcurrentLinkedQueue<>();

		// onOperationTimeout delegate - per receive call
		this.onOperationTimedout = new Runnable()
		{
			public void run()
			{
				WorkItem<Collection<Message>> topWorkItem = null;
				boolean workItemTimedout = false;
				while((topWorkItem = MessageReceiver.this.pendingReceives.peek()) != null)
				{
					if (topWorkItem.getTimeoutTracker().remaining().toMillis() <= MessageReceiver.MIN_TIMEOUT_DURATION_MILLIS)
					{
						WorkItem<Collection<Message>> dequedWorkItem = MessageReceiver.this.pendingReceives.poll();
						if (dequedWorkItem != null)
						{
							workItemTimedout = true;
							dequedWorkItem.getWork().complete(null);
						}
						else
							break;
					}
					else
					{
						MessageReceiver.this.scheduleOperationTimer(topWorkItem.getTimeoutTracker());
						break;
					}
				}

				if (workItemTimedout)
				{
					// workaround to push the sendflow-performative to reactor
					// this sets the receiveLink endpoint to modified state
					// (and increment the unsentCredits in proton by 0)
					try
					{
						MessageReceiver.this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler()
						{
							@Override
							public void onEvent()
							{
								MessageReceiver.this.receiveLink.flow(0);
							}
						});
					}
					catch (IOException ignore)
					{
					}
				}
			}
		};
	}
        
        public static CompletableFuture<MessageReceiver> create(
			final MessagingFactory factory, 
			final String name, 
			final String recvPath,
			final int prefetchCount,
			final IReceiverSettingsProvider settingsProvider)
	{
            return MessageReceiver.create(factory, name, recvPath, prefetchCount, settingsProvider, null, null, SenderSettleMode.UNSETTLED, null);
        }

	// @param connection Connection on which the MessageReceiver's receive Amqp link need to be created on.
	// Connection has to be associated with Reactor before Creating a receiver on it.
	public static CompletableFuture<MessageReceiver> create(
			final MessagingFactory factory, 
			final String name, 
			final String recvPath,
			final int prefetchCount,
			final IReceiverSettingsProvider settingsProvider,
                        final String sessionId,
                        final String receiveLinkTargetPath,
                        final SenderSettleMode serviceSettleMode,
                        final Duration openTimeout)
	{
		MessageReceiver msgReceiver = new MessageReceiver(
                        factory,
                        name,
                        recvPath,
                        prefetchCount,
                        settingsProvider,
                        sessionId,
                        receiveLinkTargetPath,
                        serviceSettleMode,
                        openTimeout);
		return msgReceiver.createLink();
	}
        
        public String getReceivePath()
	{
		return this.receivePath;
	}

	private CompletableFuture<MessageReceiver> createLink()
	{
		this.scheduleLinkOpenTimeout(this.linkOpen.getTimeoutTracker());
		try
		{
			this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler()
			{
				@Override
				public void onEvent()
				{
					MessageReceiver.this.createReceiveLink();
				}
			});
		}
		catch (IOException ioException)
		{
			this.linkOpen.getWork().completeExceptionally(new ServiceBusException(false, "Failed to create Receiver, see cause for more details.", ioException));
		}

		return this.linkOpen.getWork();
	}

	private List<Message> receiveCore(final int messageCount)
	{
		List<Message> returnMessages = null;
		Message currentMessage = this.pollPrefetchQueue();
	
		while (currentMessage != null) 
		{
			if (returnMessages == null)
			{
				returnMessages = new LinkedList<Message>();
			}

			returnMessages.add(currentMessage);
			if (returnMessages.size() >= messageCount)
			{
				break;
			}

			currentMessage = this.pollPrefetchQueue();
		}
		
		return returnMessages;
	}

	public int getPrefetchCount()
	{
		synchronized (this.prefetchCountSync)
		{
			return this.prefetchCount;
		}
	}

	public void setPrefetchCount(final int value) throws ServiceBusException
	{
		final int deltaPrefetchCount;
		synchronized (this.prefetchCountSync)
		{
			deltaPrefetchCount = this.prefetchCount - value;
			this.prefetchCount = value;
		}
		
		try
		{
			this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler()
			{
				@Override
				public void onEvent()
				{
					sendFlow(deltaPrefetchCount);
				}
			});
		}
		catch (IOException ioException)
		{
			throw new ServiceBusException(false, "Setting prefetch count failed, see cause for more details", ioException);
		}
	}

	public Duration getReceiveTimeout()
	{
		return this.receiveTimeout;
	}

	public void setReceiveTimeout(final Duration value)
	{
		this.receiveTimeout = value;
	}

	public CompletableFuture<Collection<Message>> receive(final int maxMessageCount)
	{
		this.throwIfClosed(this.lastKnownLinkError);

		if (maxMessageCount <= 0 || maxMessageCount > this.prefetchCount)
		{
			throw new IllegalArgumentException(String.format(Locale.US, "parameter 'maxMessageCount' should be a positive number and should be less than prefetchCount(%s)", this.prefetchCount));
		}

		if (this.pendingReceives.isEmpty())
		{
			this.scheduleOperationTimer(TimeoutTracker.create(this.receiveTimeout));
		}

		CompletableFuture<Collection<Message>> onReceive = new CompletableFuture<Collection<Message>>();
		
		try
		{
			this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler()
			{
				@Override
				public void onEvent()
				{
					final List<Message> messages = receiveCore(maxMessageCount);
					if (messages != null)
						onReceive.complete(messages);
					else
						pendingReceives.offer(new ReceiveWorkItem(onReceive, receiveTimeout, maxMessageCount));

					// calls to reactor should precede enqueue of the workItem into PendingReceives.
					// This will allow error handling to enact on the enqueued workItem.
					if (receiveLink.getLocalState() == EndpointState.CLOSED || receiveLink.getRemoteState() == EndpointState.CLOSED)
					{
                                                if (!creatingLink)
                                                    createReceiveLink();
					}
				}
			});
		}
		catch (IOException ioException)
		{
			onReceive.completeExceptionally(
					new ServiceBusException(false, "Receive failed while dispatching to Reactor, see cause for more details.", ioException));
		}

		return onReceive;
	}

	public void onOpenComplete(Exception exception)
	{		
		if (exception == null)
		{
			if (this.linkOpen != null && !this.linkOpen.getWork().isDone())
			{
				this.linkOpen.getWork().complete(this);
			}

			this.lastKnownLinkError = null;

			this.underlyingFactory.getRetryPolicy().resetRetryCount(this.underlyingFactory.getClientId());

			this.nextCreditToFlow = 0;
			this.sendFlow(this.prefetchCount - this.prefetchedMessages.size());

			if(TRACE_LOGGER.isLoggable(Level.FINE))
			{
				TRACE_LOGGER.log(Level.FINE, String.format("receiverPath[%s], linkname[%s], updated-link-credit[%s], sentCredits[%s]",
						this.receivePath, this.receiveLink.getName(), this.receiveLink.getCredit(), this.prefetchCount));
			}
		}
		else
		{
			if (this.linkOpen != null && !this.linkOpen.getWork().isDone())
			{
				this.setClosed();
				ExceptionUtil.completeExceptionally(this.linkOpen.getWork(), exception, this);
			}

			this.lastKnownLinkError = exception;
		}
	}

	@Override
	public void onReceiveComplete(Delivery delivery)
	{
		Message message = null;
		
		int msgSize = delivery.pending();
		byte[] buffer = new byte[msgSize];
		
		int read = receiveLink.recv(buffer, 0, msgSize);
		
		message = Proton.message();
		message.decode(buffer, 0, read);
                
		delivery.settle();

		this.prefetchedMessages.add(message);
		this.underlyingFactory.getRetryPolicy().resetRetryCount(this.getClientId());
		
		final ReceiveWorkItem currentReceive = this.pendingReceives.poll();
		if (currentReceive != null && !currentReceive.getWork().isDone())
		{
			List<Message> messages = this.receiveCore(currentReceive.maxMessageCount);

			CompletableFuture<Collection<Message>> future = currentReceive.getWork();
			future.complete(messages);
		}
	}

	public void onError(final ErrorCondition error)
	{		
		final Exception completionException = ExceptionUtil.toException(error);
		this.onError(completionException);
	}

	@Override
	public void onError(final Exception exception)
	{
                this.creatingLink = false;
		this.prefetchedMessages.clear();

		if (this.getIsClosingOrClosed())
		{
			this.linkClose.complete(null);
			
			WorkItem<Collection<Message>> workItem = null;
			final boolean isTransientException = exception == null ||
					(exception instanceof ServiceBusException && ((ServiceBusException) exception).getIsTransient());
			while ((workItem = this.pendingReceives.poll()) != null)
			{
				CompletableFuture<Collection<Message>> future = workItem.getWork();
				if (isTransientException)
				{
					future.complete(null);
				}
				else
				{
					ExceptionUtil.completeExceptionally(future, exception, this);
				}
			}
		}
		else
		{
			this.lastKnownLinkError = exception;
			this.onOpenComplete(exception);
			
			final WorkItem<Collection<Message>> workItem = this.pendingReceives.peek();
			final Duration nextRetryInterval = workItem != null && workItem.getTimeoutTracker() != null
					? this.underlyingFactory.getRetryPolicy().getNextRetryInterval(this.getClientId(), exception, workItem.getTimeoutTracker().remaining())
					: null;
			
                        boolean recreateScheduled = true;

                        if (nextRetryInterval != null)
			{
                                try
				{
					this.underlyingFactory.scheduleOnReactorThread((int) nextRetryInterval.toMillis(), new DispatchHandler()
					{
						@Override
						public void onEvent()
						{
							if (receiveLink.getLocalState() == EndpointState.CLOSED || receiveLink.getRemoteState() == EndpointState.CLOSED)
							{
								createReceiveLink();
								underlyingFactory.getRetryPolicy().incrementRetryCount(getClientId());
							}
						}
					});
				}
				catch (IOException ignore)
				{
                                    recreateScheduled = false;
				}
                        }			
                                
			if (nextRetryInterval == null || !recreateScheduled)
			{
				WorkItem<Collection<Message>> pendingReceive = null;
				while ((pendingReceive = this.pendingReceives.poll()) != null)
				{
					ExceptionUtil.completeExceptionally(pendingReceive.getWork(), exception, this);
				}
			}
		}
	}

	private void scheduleOperationTimer(final TimeoutTracker tracker)
	{
		if (tracker != null)
		{
			Timer.schedule(this.onOperationTimedout, tracker.remaining(), TimerType.OneTimeRun);
		}
	}

	private void createReceiveLink()
	{	
            this.creatingLink = true;
            
            final Consumer<Session> onSessionOpen = new Consumer<Session>()
            {
                @Override
                public void accept(Session session)
                {
                    final Source source = new Source();
                    source.setAddress(receivePath);

                    final Map<Symbol, UnknownDescribedType> filterMap = MessageReceiver.this.settingsProvider.getFilter(MessageReceiver.this.lastReceivedMessage);
                    if (filterMap != null)
                        source.setFilter(filterMap);


                    final String receiveLinkNamePrefix = StringUtil.getRandomString();
                    final String receiveLinkName = session.getConnection() != null && !StringUtil.isNullOrEmpty(session.getConnection().getRemoteContainer()) ? 
                                    receiveLinkNamePrefix.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(session.getConnection().getRemoteContainer()) :
                                    receiveLinkNamePrefix;
                    final Receiver receiver = session.receiver(receiveLinkName);
                    receiver.setSource(source);
                    
                    final Target target = new Target();
                    if (targetPath != null)
                        target.setAddress(targetPath);
                    
                    receiver.setTarget(target);

                    // use explicit settlement via dispositions (not pre-settled)
                    receiver.setSenderSettleMode(serviceSettleMode);
                    receiver.setReceiverSettleMode(ReceiverSettleMode.SECOND);

                    final Map<Symbol, Object> linkProperties = MessageReceiver.this.settingsProvider.getProperties();
                    if (linkProperties != null)
                        receiver.setProperties(linkProperties);

                    final ReceiveLinkHandler handler = new ReceiveLinkHandler(MessageReceiver.this);
                    BaseHandler.setHandler(receiver, handler);
                    MessageReceiver.this.underlyingFactory.registerForConnectionError(receiver);

                    receiver.open();

                    if (MessageReceiver.this.receiveLink != null)
                    {
                            final Receiver oldReceiver = MessageReceiver.this.receiveLink;
                            MessageReceiver.this.underlyingFactory.deregisterForConnectionError(oldReceiver);
                    }

                    MessageReceiver.this.receiveLink = receiver;
                    MessageReceiver.this.creatingLink = false;
                }
            };
            
            final Consumer<ErrorCondition> onSessionOpenFailed = new Consumer<ErrorCondition>()
            {
                @Override
                public void accept(ErrorCondition t)
                {
                    onError(t);
                }
            };
		
            this.underlyingFactory.getSession(this.receivePath,
                    this.sessionId,
                    onSessionOpen,
                    onSessionOpenFailed);
        }

	// CONTRACT: message should be delivered to the caller of MessageReceiver.receive() only via Poll on prefetchqueue
	private Message pollPrefetchQueue()
	{
		final Message message = this.prefetchedMessages.poll();
		if (message != null)
		{
			// message lastReceivedOffset should be up-to-date upon each poll - as recreateLink will depend on this 
			this.lastReceivedMessage = message;
			this.sendFlow(1);
		}

		return message;
	}

	private void sendFlow(final int credits)
	{
		// slow down sending the flow - to make the protocol less-chat'y
		this.nextCreditToFlow += credits;
		if (this.nextCreditToFlow >= this.prefetchCount || this.nextCreditToFlow >= 100)
		{
			final int tempFlow = this.nextCreditToFlow;
			this.receiveLink.flow(tempFlow);
			this.nextCreditToFlow = 0;
			
			if(TRACE_LOGGER.isLoggable(Level.FINE))
			{
				TRACE_LOGGER.log(Level.FINE, String.format("receiverPath[%s], linkname[%s], updated-link-credit[%s], sentCredits[%s]",
						this.receivePath, this.receiveLink.getName(), this.receiveLink.getCredit(), tempFlow));
			}
		}
	}

	private void scheduleLinkOpenTimeout(final TimeoutTracker timeout)
	{
		// timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
		Timer.schedule(
				new Runnable()
				{
					public void run()
					{
						if (!linkOpen.getWork().isDone())
						{
							Exception operationTimedout = new TimeoutException(
									String.format(Locale.US, "%s operation on ReceiveLink(%s) to path(%s) timed out at %s.", "Open", MessageReceiver.this.receiveLink.getName(), MessageReceiver.this.receivePath, ZonedDateTime.now()),
									MessageReceiver.this.lastKnownLinkError);
							if (TRACE_LOGGER.isLoggable(Level.WARNING))
							{
								TRACE_LOGGER.log(Level.WARNING, 
										String.format(Locale.US, "receiverPath[%s], linkName[%s], %s call timedout", MessageReceiver.this.receivePath, MessageReceiver.this.receiveLink.getName(),  "Open"), 
										operationTimedout);
							}

							ExceptionUtil.completeExceptionally(linkOpen.getWork(), operationTimedout, MessageReceiver.this);
						}
					}
				}
				, timeout.remaining()
				, TimerType.OneTimeRun);
	}

	private void scheduleLinkCloseTimeout(final TimeoutTracker timeout)
	{
		// timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
		Timer.schedule(
				new Runnable()
				{
					public void run()
					{
						if (!linkClose.isDone())
						{
							Exception operationTimedout = new TimeoutException(String.format(Locale.US, "%s operation on Receive Link(%s) timed out at %s", "Close", MessageReceiver.this.receiveLink.getName(), ZonedDateTime.now()));
							if (TRACE_LOGGER.isLoggable(Level.WARNING))
							{
								TRACE_LOGGER.log(Level.WARNING, 
										String.format(Locale.US, "receiverPath[%s], linkName[%s], %s call timedout", MessageReceiver.this.receivePath, MessageReceiver.this.receiveLink.getName(), "Close"), 
										operationTimedout);
							}

							ExceptionUtil.completeExceptionally(linkClose, operationTimedout, MessageReceiver.this);
						}
					}
				}
				, timeout.remaining()
				, TimerType.OneTimeRun);
	}

	@Override
	public void onClose(ErrorCondition condition)
	{
		if (condition == null)
		{
			this.onError(new ServiceBusException(true, 
					String.format(Locale.US, "Closing the link. LinkName(%s), EntityPath(%s)", this.receiveLink.getName(), this.receivePath)));
		}
		else
		{
			this.onError(condition);
		}
	}

	@Override
	public ErrorContext getContext()
	{
		final boolean isLinkOpened = this.linkOpen != null && this.linkOpen.getWork().isDone();
		final String referenceId = this.receiveLink != null && this.receiveLink.getRemoteProperties() != null && this.receiveLink.getRemoteProperties().containsKey(ClientConstants.TRACKING_ID_PROPERTY)
				? this.receiveLink.getRemoteProperties().get(ClientConstants.TRACKING_ID_PROPERTY).toString()
						: ((this.receiveLink != null) ? this.receiveLink.getName(): null);

		ReceiverContext errorContext = new ReceiverContext(this.underlyingFactory != null ? this.underlyingFactory.getHostName() : null,
				this.receivePath,
				referenceId,
				isLinkOpened ? this.prefetchCount : null, 
                                isLinkOpened && this.receiveLink != null ? this.receiveLink.getCredit(): null, 
				isLinkOpened && this.prefetchedMessages != null ? this.prefetchedMessages.size(): null);

		return errorContext;
	}	

	private static class ReceiveWorkItem extends WorkItem<Collection<Message>>
	{
		private final int maxMessageCount;

		public ReceiveWorkItem(CompletableFuture<Collection<Message>> completableFuture, Duration timeout, final int maxMessageCount)
		{
			super(completableFuture, timeout);
			this.maxMessageCount = maxMessageCount;
		}
	}

	@Override
	protected CompletableFuture<Void> onClose()
	{
            if (!this.getIsClosed())
            {
                try
                {
                    this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler()
                        {
                            @Override
                            public void onEvent()
                            {                                    
                                if (receiveLink != null && receiveLink.getLocalState() != EndpointState.CLOSED)
                                {
                                    receiveLink.close();
                                    scheduleLinkCloseTimeout(TimeoutTracker.create(operationTimeout));
                                }
                                else if (receiveLink == null || receiveLink.getRemoteState() == EndpointState.CLOSED)
                                {
                                    linkClose.complete(null);
                                }
                            }
                        });
                }
                catch(IOException ioException)
                {
                    this.linkClose.completeExceptionally(new ServiceBusException(false, "Scheduling close failed with error. See cause for more details.", ioException));
                }
            }

            return this.linkClose;
	}
}
