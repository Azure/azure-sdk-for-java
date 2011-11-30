/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.blob.client;

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
    public BlockSearchMode searchMode = BlockSearchMode.LATEST;

    /**
     * Creates an instance of the <code>BlockEntry</code> class.
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
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return this.size;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @param size
     *            the size to set
     */
    public void setSize(final long size) {
        this.size = size;
    }
}
