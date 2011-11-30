/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
