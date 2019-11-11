/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.queue.models;

import java.util.HashMap;

/**
 * Represents the options that may be set on a queue when created in the storage
 * service with a {@link com.microsoft.windowsazure.services.queue.QueueContract#createQueue(String, CreateQueueOptions)
 * createQueue} request. These options include a server response timeout for the
 * request and the metadata to associate with the created queue.
 */
public class CreateQueueOptions extends QueueServiceOptions {
    private HashMap<String, String> metadata = new HashMap<String, String>();

    /**
     * Sets the server request timeout value associated with this
     * {@link CreateQueueOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link CreateQueueOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link CreateQueueOptions} instance.
     */
    @Override
    public CreateQueueOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the metadata collection of key-value {@link String} pairs to set on
     * a queue when the queue is created.
     * 
     * @return A {@link java.util.HashMap} of key-value {@link String} pairs
     *         containing the metadata to set on the queue.
     */
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata collection of key-value {@link String} pairs to set on
     * a queue when the queue is created. Queue metadata is a user-defined
     * collection of key-value pairs that is opaque to the server.
     * <p>
     * The <em>metadata</em> value is only added to a newly created queue where
     * this {@link CreateQueueOptions} instance is passed as a parameter.
     * 
     * @param metadata
     *            The {@link java.util.HashMap} of key-value {@link String}
     *            pairs containing the metadata to set on the queue.
     * @return A reference to this {@link CreateQueueOptions} instance.
     */
    public CreateQueueOptions setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Adds a key-value pair of {@link String} to the metadata collection to set
     * on a queue when the queue is created. Queue metadata is a user-defined
     * collection of key-value pairs that is opaque to the server. If the key
     * already exists in the metadata collection, the value parameter will
     * overwrite the existing value paired with that key without notification.
     * <p>
     * The updated metadata is only added to a newly created queue where this
     * {@link CreateQueueOptions} instance is passed as a parameter.
     * 
     * @param key
     *            A {@link String} containing the key part of the key-value pair
     *            to add to the metadata.
     * @param value
     *            A {@link String} containing the value part of the key-value
     *            pair to add to the metadata.
     * @return A reference to this {@link CreateQueueOptions} instance.
     */
    public CreateQueueOptions addMetadata(String key, String value) {
        this.metadata.put(key, value);
        return this;
    }
}
