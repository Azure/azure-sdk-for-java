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
 * {@link com.microsoft.windowsazure.services.queue.QueueContract#peekMessages(String, PeekMessagesOptions) peekMessages}
 * request. These options include a server response timeout for the request and
 * the number of messages to peek from the queue.
 */
public class PeekMessagesOptions extends QueueServiceOptions {
    private Integer numberOfMessages;

    /**
     * Sets the server request timeout value associated with this
     * {@link PeekMessagesOptions} instance.
     * <p>
     * The timeout value only affects calls made on methods where this
     * {@link PeekMessagesOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link PeekMessagesOptions} instance.
     */
    @Override
    public PeekMessagesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the number of messages to return in the response to a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#peekMessages(String, PeekMessagesOptions)
     * peekMessages} request specified in this instance.
     * 
     * @return The number of messages to return in the response.
     */
    public Integer getNumberOfMessages() {
        return numberOfMessages;
    }

    /**
     * Sets the number of messages to return in the response to a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#peekMessages(String, PeekMessagesOptions)
     * peekMessages} request.
     * <p>
     * The <em>numberOfMessages</em> value only affects calls made on methods
     * where this {@link PeekMessagesOptions} instance is passed as a parameter.
     * 
     * 
     * @param numberOfMessages
     *            The number of messages to return in the response. This value
     *            must be in the range from 0 to 32.
     * @return A reference to this {@link PeekMessagesOptions} instance.
     */
    public PeekMessagesOptions setNumberOfMessages(Integer numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
        return this;
    }
}
