// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.blob.models;

import com.azure.storage.blob.models.Block;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * The BlockList model.
 */
@JacksonXmlRootElement(localName = "BlockList")
public final class BlockList {
    private static final class CommittedBlocksWrapper {
        @JacksonXmlProperty(localName = "Block")
        private final List<com.azure.storage.blob.models.Block> items;

        @JsonCreator
        private CommittedBlocksWrapper(@JacksonXmlProperty(localName = "Block") List<com.azure.storage.blob.models.Block> items) {
            this.items = items;
        }
    }

    /*
     * The committedBlocks property.
     */
    @JsonProperty(value = "CommittedBlocks")
    private CommittedBlocksWrapper committedBlocks;

    private static final class UncommittedBlocksWrapper {
        @JacksonXmlProperty(localName = "Block")
        private final List<com.azure.storage.blob.models.Block> items;

        @JsonCreator
        private UncommittedBlocksWrapper(@JacksonXmlProperty(localName = "Block") List<com.azure.storage.blob.models.Block> items) {
            this.items = items;
        }
    }

    /*
     * The uncommittedBlocks property.
     */
    @JsonProperty(value = "UncommittedBlocks")
    private UncommittedBlocksWrapper uncommittedBlocks;

    /**
     * Get the committedBlocks property: The committedBlocks property.
     *
     * @return the committedBlocks value.
     */
    public List<com.azure.storage.blob.models.Block> committedBlocks() {
        if (this.committedBlocks == null) {
            this.committedBlocks = new CommittedBlocksWrapper(new ArrayList<com.azure.storage.blob.models.Block>());
        }
        return this.committedBlocks.items;
    }

    /**
     * Set the committedBlocks property: The committedBlocks property.
     *
     * @param committedBlocks the committedBlocks value to set.
     * @return the BlockList object itself.
     */
    public BlockList committedBlocks(List<com.azure.storage.blob.models.Block> committedBlocks) {
        this.committedBlocks = new CommittedBlocksWrapper(committedBlocks);
        return this;
    }

    /**
     * Get the uncommittedBlocks property: The uncommittedBlocks property.
     *
     * @return the uncommittedBlocks value.
     */
    public List<com.azure.storage.blob.models.Block> uncommittedBlocks() {
        if (this.uncommittedBlocks == null) {
            this.uncommittedBlocks = new UncommittedBlocksWrapper(new ArrayList<com.azure.storage.blob.models.Block>());
        }
        return this.uncommittedBlocks.items;
    }

    /**
     * Set the uncommittedBlocks property: The uncommittedBlocks property.
     *
     * @param uncommittedBlocks the uncommittedBlocks value to set.
     * @return the BlockList object itself.
     */
    public BlockList uncommittedBlocks(List<Block> uncommittedBlocks) {
        this.uncommittedBlocks = new UncommittedBlocksWrapper(uncommittedBlocks);
        return this;
    }
}
