package com.microsoft.windowsazure.services.blob.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.utils.jersey.Base64StringAdapter;

@XmlRootElement(name = "BlockList")
public class ListBlobBlocksResult {
    private Date lastModified;
    private String etag;
    private String contentType;
    private long contentLength;
    private List<Entry> committedBlocks = new ArrayList<Entry>();
    private List<Entry> uncommittedBlocks = new ArrayList<Entry>();

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @XmlElementWrapper(name = "CommittedBlocks")
    @XmlElement(name = "Block")
    public List<Entry> getCommittedBlocks() {
        return committedBlocks;
    }

    public void setCommittedBlocks(List<Entry> committedBlocks) {
        this.committedBlocks = committedBlocks;
    }

    @XmlElementWrapper(name = "UncommittedBlocks")
    @XmlElement(name = "Block")
    public List<Entry> getUncommittedBlocks() {
        return uncommittedBlocks;
    }

    public void setUncommittedBlocks(List<Entry> uncommittedBlocks) {
        this.uncommittedBlocks = uncommittedBlocks;
    }

    public static class Entry {
        private String blockId;
        private long blockLength;

        @XmlElement(name = "Name")
        @XmlJavaTypeAdapter(Base64StringAdapter.class)
        public String getBlockId() {
            return blockId;
        }

        public void setBlockId(String blockId) {
            this.blockId = blockId;
        }

        @XmlElement(name = "Size")
        public long getBlockLength() {
            return blockLength;
        }

        public void setBlockLength(long blockLength) {
            this.blockLength = blockLength;
        }
    }
}
