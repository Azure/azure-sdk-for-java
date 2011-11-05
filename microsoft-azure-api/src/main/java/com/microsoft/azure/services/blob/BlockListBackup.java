package com.microsoft.azure.services.blob;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "BlockList")
public class BlockListBackup {
    private List<String> committedEntries = new ArrayList<String>();
    private List<String> uncommittedEntries = new ArrayList<String>();
    private List<String> latestEntries = new ArrayList<String>();

    public BlockListBackup addCommittedEntry(String blockId) {
        getCommittedEntries().add(blockId);
        return this;
    }

    public BlockListBackup addUncommittedEntry(String blockId) {
        getUncommittedEntries().add(blockId);
        return this;
    }

    public BlockListBackup addLatestEntry(String blockId) {
        getLatestEntries().add(blockId);
        return this;
    }

    @XmlElement(name = "Committed")
    @XmlJavaTypeAdapter(Base64StringAdapter.class)
    public List<String> getCommittedEntries() {
        return committedEntries;
    }

    public BlockListBackup setCommittedEntries(List<String> committedEntries) {
        this.committedEntries = committedEntries;
        return this;
    }

    @XmlElement(name = "Uncommitted")
    @XmlJavaTypeAdapter(Base64StringAdapter.class)
    public List<String> getUncommittedEntries() {
        return uncommittedEntries;
    }

    public BlockListBackup setUncommittedEntries(List<String> uncommittedEntries) {
        this.uncommittedEntries = uncommittedEntries;
        return this;
    }

    @XmlElement(name = "Latest")
    @XmlJavaTypeAdapter(Base64StringAdapter.class)
    public List<String> getLatestEntries() {
        return latestEntries;
    }

    public BlockListBackup setLatestEntries(List<String> latestEntries) {
        this.latestEntries = latestEntries;
        return this;
    }
}
/*
public class BlockList {
    private List<String> committedEntries = new ArrayList<String>();
    private List<String> uncommittedEntries = new ArrayList<String>();
    private List<String> latestEntries = new ArrayList<String>();

    public BlockList addCommittedEntry(String blockId) {
        getCommittedEntries().add(blockId);
        return this;
    }

    public BlockList addUncommittedEntry(String blockId) {
        getUncommittedEntries().add(blockId);
        return this;
    }

    public BlockList addLatestEntry(String blockId) {
        getLatestEntries().add(blockId);
        return this;
    }

    @XmlElement(name = "Committed")
    @XmlJavaTypeAdapter(Base64StringAdapter.class)
    public List<String> getCommittedEntries() {
        return committedEntries;
    }

    public BlockList setCommittedEntries(List<String> committedEntries) {
        this.committedEntries = committedEntries;
        return this;
    }

    @XmlElement(name = "Uncommitted")
    @XmlJavaTypeAdapter(Base64StringAdapter.class)
    public List<String> getUncommittedEntries() {
        return uncommittedEntries;
    }

    public BlockList setUncommittedEntries(List<String> uncommittedEntries) {
        this.uncommittedEntries = uncommittedEntries;
        return this;
    }

    @XmlElement(name = "Latest")
    @XmlJavaTypeAdapter(Base64StringAdapter.class)
    public List<String> getLatestEntries() {
        return latestEntries;
    }

    public BlockList setLatestEntries(List<String> latestEntries) {
        this.latestEntries = latestEntries;
        return this;
    }
    */
