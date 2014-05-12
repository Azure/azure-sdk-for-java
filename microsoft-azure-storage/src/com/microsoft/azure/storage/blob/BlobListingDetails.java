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

/**
 * Specifies which items to include when listing a set of blobs.
 * <p>
 * By default, committed blocks are always returned. Use the values in this enum to include snapshots, metadata, and/or
 * uncommitted blocks.
 * <p>
 */
public enum BlobListingDetails {
    /**
     * Specifies listing committed blobs and blob snapshots.
     */
    SNAPSHOTS(1),

    /**
     * Specifies listing blob metadata for each blob returned in the listing.
     */
    METADATA(2),

    /**
     * Specifies listing uncommitted blobs.
     */
    UNCOMMITTED_BLOBS(4),

    /**
     * Include copy properties in the listing.
     */
    COPY(8);

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
    BlobListingDetails(final int val) {
        this.value = val;
    }
}
