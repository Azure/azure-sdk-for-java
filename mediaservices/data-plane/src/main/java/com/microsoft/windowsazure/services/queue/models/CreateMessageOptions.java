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
 * {@link com.microsoft.windowsazure.services.queue.QueueContract#createMessage(String, String, CreateMessageOptions)
 * createMessage} requests. These options include a server response timeout for
 * the request, the visibility timeout to set on the created message, and the
 * time-to-live value to set on the message.
 */
public class CreateMessageOptions extends QueueServiceOptions {
    private Integer visibilityTimeoutInSeconds;
    private Integer timeToLiveInSeconds;

    /**
     * Sets the server request timeout value associated with this
     * {@link CreateMessageOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link CreateMessageOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link CreateMessageOptions} instance.
     */
    @Override
    public CreateMessageOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the message visibility timeout in seconds value in this
     * {@link CreateMessageOptions} instance. to set on messages when making a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#createMessage(String, String, CreateMessageOptions)
     * createMessage} request.
     * 
     * @return The message visibility timeout in seconds.
     */
    public Integer getVisibilityTimeoutInSeconds() {
        return visibilityTimeoutInSeconds;
    }

    /**
     * Sets the message visibility timeout in seconds value to set on messages
     * when making a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#createMessage(String, String, CreateMessageOptions)
     * createMessage} request. This allows messages to be loaded into the queue
     * but not become visible until the visibility timeout has passed. Valid
     * visibility timeout values range from 0 to 604800 seconds (0 to 7 days),
     * and must be less than the time-to-live value.
     * <p>
     * The <em>visibilityTimeoutInSeconds</em> value only affects calls made on
     * methods where this {@link CreateMessageOptions} instance is passed as a
     * parameter.
     * 
     * @param visibilityTimeoutInSeconds
     *            The length of time during which the message will be invisible,
     *            starting when it is added to the queue, or 0 to make the
     *            message visible immediately. This value must be greater than
     *            or equal to zero and less than or equal to the time-to-live
     *            value.
     * @return A reference to this {@link CreateMessageOptions} instance.
     */
    public CreateMessageOptions setVisibilityTimeoutInSeconds(
            Integer visibilityTimeoutInSeconds) {
        this.visibilityTimeoutInSeconds = visibilityTimeoutInSeconds;
        return this;
    }

    /**
     * Gets the message time-to-live in seconds value associated with this
     * {@link CreateMessageOptions} instance.
     * 
     * @return The message time-to-live value in seconds.
     */
    public Integer getTimeToLiveInSeconds() {
        return timeToLiveInSeconds;
    }

    /**
     * Sets the message time-to-live timeout value to set on messages when
     * making a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#createMessage(String, String, CreateMessageOptions)
     * createMessage} request. This is the maximum duration in seconds for the
     * message to remain in the queue after it is created. Valid
     * <em>timeToLiveInSeconds</em> values range from 0 to 604800 seconds (0 to
     * 7 days), with the default value set to seven days.
     * <p>
     * The <em>timeToLiveInSeconds</em> value only affects calls made on methods
     * where this {@link CreateMessageOptions} instance is passed as a
     * parameter.
     * 
     * @param timeToLiveInSeconds
     *            The maximum time to allow the message to be in the queue, in
     *            seconds.
     * @return A reference to this {@link CreateMessageOptions} instance.
     */
    public CreateMessageOptions setTimeToLiveInSeconds(
            Integer timeToLiveInSeconds) {
        this.timeToLiveInSeconds = timeToLiveInSeconds;
        return this;
    }
}
