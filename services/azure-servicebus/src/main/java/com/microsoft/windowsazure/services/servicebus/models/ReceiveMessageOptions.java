/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.microsoft.windowsazure.services.servicebus.models;

/**
 * 
 * Specifies options when receiving messages.
 * 
 */
public class ReceiveMessageOptions {
    private Integer timeout;
    private ReceiveMode receiveMode = ReceiveMode.RECEIVE_AND_DELETE;

    /**
     * Returns a new instance of the <code>ReceiveMessageOptions</code> class.
     */
    public static final ReceiveMessageOptions DEFAULT = new ReceiveMessageOptions();

    /**
     * Returns the timeout when receiving messages.
     * 
     * @return The timeout, in seconds.
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout when receiving messages.
     * 
     * @param timeout
     *            The timeout, in seconds.
     * 
     * @return A <code>ReceiveMessageOptions</code> object that represents the
     *         updated receive message options.
     */
    public ReceiveMessageOptions setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Returns the receive mode when receiving messages.
     * 
     * @return A {@link ReceiveMode} value that represents the receive mode.
     */
    public ReceiveMode getReceiveMode() {
        return receiveMode;
    }

    /**
     * Sets the receive mode when receiving messages.
     * 
     * @param receiveMode
     *            A {@link ReceiveMode} value that specifies the receive mode.
     * 
     * @return A <code>ReceiveMessageOptions</code> object that represents the
     *         updated receive message options.
     */
    public ReceiveMessageOptions setReceiveMode(ReceiveMode receiveMode) {
        this.receiveMode = receiveMode;
        return this;
    }

    /**
     * Indicates whether the receive mode is receive and delete.
     * 
     * @return <code>true</code> if the receive mode is
     *         {@link ReceiveMode#RECEIVE_AND_DELETE}; otherwise,
     *         <code>false</code>.
     */
    public boolean isReceiveAndDelete() {
        return receiveMode == ReceiveMode.RECEIVE_AND_DELETE;
    }

    /**
     * Sets the receive mode to receive and delete.
     * 
     * @return A <code>ReceiveMessageOptions</code> object that represents the
     *         updated receive message options.
     */
    public ReceiveMessageOptions setReceiveAndDelete() {
        this.receiveMode = ReceiveMode.RECEIVE_AND_DELETE;
        return this;
    }

    /**
     * Indicates whether the receive mode is peek/lock.
     * 
     * @return <code>true</code> if the receive mode is
     *         {@link ReceiveMode#PEEK_LOCK}; otherwise, <code>false</code>.
     */
    public boolean isPeekLock() {
        return receiveMode == ReceiveMode.PEEK_LOCK;
    }

    /**
     * Sets the receive mode to peek/lock.
     * 
     * @return A <code>ReceiveMessageOptions</code> object that represents the
     *         updated receive message options.
     */
    public ReceiveMessageOptions setPeekLock() {
        this.receiveMode = ReceiveMode.PEEK_LOCK;
        return this;
    }
}
