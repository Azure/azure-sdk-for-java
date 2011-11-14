package com.microsoft.windowsazure.services.blob.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.services.blob.implementation.MetadataAdapter;

@XmlRootElement(name = "EnumerationResults")
public class ListBlobsResult {
    private List<BlobPrefixEntry> blobPrefixes = new ArrayList<BlobPrefixEntry>();
    private List<BlobEntry> blobs = new ArrayList<BlobEntry>();
    private String containerName;
    private String prefix;
    private String marker;
    private String nextMarker;
    private String delimiter;
    private int maxResults;

    @XmlElementWrapper(name = "Blobs")
    @XmlElementRefs({ @XmlElementRef(name = "BlobPrefix", type = BlobPrefixEntry.class), @XmlElementRef(name = "Blob", type = BlobEntry.class) })
    public List<ListBlobsEntry> getEntries() {
        ArrayList<ListBlobsEntry> result = new ArrayList<ListBlobsEntry>();
        result.addAll(this.blobPrefixes);
        result.addAll(this.blobs);
        return result;
    }

    public void setEntries(List<ListBlobsEntry> entries) {
        // Split collection into "blobs" and "blobPrefixes" collections
        this.blobPrefixes = new ArrayList<BlobPrefixEntry>();
        this.blobs = new ArrayList<BlobEntry>();

        for (ListBlobsEntry entry : entries) {
            if (entry instanceof BlobPrefixEntry) {
                this.blobPrefixes.add((BlobPrefixEntry) entry);
            }
            else if (entry instanceof BlobEntry) {
                this.blobs.add((BlobEntry) entry);
            }
        }
    }

    public List<BlobPrefixEntry> getBlobPrefixes() {
        return this.blobPrefixes;
    }

    public List<BlobEntry> getBlobs() {
        return this.blobs;
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

    public static abstract class ListBlobsEntry {

    }

    @XmlRootElement(name = "BlobPrefix")
    public static class BlobPrefixEntry extends ListBlobsEntry {
        private String name;

        @XmlElement(name = "Name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @XmlRootElement(name = "Blob")
    public static class BlobEntry extends ListBlobsEntry {
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
    }
}
