// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package com.microsoft.azure.servicebus;

import java.net.URI;
import java.sql.Date;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.MessagingEntityType;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.MiscRequestResponseOperationHandler;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.StringUtil;
import com.microsoft.azure.servicebus.primitives.Util;
import com.microsoft.azure.servicebus.rules.Filter;
import com.microsoft.azure.servicebus.rules.RuleDescription;

public final class SubscriptionClient extends InitializableEntity implements ISubscriptionClient
{
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SubscriptionClient.class);
    private static final String SUBSCRIPTIONS_DELIMITER = "/subscriptions/";
	private final ReceiveMode receiveMode;
	private final String subscriptionPath;
	private MessagingFactory factory;
	private MessageAndSessionPump messageAndSessionPump;
	private SessionBrowser sessionBrowser;
	private MiscRequestResponseOperationHandler miscRequestResponseHandler;

	public static final String DEFAULT_RULE_NAME = "$Default";

	private SubscriptionClient(ReceiveMode receiveMode, String subscriptionPath)
	{
		super(StringUtil.getShortRandomString());		
		this.receiveMode = receiveMode;
		this.subscriptionPath = subscriptionPath;
	}
	
	public SubscriptionClient(ConnectionStringBuilder amqpConnectionStringBuilder, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
	{
		this(receiveMode, amqpConnectionStringBuilder.getEntityPath());
		CompletableFuture<MessagingFactory> factoryFuture = MessagingFactory.createFromConnectionStringBuilderAsync(amqpConnectionStringBuilder);
		Utils.completeFuture(factoryFuture.thenComposeAsync((f) -> this.createPumpAndBrowserAsync(f)));
		if(TRACE_LOGGER.isInfoEnabled())
        {
            TRACE_LOGGER.info("Created subscription client to connection string '{}'", amqpConnectionStringBuilder.toLoggableString());
        }
	}
	
	public SubscriptionClient(String namespace, String subscriptionPath, ClientSettings clientSettings, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
    {
        this(Util.convertNamespaceToEndPointURI(namespace), subscriptionPath, clientSettings, receiveMode);
    }
    
    public SubscriptionClient(URI namespaceEndpointURI, String subscriptionPath, ClientSettings clientSettings, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
    {
        this(receiveMode, subscriptionPath);
        CompletableFuture<MessagingFactory> factoryFuture = MessagingFactory.createFromNamespaceEndpointURIAsyc(namespaceEndpointURI, clientSettings);
        Utils.completeFuture(factoryFuture.thenComposeAsync((f) -> this.createPumpAndBrowserAsync(f)));
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("Created subscription client to subscription '{}/{}'", namespaceEndpointURI.toString(), subscriptionPath);
        }
    }
	
	SubscriptionClient(MessagingFactory factory, String subscriptionPath, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
	{
		this(receiveMode, subscriptionPath);
		Utils.completeFuture(this.createPumpAndBrowserAsync(factory));
		TRACE_LOGGER.info("Created subscription client to subscripton '{}'", subscriptionPath);
	}
	
	private CompletableFuture<Void> createPumpAndBrowserAsync(MessagingFactory factory)
	{
	    this.factory = factory;
		CompletableFuture<Void> postSessionBrowserFuture = MiscRequestResponseOperationHandler.create(factory, this.subscriptionPath, MessagingEntityType.SUBSCRIPTION).thenAcceptAsync((msoh) -> {
			this.miscRequestResponseHandler = msoh;
			this.sessionBrowser = new SessionBrowser(factory, this.subscriptionPath, MessagingEntityType.SUBSCRIPTION, msoh);
		});		
		
		this.messageAndSessionPump = new MessageAndSessionPump(factory, this.subscriptionPath, MessagingEntityType.SUBSCRIPTION, receiveMode);
		CompletableFuture<Void> messagePumpInitFuture = this.messageAndSessionPump.initializeAsync();
		
		return CompletableFuture.allOf(postSessionBrowserFuture, messagePumpInitFuture);
	}
	
	@Override
	public ReceiveMode getReceiveMode() {
		return this.receiveMode;
	}
	
	@Override
	public String getEntityPath() {
		return this.subscriptionPath;				
	}

	@Override
	public void addRule(RuleDescription ruleDescription) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.addRuleAsync(ruleDescription));
	}

	@Override
	public CompletableFuture<Void> addRuleAsync(RuleDescription ruleDescription) {
		return this.miscRequestResponseHandler.addRuleAsync(ruleDescription);
	}

	@Override
	public void addRule(String ruleName, Filter filter) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.addRuleAsync(ruleName, filter));
	}

	@Override
	public CompletableFuture<Void> addRuleAsync(String ruleName, Filter filter) {
		return this.addRuleAsync(new RuleDescription(ruleName, filter));
	}
	
	@Override
	public void removeRule(String ruleName) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.removeRuleAsync(ruleName));
	}

	@Override
	public CompletableFuture<Void> removeRuleAsync(String ruleName) {
		return this.miscRequestResponseHandler.removeRuleAsync(ruleName);
	}

	@Override
	public Collection<RuleDescription> getRules() throws ServiceBusException, InterruptedException {
		return Utils.completeFuture(this.getRulesAsync());
	}

	@Override
	public CompletableFuture<Collection<RuleDescription>> getRulesAsync()
	{
		// Skip and Top can be used to implement pagination.
		// In this case, we are trying to fetch all the rules associated with the subscription.
		int skip = 0, top = Integer.MAX_VALUE;
		return this.miscRequestResponseHandler.getRulesAsync(skip, top);
	}

	@Override
	public void registerMessageHandler(IMessageHandler handler) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.registerMessageHandler(handler);		
	}

	@Override
	public void registerMessageHandler(IMessageHandler handler, MessageHandlerOptions handlerOptions) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.registerMessageHandler(handler, handlerOptions);		
	}

	@Override
	public void registerSessionHandler(ISessionHandler handler) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.registerSessionHandler(handler);		
	}

	@Override
	public void registerSessionHandler(ISessionHandler handler, SessionHandlerOptions handlerOptions) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.registerSessionHandler(handler, handlerOptions);		
	}

	// No op now
	@Override
	CompletableFuture<Void> initializeAsync() {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	protected CompletableFuture<Void> onClose() {
		return this.messageAndSessionPump.closeAsync().thenCompose((v) -> this.miscRequestResponseHandler.closeAsync().thenCompose((w) -> this.factory.closeAsync()));
	}
	
//	@Override
	Collection<IMessageSession> getMessageSessions() throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.getMessageSessionsAsync());
	}

