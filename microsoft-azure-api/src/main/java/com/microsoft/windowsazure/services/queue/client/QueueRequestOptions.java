package com.microsoft.windowsazure.services.queue.client;

import com.microsoft.windowsazure.services.core.storage.RequestOptions;

/**
 * Represents a set of options that may be specified on a queue request.
 */
public final class QueueRequestOptions extends RequestOptions {
    /**
     * Initializes a new instance of the QueueRequestOptions class.
     */
    public QueueRequestOptions() {
        // no op
    }

    /**
     * Initializes a new instance of the QueueRequestOptions class as a copy of
     * another QueueRequestOptions instance.
     * 
     * @param other
     *            The {@link QueueRequestOptions} object to copy the values
     *            from.
     */
    public QueueRequestOptions(final QueueRequestOptions other) {
        super(other);
    }

    /**
     * Populates the default timeout and retry policy from client if they are
     * not set.
     * 
     * @param client
     *            The {@link CloudQueueClient} service client to populate the
     *            default values from.
     */
    protected void applyDefaults(final CloudQueueClient client) {
        super.applyBaseDefaults(client);
    }
}
