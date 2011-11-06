package com.microsoft.azure.services.blob;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "EnumerationResults")
public class ListBlobsResult {
    private List<Blob> blobs;
    private String containerName;
    private String prefix;
    private String marker;
    private String nextMarker;
    private String delimiter;
    private int maxResults;

    @XmlElementWrapper(name = "Blobs")
    @XmlElement(name = "Blob")
    public List<Blob> getBlobs() {
        return blobs;
    }

    public void setBlobs(List<Blob> value) {
        this.blobs = value;
    }

    @XmlElement(name = "Prefix")
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @XmlElement(name = "Marker")
    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    @XmlElement(name = "NextMarker")
    public String getNextMarker() {
        return nextMarker;
    }

    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }

    @XmlElement(name = "MaxResults")
    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    @XmlElement(name = "Delimiter")
    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @XmlAttribute(name = "ContainerName")
    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public static class Blob {
        private String name;
        private String url;
        private String snapshot;
        private HashMap<String, String> metadata = new HashMap<String, String>();
        private BlobProperties properties;

        @XmlElement(name = "Name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlElement(name = "Url")
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @XmlElement(name = "Properties")
        public BlobProperties getProperties() {
            return properties;
        }

        public void setProperties(BlobProperties properties) {
            this.properties = properties;
        }

        @XmlElement(name = "Snapshot")
        public String getSnapshot() {
            return snapshot;
        }

        public void setSnapshot(String snapshot) {
            this.snapshot = snapshot;
        }

        @XmlElement(name = "Metadata")
        @XmlJavaTypeAdapter(MetadataAdapter.class)
        public HashMap<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(HashMap<String, String> metadata) {
            this.metadata = metadata;
        }

        public static class BlobProperties {
            private Date lastModified;
            private String etag;
            private String contentType;
            private String contentLength;
            private String contentEncoding;
            private String contentLanguage;
            private String contentMD5;
            private String cacheControl;
            private String blobType;
            private String leaseStatus;
            private String sequenceNUmber;

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
            public String getContentLength() {
                return contentLength;
            }

            public void setContentLength(String contentLength) {
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
            public String getSequenceNUmber() {
                return sequenceNUmber;
            }

            public void setSequenceNUmber(String sequenceNUmber) {
                this.sequenceNUmber = sequenceNUmber;
            }
        }
    }
}
