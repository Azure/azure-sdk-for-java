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
 * Represents the result of a <code>receiveMessage</code> operation.
 */
public class ReceiveMessageResult {

    private BrokeredMessage value;

    /**
     * Creates an instance of the <code>ReceiveQueueMessageResult</code> class.
     * 
     * @param value
     *            A {@link BrokeredMessage} object assigned as the value of the
     *            result.
     */
    public ReceiveMessageResult(BrokeredMessage value) {
        this.setValue(value);
    }

    /**
     * Specifies the value of the result.
     * 
     * @param value
     *            A {@link BrokeredMessage} object assigned as the value of the
     *            result.
     */
    public void setValue(BrokeredMessage value) {
        this.value = value;
    }

    /**
     * Returns the value of the result.
     * 
     * @return A {@link BrokeredMessage} object that represents the value of the
     *         result.
     */
    public BrokeredMessage getValue() {
        return value;
    }

}
