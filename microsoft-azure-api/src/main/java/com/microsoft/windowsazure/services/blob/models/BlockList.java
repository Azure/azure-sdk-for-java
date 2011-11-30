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
package com.microsoft.windowsazure.services.blob.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.services.core.utils.pipeline.Base64StringAdapter;

@XmlRootElement(name = "BlockList")
public class BlockList {
    private List<Entry> entries = new ArrayList<Entry>();

    public BlockList addCommittedEntry(String blockId) {
        CommittedEntry entry = new CommittedEntry();
        entry.setBlockId(blockId);
        getEntries().add(entry);
        return this;
    }

    public BlockList addUncommittedEntry(String blockId) {
        UncommittedEntry entry = new UncommittedEntry();
        entry.setBlockId(blockId);
        getEntries().add(entry);
        return this;
    }

    public BlockList addLatestEntry(String blockId) {
        LatestEntry entry = new LatestEntry();
        entry.setBlockId(blockId);
        getEntries().add(entry);
        return this;
    }

    @XmlElementRefs({ @XmlElementRef(name = "Committed", type = CommittedEntry.class),
            @XmlElementRef(name = "Uncommitted", type = UncommittedEntry.class),
            @XmlElementRef(name = "Latest", type = LatestEntry.class) })
    @XmlMixed
    public List<Entry> getEntries() {
        return entries;
    }

    public BlockList setEntries(List<Entry> entries) {
        this.entries = entries;
        return this;
    }

    public abstract static class Entry {
        private String blockId;

        @XmlJavaTypeAdapter(Base64StringAdapter.class)
        @XmlValue
        public String getBlockId() {
            return blockId;
        }

        public void setBlockId(String blockId) {
            this.blockId = blockId;
        }
    }

    @XmlRootElement(name = "Committed")
    public static class CommittedEntry extends Entry {
    }

    @XmlRootElement(name = "Uncommitted")
    public static class UncommittedEntry extends Entry {
    }

    @XmlRootElement(name = "Latest")
    public static class LatestEntry extends Entry {
    }
}
