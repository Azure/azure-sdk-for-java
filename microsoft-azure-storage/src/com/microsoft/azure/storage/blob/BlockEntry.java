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
 * A class which is used to list and commit blocks of a {@link CloudBlockBlob}.
 */
public final class BlockEntry {
    /**
     * Represents the name of the block.
     */
    private String id;

    /**
     * Represents the size, in bytes, of the block.
     */
    private long size;

    /**
     * Represents the block search mode. The default value is {@link BlockSearchMode#LATEST}.
     */
    private BlockSearchMode searchMode;

    /**
     * Creates an instance of the <code>BlockEntry</code> class with the specified id and default search mode
     * {@link BlockSearchMode#LATEST}.
     * 
     * @param id
     *            A <code>String</code> that represents the name of the block.
     */
    public BlockEntry(final String id) {
        this.setId(id);
        this.searchMode = BlockSearchMode.LATEST;
    }

    /**
     * Creates an instance of the <code>BlockEntry</code> class with the specified id and search mode.
     * 
     * @param id
     *            A <code>String</code> that represents the name of the block.
     * @param searchMode
     *            A {@link BlockSearchMode} value that represents the block search mode.
     */
    public BlockEntry(final String id, final BlockSearchMode searchMode) {
        this.setId(id);
        this.searchMode = searchMode;
    }

    /**
     * Gets the id of the block. The block id is a valid Base64 string value that identifies the block. Prior to
     * encoding, the string must be less than or equal to 64 bytes in size. For a given blob, the length of the block id
     * must be the same size for each block.
     * 
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the size, in bytes, of the block.
     * 
     * @return the size
     */
    public long getSize() {
        return this.size;
    }

    /**
     * Gets the {@link BlockSearchMode}.
     * 
     * @return the block search mode
     */
    public BlockSearchMode getSearchMode() {
        return searchMode;
    }

    /**
     * Sets the id of the block. The block id is a valid Base64 string value that identifies the block. Prior to
     * encoding, the string must be less than or equal to 64 bytes in size. For a given blob, the length of the block id
     * must be the same size for each block.
     * 
     * @param id
     *            the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Sets the size, in bytes, of the block.
     * 
     * @param size
     *            the size to set
     */
    public void setSize(final long size) {
        this.size = size;
    }

    /**
     * Sets the {@link BlockSearchMode}.
     * 
     * @param searchMode
     *            the block search mode to set
     */
    public void setSearchMode(BlockSearchMode searchMode) {
        this.searchMode = searchMode;
    }
}
