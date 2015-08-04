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
 * Class representing a set of statistics pertaining to a cloud file share.
 */
public final class ShareStats {
    /**
     * The approximate size of the data stored on the share, in GB.
     */
    private int usage;

    /**
     * Gets the approximate size of the data stored on the share, in GB.
     * 
     * @return the share usage
     */
    public int getUsage() {
        return usage;
    }

    /**
     * Sets approximate size of the data stored on the share, in GB.
     * 
     * @param usage
     *            The approximate size of the data stored on the share, in GB.
     */
    void setUsage(int usage) {
        this.usage = usage;
    }
}