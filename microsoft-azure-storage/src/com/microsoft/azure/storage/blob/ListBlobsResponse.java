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
package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.core.ListResponse;

/**
 * RESERVED FOR INTERNAL USE. A class used to parse a list blobs response stream.
 */
final class ListBlobsResponse extends ListResponse<ListBlobItem> {

    /**
     * Stores the blob delimiter.
     */
    private String delimiter;

    /**
     * Gets the delimiter.
     * 
     * @return the delimiter
     */
    public String getDelimiter() {
        return this.delimiter;
    }

    /**
     * Sets the delimiter
     * 
     * @param delimiter
     *            the delimiter to set
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
}
