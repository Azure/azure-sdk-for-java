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

import java.util.Date;

/**
 * A wrapper class for the results returned in response to Queue Service REST
 * API operations to update a message. This is returned by calls to
 * implementations of
 * {@link com.microsoft.windowsazure.services.queue.QueueContract#updateMessage(String, String, String, String, int)} and
 * {@link com.microsoft.windowsazure.services.queue.QueueContract#updateMessage(String, String, String, String, int, QueueServiceOptions)}
 * .
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/hh452234.aspx"
 * >Update Message</a> documentation on MSDN for details of the underlying Queue
 * Service REST API operation.
 */
public class UpdateMessageResult {
    private String popReceipt;
    private Date timeNextVisible;

    /**
     * Gets the pop receipt value for the updated queue message. The pop receipt
     * is a value that is opaque to the client that must be used along with the
     * message ID to validate an update message or delete message operation.
     * 
     * @return A {@link String} containing the pop receipt value for the queue
     *         message.
     */
    public String getPopReceipt() {
        return popReceipt;
    }

    /**
     * Reserved for internal use. Sets the value of the pop receipt for the
     * updated queue message. This method is invoked by the API as part of the
     * response generation from the Queue Service REST API operation to set the
     * value with the pop receipt returned by the server.
     * 
     * @param popReceipt
     *            A {@link String} containing the pop receipt value for the
     *            queue message.
     */
    public void setPopReceipt(String popReceipt) {
        this.popReceipt = popReceipt;
    }

    /**
     * Gets the {@link Date} when the updated message will become visible in the
     * queue.
     * 
     * @return The {@link Date} when the updated message will become visible in
     *         the queue.
     */
    public Date getTimeNextVisible() {
        return timeNextVisible;
    }

    /**
     * Reserved for internal use. Sets the value of the time the updated message
     * will become visible. This method is invoked by the API as part of the
     * response generation from the Queue Service REST API operation to set the
     * value with the time next visible returned by the server.
     * 
     * @param timeNextVisible
     *            The {@link Date} when the updated message will become visible
     *            in the queue.
     */
    public void setTimeNextVisible(Date timeNextVisible) {
        this.timeNextVisible = timeNextVisible;
    }
}
