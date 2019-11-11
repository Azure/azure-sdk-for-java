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
 * A wrapper class for the result returned from a Queue Service REST API
 * operation to get queue metadata. This is returned by calls to implementations
 * of {@link com.microsoft.windowsazure.services.queue.QueueContract#getQueueMetadata(String)} and
 * {@link com.microsoft.windowsazure.services.queue.QueueContract#getQueueMetadata(String, QueueServiceOptions)}.
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179384.aspx">Get
 * Queue Metadata</a> documentation on MSDN for details of the underlying Queue
 * Service REST API operation.
 */
public class GetQueueMetadataResult {
    private long approximateMessageCount;
    private HashMap<String, String> metadata;

    /**
     * Gets the queue's approximate message count, as reported by the server.
     * 
     * @return The queue's approximate message count.
     */
    public long getApproximateMessageCount() {
        return approximateMessageCount;
    }

    /**
     * Reserved for internal use. This method is invoked by the API as part of
     * the response generation from the Queue Service REST API operation to set
     * the value for the approximate message count returned by the server.
     * 
     * @param approximateMessageCount
     *            The queue's approximate message count to set.
     */
    public void setApproximateMessageCount(long approximateMessageCount) {
        this.approximateMessageCount = approximateMessageCount;
    }

    /**
     * Gets the metadata collection of key-value {@link String} pairs currently
     * set on a queue. Queue metadata is a user-defined collection of key-value
     * pairs that is opaque to the server.
     * 
     * @return A {@link java.util.HashMap} of key-value {@link String} pairs
     *         containing the metadata set on the queue.
     */
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Reserved for internal use. This method is invoked by the API as part of
     * the response generation from the Queue Service REST API operation to set
     * the value from the queue metadata returned by the server.
     * 
     * @param metadata
     *            A {@link java.util.HashMap} of key-value {@link String} pairs
     *            containing the metadata set on the queue.
     */
    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }
}
