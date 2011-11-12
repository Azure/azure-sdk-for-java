package com.microsoft.windowsazure.services.blob.models;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PageList")
public class ListBlobRegionsResult {
    private Date lastModified;
    private String etag;
    private long contentLength;
    private List<PageRange> pageRanges;

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

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @XmlElement(name = "PageRange")
    public List<PageRange> getPageRanges() {
        return pageRanges;
    }

    public void setPageRanges(List<PageRange> pageRanges) {
        this.pageRanges = pageRanges;
    }
}
