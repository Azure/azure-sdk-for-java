/**
 * 
 */
package com.microsoft.windowsazure.services.core.storage;

/**
 * Represents the options to use while processing a given request.
 */
public class RequestOptions {

    /**
     * The instance of the {@link RetryPolicyFactory} interface to use for the request.
     */
    private RetryPolicyFactory retryPolicyFactory;

    /**
     * The timeout interval, in milliseconds, to use for the request.
     */
    private Integer timeoutIntervalInMs;

    /**
     * Creates an instance of the <code>RequestOptions</code> class.
     */
    public RequestOptions() {
        // Empty Default Ctor
    }

    /**
     * Creates an instance of the <code>RequestOptions</code> class by copying values from another
     * <code>RequestOptions</code> instance.
     * 
     * @param other
     *            A <code>RequestOptions</code> object that represents the request options to copy.
     */
    public RequestOptions(final RequestOptions other) {
        this.setTimeoutIntervalInMs(other.getTimeoutIntervalInMs());
        this.setRetryPolicyFactory(other.getRetryPolicyFactory());
    }

    /**
     * Populates the default timeout and retry policy from client if they are null.
     * 
     * @param client
     *            the service client to populate from
     */
    protected final void applyBaseDefaults(final ServiceClient client) {
        if (this.getRetryPolicyFactory() == null) {
            this.setRetryPolicyFactory(client.getRetryPolicyFactory());
        }

        if (this.getTimeoutIntervalInMs() == null) {
            this.setTimeoutIntervalInMs(client.getTimeoutInMs());
        }
    }

    /**
     * @return the retryPolicyFactory
     */
    public final RetryPolicyFactory getRetryPolicyFactory() {
        return this.retryPolicyFactory;
    }

    /**
     * @return the timeoutIntervalInMs
     */
    public final Integer getTimeoutIntervalInMs() {
        return this.timeoutIntervalInMs;
    }

    /**
     * @param retryPolicyFactory
     *            the retryPolicyFactory to set
     */
    public final void setRetryPolicyFactory(final RetryPolicyFactory retryPolicyFactory) {
        this.retryPolicyFactory = retryPolicyFactory;
    }

    /**
     * @param timeoutIntervalInMs
     *            the timeoutIntervalInMs to set
     */
    public final void setTimeoutIntervalInMs(final Integer timeoutIntervalInMs) {
        this.timeoutIntervalInMs = timeoutIntervalInMs;
    }
}