//	@Override
	Collection<IMessageSession> getMessageSessions(Instant lastUpdatedTime) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.getMessageSessionsAsync(lastUpdatedTime));
	}

//	@Override
	CompletableFuture<Collection<IMessageSession>> getMessageSessionsAsync() {
		return this.sessionBrowser.getMessageSessionsAsync();
	}

//	@Override
	CompletableFuture<Collection<IMessageSession>> getMessageSessionsAsync(Instant lastUpdatedTime) {
		return this.sessionBrowser.getMessageSessionsAsync(Date.from(lastUpdatedTime));
	}
	
	@Override
	public void abandon(UUID lockToken) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.abandon(lockToken);
	}

	@Override
	public void abandon(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.abandon(lockToken, transaction);
	}

	@Override
	public void abandon(UUID lockToken, Map<String, Object> propertiesToModify)	throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.abandon(lockToken, propertiesToModify);		
	}

	@Override
	public void abandon(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction)	throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.abandon(lockToken, propertiesToModify, transaction);
	}

	@Override
	public CompletableFuture<Void> abandonAsync(UUID lockToken) {
		return this.messageAndSessionPump.abandonAsync(lockToken);
	}

	@Override
	public CompletableFuture<Void> abandonAsync(UUID lockToken, TransactionContext transaction) {
		return this.messageAndSessionPump.abandonAsync(lockToken, transaction);
	}

	@Override
	public CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
		return this.messageAndSessionPump.abandonAsync(lockToken, propertiesToModify);
	}

	@Override
	public CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) {
		return this.messageAndSessionPump.abandonAsync(lockToken, propertiesToModify, transaction);
	}

	@Override
	public void complete(UUID lockToken) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.complete(lockToken);		
	}

	@Override
	public void complete(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.complete(lockToken, transaction);
	}

	@Override
	public CompletableFuture<Void> completeAsync(UUID lockToken) {
		return this.messageAndSessionPump.completeAsync(lockToken);
	}

	@Override
	public CompletableFuture<Void> completeAsync(UUID lockToken, TransactionContext transaction) {
		return this.messageAndSessionPump.completeAsync(lockToken, transaction);
	}

