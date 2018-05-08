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
package com.microsoft.windowsazure.services.blob.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.core.pipeline.Base64StringAdapter;

/**
 * Represents a list of blocks that may be committed to a block blob.
 */
@XmlRootElement(name = "BlockList")
public class BlockList {
    private List<Entry> entries = new ArrayList<Entry>();

    /**
     * Adds the committed block specified by the block ID to the block list.
     * 
     * @param blockId
     *            A {@link String} containing the client-specified block ID for
     *            a committed block.
     * @return A reference to this {@link BlockList} instance.
     */
    public BlockList addCommittedEntry(String blockId) {
        CommittedEntry entry = new CommittedEntry();
        entry.setBlockId(blockId);
        getEntries().add(entry);
        return this;
    }

    /**
     * Adds the uncommitted block specified by the block ID to the block list.
     * 
     * @param blockId
     *            A {@link String} containing the client-specified block ID for
     *            an uncommitted block.
     * @return A reference to this {@link BlockList} instance.
     */
    public BlockList addUncommittedEntry(String blockId) {
        UncommittedEntry entry = new UncommittedEntry();
        entry.setBlockId(blockId);
        getEntries().add(entry);
        return this;
    }

    /**
     * Adds the latest block specified by the block ID to the block list. An
     * entry of this type will cause the server commit the most recent
     * uncommitted block with the specified block ID, or the committed block
     * with the specified block ID if no uncommitted block is found.
     * 
     * @param blockId
     *            A {@link String} containing the client-specified block ID for
     *            the latest matching block.
     * @return A reference to this {@link BlockList} instance.
     */
    public BlockList addLatestEntry(String blockId) {
        LatestEntry entry = new LatestEntry();
        entry.setBlockId(blockId);
        getEntries().add(entry);
        return this;
    }

    /**
     * Gets the collection of entries for the block list.
     * 
     * @return A {@link List} of {@link Entry} instances specifying the blocks
     *         to commit.
     */
    @XmlElementRefs({
            @XmlElementRef(name = "Committed", type = CommittedEntry.class),
            @XmlElementRef(name = "Uncommitted", type = UncommittedEntry.class),
            @XmlElementRef(name = "Latest", type = LatestEntry.class) })
    @XmlMixed
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * Sets the block list to the specified collection of entries.
     * 
     * @param entries
     *            A {@link List} of {@link Entry} instances specifying the
     *            blocks to commit.
     * @return A reference to this {@link BlockList} instance.
     */
    public BlockList setEntries(List<Entry> entries) {
        this.entries = entries;
        return this;
    }

    /**
     * The abstract base class for an entry in a {@link BlockList}, representing
     * a committed or uncommitted block.
     */
    public abstract static class Entry {
        private String blockId;

        /**
         * Gets the client-specified block ID for a {@link BlockList} entry.
         * 
         * @return A {@link String} containing the client-specified block ID for
         *         a block.
         */
        @XmlJavaTypeAdapter(Base64StringAdapter.class)
        @XmlValue
        public String getBlockId() {
            return blockId;
        }

        /**
         * Sets the client-specified block ID for a {@link BlockList} entry.
         * 
         * @param blockId
         *            A {@link String} containing the client-specified block ID
         *            for the block.
         */
        public void setBlockId(String blockId) {
            this.blockId = blockId;
        }
    }

    /**
     * Represents an entry in a {@link BlockList} for a previously committed
     * block.
     */
    @XmlRootElement(name = "Committed")
    public static class CommittedEntry extends Entry {
    }

    /**
     * Represents an entry in a {@link BlockList} for an uncommitted block.
     */
    @XmlRootElement(name = "Uncommitted")
    public static class UncommittedEntry extends Entry {
    }

    /**
     * Represents an entry in a {@link BlockList} for the most recent
     * uncommitted block with the specified block ID, or the committed block
     * with the specified block ID if no uncommitted block is found.
     */
    @XmlRootElement(name = "Latest")
    public static class LatestEntry extends Entry {
    }
}
