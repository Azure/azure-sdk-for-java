package com.microsoft.windowsazure.services.blob.models;

import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.services.blob.implementation.RFC1123DateAdapter;

// TODO: Unify this with ListBlobsResults.BlobProperties
public class BlobProperties {
    private Date lastModified;
    private String etag;
    private String contentType;
    private long contentLength;
    private String contentEncoding;
    private String contentLanguage;
    private String contentMD5;
    private String cacheControl;
    private String blobType;
    private String leaseStatus;
    private long sequenceNumber;
    private HashMap<String, String> metadata = new HashMap<String, String>();

    @XmlElement(name = "Last-Modified")
    @XmlJavaTypeAdapter(RFC1123DateAdapter.class)
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @XmlElement(name = "Etag")
    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    @XmlElement(name = "Content-Type")
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @XmlElement(name = "Content-Length")
    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @XmlElement(name = "Content-Encoding")
    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    @XmlElement(name = "Content-Language")
    public String getContentLanguage() {
        return contentLanguage;
    }

    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    @XmlElement(name = "Content-MD5")
    public String getContentMD5() {
        return contentMD5;
    }

    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    @XmlElement(name = "Cache-Control")
    public String getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    @XmlElement(name = "BlobType")
    public String getBlobType() {
        return blobType;
    }

    public void setBlobType(String blobType) {
        this.blobType = blobType;
    }

    @XmlElement(name = "LeaseStatus")
    public String getLeaseStatus() {
        return leaseStatus;
    }

    public void setLeaseStatus(String leaseStatus) {
        this.leaseStatus = leaseStatus;
    }

    @XmlElement(name = "x-ms-blob-sequence-number")
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNUmber(long sequenceNUmber) {
        this.sequenceNumber = sequenceNUmber;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }
}
