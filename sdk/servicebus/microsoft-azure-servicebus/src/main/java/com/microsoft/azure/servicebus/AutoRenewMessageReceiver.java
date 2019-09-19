package com.microsoft.azure.servicebus;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.microsoft.azure.servicebus.primitives.MessageLockAutoRenewTask;
import com.microsoft.azure.servicebus.primitives.MessagingEntityType;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;

public class AutoRenewMessageReceiver extends MessageReceiver {

    public static final ScheduledThreadPoolExecutor DEFAULT_SCHEDULER = new ScheduledThreadPoolExecutor(10); 
    static 
    {
        DEFAULT_SCHEDULER.setRemoveOnCancelPolicy(true);
    }

    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<UUID, ScheduledFuture<?>> renewLockTaskMessageIdMap = new ConcurrentHashMap<>();

    AutoRenewMessageReceiver(MessagingFactory messagingFactory, String entityPath, MessagingEntityType entityType,
        ReceiveMode receiveMode) {
        this(messagingFactory, entityPath, entityType, receiveMode, DEFAULT_SCHEDULER);
    }

    AutoRenewMessageReceiver(MessagingFactory messagingFactory, String entityPath, MessagingEntityType entityType,
            ReceiveMode receiveMode, ScheduledExecutorService scheduledExecutorService) {
        super(messagingFactory, entityPath, entityType, receiveMode);
        scheduler = scheduledExecutorService;
    }

    AutoRenewMessageReceiver(URI namespaceEndpointURI, String entityPath, MessagingEntityType entityType,
            ClientSettings clientSettings, ReceiveMode receiveMode) {
        this(namespaceEndpointURI, entityPath, entityType, clientSettings, receiveMode, DEFAULT_SCHEDULER);
    }

    AutoRenewMessageReceiver(URI namespaceEndpointURI, String entityPath, MessagingEntityType entityType,
            ClientSettings clientSettings, ReceiveMode receiveMode, ScheduledExecutorService scheduledExecutorService) {
        super(namespaceEndpointURI, entityPath, entityType, clientSettings, receiveMode);
        scheduler = scheduledExecutorService;
    }

    @Override
    public CompletableFuture<IMessage> receiveAsync(Duration serverWaitTime) {
        return super.receiveAsync(serverWaitTime).thenApplyAsync(c -> scheduleLockRenewal(c));
    }

    @Override
    public CompletableFuture<Collection<IMessage>> receiveBatchAsync(int maxMessageCount, Duration serverWaitTime) {
        return super.receiveBatchAsync(maxMessageCount, serverWaitTime).thenApplyAsync(c -> scheduleLockRenewal(c));
    }

    @Override
    public CompletableFuture<IMessage> receiveDeferredMessageAsync(long sequenceNumber) {
        return super.receiveDeferredMessageAsync(sequenceNumber).thenApplyAsync(c -> scheduleLockRenewal(c));
    }

    @Override
    public CompletableFuture<Collection<IMessage>> receiveDeferredMessageBatchAsync(Collection<Long> sequenceNumbers) {
        return super.receiveDeferredMessageBatchAsync(sequenceNumbers).thenApplyAsync(c -> scheduleLockRenewal(c));
    }

    private IMessage scheduleLockRenewal(IMessage message) {
        if (message != null) {
            MessageLockAutoRenewTask task = new MessageLockAutoRenewTask(this, message);
            Long schedule = ChronoUnit.SECONDS.between(Instant.now(), message.getLockedUntilUtc()) - 20; // give us a 20 sec headroom
            ScheduledFuture<?> renewFuture = scheduler.scheduleAtFixedRate(task, schedule, schedule, TimeUnit.SECONDS);
            renewLockTaskMessageIdMap.put(message.getLockToken(), renewFuture);
        }

        return message;
    }

    private Collection<IMessage> scheduleLockRenewal(Collection<IMessage> messages) {
        if (messages != null && !messages.isEmpty()) {
            for (IMessage message : messages) {
                scheduleLockRenewal(message);
            }
        }

        return messages;
    }

    @Override
    protected CompletableFuture<Void> onClose() {
        CompletableFuture<Void> result = super.onClose();
        scheduler.shutdown();
        return result;
    }

    @Override
    protected void disposeLockToken(UUID lockToken, TransactionContext transaction) {
        super.disposeLockToken(lockToken, transaction);

        ScheduledFuture<?> task = renewLockTaskMessageIdMap.get(lockToken);

        if (task != null) {
            task.cancel(false);
            renewLockTaskMessageIdMap.remove(lockToken);
        }
    }
}