//	@Override
	void defer(UUID lockToken) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.defer(lockToken);		
	}

//	@Override
	void defer(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.defer(lockToken, propertiesToModify);		
	}

//	@Override
//	public CompletableFuture<Void> deferAsync(UUID lockToken) {
//		return this.messageAndSessionPump.deferAsync(lockToken);
//	}
//
//	@Override
//	public CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
//		return this.messageAndSessionPump.deferAsync(lockToken, propertiesToModify);
//	}

	@Override
	public void deadLetter(UUID lockToken) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.deadLetter(lockToken);		
	}

	@Override
	public void deadLetter(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.deadLetter(lockToken, transaction);
	}

	@Override
	public void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.deadLetter(lockToken, propertiesToModify);		
	}

	@Override
	public void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.deadLetter(lockToken, propertiesToModify, transaction);
	}

	@Override
	public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.deadLetter(lockToken, deadLetterReason, deadLetterErrorDescription);		
	}

	@Override
	public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, TransactionContext transaction) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.deadLetter(lockToken, deadLetterReason, deadLetterErrorDescription, transaction);
	}

	@Override
	public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.deadLetter(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify);		
	}

	@Override
	public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify, TransactionContext transaction) throws InterruptedException, ServiceBusException {
		this.messageAndSessionPump.deadLetter(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify, transaction);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken) {
		return this.messageAndSessionPump.deadLetterAsync(lockToken);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, TransactionContext transaction) {
		return this.messageAndSessionPump.deadLetterAsync(lockToken, transaction);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
		return this.messageAndSessionPump.deadLetterAsync(lockToken, propertiesToModify);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) {
		return this.messageAndSessionPump.deadLetterAsync(lockToken, propertiesToModify, transaction);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) {
		return this.messageAndSessionPump.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, TransactionContext transaction) {
		return this.messageAndSessionPump.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, transaction);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason,	String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {
		return this.messageAndSessionPump.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason,	String deadLetterErrorDescription, Map<String, Object> propertiesToModify, TransactionContext transaction) {
		return this.messageAndSessionPump.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify, transaction);
	}
	
	@Override
    public int getPrefetchCount() {
        return this.messageAndSessionPump.getPrefetchCount();
    }

    @Override
    public void setPrefetchCount(int prefetchCount) throws ServiceBusException {
        this.messageAndSessionPump.setPrefetchCount(prefetchCount);
    }

    @Override
    public String getTopicName() {
       String entityPath = this.getEntityPath();
       String[] parts = Pattern.compile(SUBSCRIPTIONS_DELIMITER, Pattern.CASE_INSENSITIVE).split(entityPath, 2);
       return parts[0];
    }

    @Override
    public String getSubscriptionName() {
        String entityPath = this.getEntityPath();
        String[] parts = Pattern.compile(SUBSCRIPTIONS_DELIMITER, Pattern.CASE_INSENSITIVE).split(entityPath, 2);
        if(parts.length == 2)
        {
        	return parts[1];
        }
        else
        {
        	throw new RuntimeException("Invalid entity path in the subscription client.");
        }
    }
}
