package com.microsoft.windowsazure.services.blob.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.services.blob.implementation.Base64StringAdapter;

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

    @XmlElementRefs({ @XmlElementRef(name = "Committed", type = CommittedEntry.class), @XmlElementRef(name = "Uncommitted", type = UncommittedEntry.class),
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
