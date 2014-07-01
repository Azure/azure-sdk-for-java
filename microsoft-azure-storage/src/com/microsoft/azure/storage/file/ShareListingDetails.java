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
package com.microsoft.azure.storage.file;

/**
 * Specifies which details to include when listing the shares in this storage account.
 */
public enum ShareListingDetails {
    /**
     * Specifies including all available details.
     */
    ALL(1),

    /**
     * Specifies including share metadata.
     */
    METADATA(1),

    /**
     * Specifies including no additional details.
     */
    NONE(0);

    /**
     * Returns the value of this enum.
     */
    public int value;

    /**
     * Sets the value of this enum.
     * 
     * @param val
     *            An <code>int</code> which represents the value being assigned.
     */
    ShareListingDetails(final int val) {
        this.value = val;
    }
}
