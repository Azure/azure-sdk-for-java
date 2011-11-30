/**
 * 
 */
package com.microsoft.windowsazure.services.core.storage;

/**
 * Represents a retry policy factory that creates a new {@link RetryPolicy} object per transaction.
 */
public interface RetryPolicyFactory {

    /**
     * Creates a new {@link RetryPolicy} object for the current request attempt.
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link RetryPolicy} object that represents the new retry policy for the current request attempt.
     */
    RetryPolicy createInstance(OperationContext opContext);
}
