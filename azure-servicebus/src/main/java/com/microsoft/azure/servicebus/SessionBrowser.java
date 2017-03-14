package com.microsoft.azure.servicebus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.Util;

final class SessionBrowser
{
	private static final int PAGESIZE = 100;
	// .net DateTime.MaxValue need to be passed
	private static final Date MAXDATE = new Date(253402300800000l);
	
	private final BrokeredMessageReceiver messageReceiver;
	private final MessagingFactory messagingFactory;
	private final String entityPath;
	private String lastSessionId = null;
	private int lastReceivedSkip = 0;
	
	SessionBrowser(MessagingFactory messagingFactory, BrokeredMessageReceiver messageReceiver, String entityPath)
	{
		this.messagingFactory = messagingFactory;
		this.messageReceiver = messageReceiver;
		this.entityPath = entityPath;
	}
	
	public CompletableFuture<Collection<? extends IMessageSession>> getMessageSessionsAsync()
	{
		return this.getMessageSessionsAsync(MAXDATE);
	}
	
	public CompletableFuture<Collection<? extends IMessageSession>> getMessageSessionsAsync(Date lastUpdatedTime)
	{
		return this.messageReceiver.getInternalReceiver().getMessageSessionsAsync(lastUpdatedTime, this.lastReceivedSkip, PAGESIZE, this.lastSessionId).thenApply((p) ->
		{
			ArrayList<BrowsableMessageSession> sessionsList = new ArrayList<>();
			this.lastReceivedSkip = p.getSecondItem();
			String[] sessionIds = p.getFirstItem();
			if(sessionIds != null && sessionIds.length > 0)
			{
				this.lastSessionId = sessionIds[sessionIds.length - 1];
				for(String sessionId : sessionIds)
				{
					sessionsList.add(new BrowsableMessageSession(sessionId, this.messagingFactory, this.messageReceiver.getInternalReceiver(), this.entityPath));
				}
			}
			return sessionsList;
		});
	}
}
