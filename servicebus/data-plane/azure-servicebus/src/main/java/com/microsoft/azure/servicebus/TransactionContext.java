package com.microsoft.azure.servicebus;

import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an active servicebus transaction.
 * A transaction is initiated by calling {@link MessagingFactory#startTransactionAsync()}.
 * A transaction can result in commit or rollback.
 * To commit, call {@link MessagingFactory#endTransactionAsync(TransactionContext, boolean)} with <code>commit = true</code>
 * To rollback, call {@link MessagingFactory#endTransactionAsync(TransactionContext, boolean)} with <code>commit = false</code>
 */
public class TransactionContext {
    public static TransactionContext NULL_TXN = new TransactionContext(null, null);

    private ByteBuffer txnId;
    private ITransactionHandler txnHandler = null;
    private MessagingFactory messagingFactory;

    public TransactionContext(ByteBuffer txnId, MessagingFactory messagingFactory) {
        this.txnId = txnId;
        this.messagingFactory = messagingFactory;
    }

    /**
     * Represents the service-side transactionID
     * @return transaction ID
     */
    public ByteBuffer getTransactionId() { return this.txnId; }

    @Override
    public String toString() {
        return new String(txnId.array(), txnId.position(), txnId.limit());
    }

    /**
     * Commits the transaction
     * @throws ServiceBusException if Commit failed
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    public void commit() throws ServiceBusException, InterruptedException {
        if (this.messagingFactory == null) {
            throw new ServiceBusException(false, "MessagingFactory should not be null");
        }

        this.messagingFactory.endTransaction(this, true);
    }

    /**
     * Asynchronously commits the transaction
     * @return a CompletableFuture for the commit operation
     */
    public CompletableFuture<Void> commitAsync() {
        if (this.messagingFactory == null) {
            CompletableFuture<Void> exceptionCompletion = new CompletableFuture<>();
            exceptionCompletion.completeExceptionally(new ServiceBusException(false, "MessagingFactory should not be null"));
            return exceptionCompletion;
        }

        return this.messagingFactory.endTransactionAsync(this, true);
    }

    /**
     * Rollback the transaction
     * @throws ServiceBusException if rollback failed
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    public void rollback() throws ServiceBusException, InterruptedException {
        if (this.messagingFactory == null) {
            throw new ServiceBusException(false, "MessagingFactory should not be null");
        }

        this.messagingFactory.endTransaction(this, false);
    }

    /**
     * Asynchronously rollback the transaction.
     * @return a CompletableFuture for the rollback operation
     */
    public CompletableFuture<Void> rollbackAsync() {
        if (this.messagingFactory == null) {
            CompletableFuture<Void> exceptionCompletion = new CompletableFuture<>();
            exceptionCompletion.completeExceptionally(new ServiceBusException(false, "MessagingFactory should not be null"));
            return exceptionCompletion;
        }

        return this.messagingFactory.endTransactionAsync(this, false);
    }

    /**
     * This is not to be called by the user.
     * @param commit true indicates success and false for rollback
     */
    public void notifyTransactionCompletion(boolean commit) {
        if (txnHandler != null) {
            txnHandler.onTransactionCompleted(commit);
        }
    }

    void registerHandler(ITransactionHandler handler)
    {
        this.txnHandler = handler;
    }

    interface ITransactionHandler {
        public void onTransactionCompleted(boolean commit);
    }
}
