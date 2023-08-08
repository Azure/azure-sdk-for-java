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


/**
 * Represents the options that may be set on the Queue service for
 * {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions)} requests. These options
 * include a server response timeout for the request, a prefix to match queue
 * names to return, a marker to specify where to resume a list queues query, the
 * maximum number of queues to return in a single response, and whether to
 * include queue metadata with the response.
 */
public class ListQueuesOptions extends QueueServiceOptions {
    private String prefix;
    private String marker;
    private int maxResults;
    private boolean includeMetadata;

    /**
     * Sets the server request timeout value associated with this
     * {@link ListQueuesOptions} instance.
     * <p>
     * The timeout value only affects calls made on methods where this
     * {@link ListQueuesOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link ListQueuesOptions} instance.
     */
    @Override
    public ListQueuesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the prefix {@link String} used to match queue names to return in a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues} request.
     * 
     * @return The prefix {@link String} used to match queue names to return in
     *         a {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues}
     *         request.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix {@link String} to use to match queue names to return in a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues} request.
     * <p>
     * The prefix value only affects calls made on methods where this
     * {@link ListQueuesOptions} instance is passed as a parameter.
     * 
     * @param prefix
     *            The prefix {@link String} to use to match queue names to
     *            return in a
     *            {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues}
     *            request.
     * @return A reference to this {@link ListQueuesOptions} instance.
     */
    public ListQueuesOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Gets a {@link String} value that identifies the beginning of the list of
     * queues to be returned with a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues} request.
     * <p>
     * The {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues} method
     * returns a <strong>NextMarker</strong> element within the response if the
     * list returned was not complete, which can be accessed with the
     * {@link ListQueuesResult#getNextMarker()} method. This opaque value may
     * then be set on a {@link ListQueuesOptions} instance with a call to
     * {@link ListQueuesOptions#setMarker(String) setMarker} to be used in a
     * subsequent {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues}
     * call to request the next portion of the list of queues.
     * 
     * @return The marker value that identifies the beginning of the list of
     *         queues to be returned.
     */
    public String getMarker() {
        return marker;
    }

    /**
     * Sets a {@link String} marker value that identifies the beginning of the
     * list of queues to be returned with a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues} request.
     * <p>
     * The {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues} method
     * returns a <strong>NextMarker</strong> element within the response if the
     * list returned was not complete, which can be accessed with the
     * {@link ListQueuesResult#getNextMarker()} method. This opaque value may
     * then be set on a {@link ListQueuesOptions} instance with a call to
     * {@link ListQueuesOptions#setMarker(String) setMarker} to be used in a
     * subsequent {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues}
     * call to request the next portion of the list of queues.
     * 
     * @param marker
     *            The {@link String} marker value to set.
     * @return A reference to this {@link ListQueuesOptions} instance.
     */
    public ListQueuesOptions setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    /**
     * Gets the maximum number of queues to return with a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues} request.
     * If the value is not specified, the server will return up to 5,000 items.
     * 
     * @return The maximum number of queues to return.
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Sets the maximum number of queues to return with a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues} request.
     * If the value is not specified, by default the server will return up to
     * 5,000 items.
     * <p>
     * The maxResults value only affects calls made on methods where this
     * {@link ListQueuesOptions} instance is passed as a parameter.
     * 
     * @param maxResults
     *            The maximum number of queues to return.
     * @return A reference to this {@link ListQueuesOptions} instance.
     */
    public ListQueuesOptions setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Gets a flag indicating whether to return metadata with a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues} request.
     * 
     * @return <code>true</code> to return metadata.
     */
    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    /**
     * Sets a flag indicating whether to return metadata with a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues} request.
     * 
     * @param includeMetadata
     *            <code>true</code> to return metadata.
     * @return A reference to this {@link ListQueuesOptions} instance.
     */
    public ListQueuesOptions setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }
}
