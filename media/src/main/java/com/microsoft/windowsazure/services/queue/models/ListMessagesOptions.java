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
 * Represents the options that may be set on a
 * {@link com.microsoft.windowsazure.services.queue.QueueContract#listMessages(String, ListMessagesOptions) listMessages}
 * request. These options include a server response timeout for the request, the
 * number of messages to retrieve from the queue, and the visibility timeout to
 * set on the retrieved messages.
 */
public class ListMessagesOptions extends QueueServiceOptions {
    private Integer numberOfMessages;
    private Integer visibilityTimeoutInSeconds;

    /**
     * Sets the server request timeout value associated with this
     * {@link ListMessagesOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link ListMessagesOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link ListMessagesOptions} instance.
     */
    @Override
    public ListMessagesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the number of messages to request from the queue with this
     * {@link ListMessagesOptions} instance.
     * 
     * @return The number of messages requested.
     */
    public Integer getNumberOfMessages() {
        return numberOfMessages;
    }

    /**
     * Sets the number of messages to request from the queue with this
     * {@link ListMessagesOptions} instance.
     * <p>
     * The <em>numberOfMessages</em> value is only used for requests where this
     * {@link ListMessagesOptions} instance is passed as a parameter.
     * 
     * @param numberOfMessages
     *            The number of messages to request. The valid range of values
     *            is 0 to 32.
     * @return A reference to this {@link ListMessagesOptions} instance.
     */
    public ListMessagesOptions setNumberOfMessages(Integer numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
        return this;
    }

    /**
     * Gets the visibility timeout to set on the messages requested from the
     * queue with this {@link ListMessagesOptions} instance.
     * 
     * @return The visibility timeout to set on the messages requested from the
     *         queue.
     */
    public Integer getVisibilityTimeoutInSeconds() {
        return visibilityTimeoutInSeconds;
    }

    /**
     * Sets the visibility timeout value to set on the messages requested from
     * the queue with this {@link ListMessagesOptions} instance.
     * <p>
     * The <em>visibilityTimeoutInSeconds</em> value is only used for requests
     * where this {@link ListMessagesOptions} instance is passed as a parameter.
     * 
     * @param visibilityTimeoutInSeconds
     *            The visibility timeout to set on the messages requested from
     *            the queue. The valid range of values is 0 to 604800 seconds.
     * @return A reference to this {@link ListMessagesOptions} instance.
     */
    public ListMessagesOptions setVisibilityTimeoutInSeconds(
            Integer visibilityTimeoutInSeconds) {
        this.visibilityTimeoutInSeconds = visibilityTimeoutInSeconds;
        return this;
    }
}
