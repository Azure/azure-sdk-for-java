/**
 * Copyright 2011 Microsoft Corporation
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
    @XmlElementRefs({ @XmlElementRef(name = "BlobPrefix", type = BlobPrefixEntry.class),
            @XmlElementRef(name = "Blob", type = BlobEntry.class) })
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

    /**
     * Gets the list of blobs that satisfied the request from the response. This list may contain only a portion of the
     * blobs that satisfy the request, limited by a server timeout or a maximum results parameter. If there are more
     * blobs that could satisfy the result, the server returns a <strong>NextMarker</strong> element with the response.
     * Use the {@link ListBlobsResult#getNextMarker() getNextMarker} method to get this value and pass it as a marker
     * option to a subsequent list blobs request to get the next set of blob results.
     * <p>
     * Blobs are listed in alphabetical order in the response body, with upper-case letters listed first.
     * 
     * @return
     *         A {@link List} of {@link BlobEntry} instances for the blobs that satisfied the request.
     */
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
