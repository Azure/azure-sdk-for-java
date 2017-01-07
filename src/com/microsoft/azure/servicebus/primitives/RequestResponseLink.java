package com.microsoft.azure.servicebus.primitives;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.amqp.DispatchHandler;
import com.microsoft.azure.servicebus.amqp.IAmqpReceiver;
import com.microsoft.azure.servicebus.amqp.IAmqpSender;
import com.microsoft.azure.servicebus.amqp.ReceiveLinkHandler;
import com.microsoft.azure.servicebus.amqp.SendLinkHandler;
import com.microsoft.azure.servicebus.amqp.SessionHandler;

public class RequestResponseLink extends ClientEntity{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	
	private MessagingFactory underlyingFactory;
	private String linkPath;
	private InternalReceiver amqpReceiver;
	private InternalSender amqpSender;	
	private CompletableFuture<RequestResponseLink> createFuture;
	private String replyTo;
	
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
				}
			});
		}
		catch (IOException ioException)
		{
			requestReponseLink.createFuture.completeExceptionally(new ServiceBusException(false, "Failed to create internal links, see cause for more details.", ioException));
		}
		
		return requestReponseLink.createFuture;
	}
	
	private RequestResponseLink(MessagingFactory messagingFactory, String linkName, String linkPath)
	{
		super(linkName, null);
		
		this.underlyingFactory = messagingFactory;
		this.linkPath = linkPath;
		this.amqpSender = new InternalSender(linkName + ":internalSender", this);
		this.amqpReceiver = new InternalReceiver(linkName + ":interalReceiver", this);
		
		this.createFuture = new CompletableFuture<RequestResponseLink>();
	}
	
	private void createInternalLinks()
	{
		this.replyTo = UUID.randomUUID().toString();
		
		// Create send link
		final Connection connection = this.underlyingFactory.getConnection();

		final Session session = connection.session();
		session.setOutgoingWindow(Integer.MAX_VALUE);
		session.open();
		BaseHandler.setHandler(session, new SessionHandler(this.linkPath));

		String sendLinkNamePrefix = StringUtil.getRandomString();
		String sendLinkName = !StringUtil.isNullOrEmpty(connection.getRemoteContainer()) ?
				sendLinkNamePrefix.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(connection.getRemoteContainer()) :
				sendLinkNamePrefix;
		
		Sender sender = session.sender(sendLinkName);
		Target sednerTarget = new Target();
		sednerTarget.setAddress(this.linkPath);
		sender.setTarget(sednerTarget);
		Source senderSource = new Source();
		sender.setSource(senderSource);
		sender.setSenderSettleMode(SenderSettleMode.SETTLED);

		SendLinkHandler sendLinkHandler = new SendLinkHandler(this.amqpSender);
		BaseHandler.setHandler(sender, sendLinkHandler);

		this.amqpSender.setSendLink(sender);
		sender.open();
		
		// Create receive link
		String receiveLinkNamePrefix = StringUtil.getRandomString();
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

		final ReceiveLinkHandler receiveLinkHandler = new ReceiveLinkHandler(this.amqpReceiver);
		BaseHandler.setHandler(receiver, receiveLinkHandler);	
		this.amqpReceiver.setReceiveLink(receiver);
		receiver.open();
		
		this.amqpSender.openFuture.runAfterBoth(this.amqpReceiver.openFuture, () -> this.createFuture.complete(this));
	}
	
	
	public CompletableFuture<Message> requestAysnc(Message message, Duration timeout)
	{
		return CompletableFuture.completedFuture(null);
	}

	@Override
	protected CompletableFuture<Void> onClose() {
		return this.amqpSender.closeAsync().thenCompose((v) -> this.amqpReceiver.closeAsync());
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
			return closeFuture;
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
		}

		@Override
		public void onClose(ErrorCondition condition) {
			if(condition == null)
			{
				if(!this.closeFuture.isDone())
				{
					this.closeFuture.complete(null);
				}
			}
			else
			{
				Exception exception = ExceptionUtil.toException(condition);
				if(!this.openFuture.isDone())
				{
					this.openFuture.completeExceptionally(exception);
				}
				if(!this.closeFuture.isDone())
				{
					this.closeFuture.completeExceptionally(exception);
				}				
			}						
		}

		@Override
		public void onReceiveComplete(Delivery delivery) {
			// TODO Auto-generated method stub
			
		}

		public Receiver getReceiveLink() {
			return this.receiveLink;
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

		protected InternalSender(String clientId, RequestResponseLink parent) {
			super(clientId, parent);			
			this.parent = parent;
			this.openFuture = new CompletableFuture<Void>();
			this.closeFuture = new CompletableFuture<Void>();
		}

		@Override
		protected CompletableFuture<Void> onClose() {
			return closeFuture;
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
		}

		@Override
		public void onClose(ErrorCondition condition) {
			if(condition == null)
			{
				if(!this.closeFuture.isDone())
				{
					this.closeFuture.complete(null);
				}
			}
			else
			{
				Exception exception = ExceptionUtil.toException(condition);
				if(!this.openFuture.isDone())
				{
					this.openFuture.completeExceptionally(exception);
				}
				if(!this.closeFuture.isDone())
				{
					this.closeFuture.completeExceptionally(exception);
				}				
			}						
		}

		@Override
		public void onFlow(int creditIssued) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSendComplete(Delivery delivery) {
			// TODO Auto-generated method stub
			
		}
		
		public Sender getSendLink() {
			return this.sendLink;
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
	}
}
