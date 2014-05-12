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

package com.microsoft.azure.storage.queue;

/**
 * Flags for the values to set when updating messages.
 */
public enum MessageUpdateFields {
    /**
     * Set to update the message visibility timeout.
     */
    VISIBILITY(1),

    /**
     * Set to update the message content.
     */
    CONTENT(2);

    /**
     * Returns the value of this enum.
     */
    public int value;

    /**
     * Sets the value of this enum.
     * 
     * @param val
     *        An <code>int</code> which represents the value being assigned.
     */
    private MessageUpdateFields(final int val) {
        this.value = val;
    }
}
