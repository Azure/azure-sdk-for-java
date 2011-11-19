/**
 * 
 */
package com.microsoft.windowsazure.services.queue.client;

import com.microsoft.windowsazure.services.core.storage.RequestOptions;

/**
 * Represents a set of options that may be specified on a request.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public final class QueueRequestOptions extends RequestOptions {
    /**
     * Initializes a new instance of the QueueRequestOptions class.
     */
    public QueueRequestOptions() {
        // no op
    }

    /**
     * Initializes a new instance of the QueueRequestOptions class.
     */
    public QueueRequestOptions(final QueueRequestOptions other) {
        super(other);
    }

    /**
     * Populates the default timeout and retry policy from client if they are null.
     * 
     * @param client
     *            the service client to populate from
     */
    protected void applyDefaults(final CloudQueueClient client) {
        super.applyBaseDefaults(client);
    }
}
