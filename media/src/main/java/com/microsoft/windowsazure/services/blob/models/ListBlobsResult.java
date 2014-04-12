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

/**
 * A wrapper class for the response returned from a Blob Service REST API List
 * Blobs operation. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobs(String)} and
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobs(String, ListBlobsOptions)}.
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd135734.aspx"
 * >List Blobs</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operation.
 */
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

    /**
     * Gets the list of <code>ListBlobsEntry</code> entries generated from the
     * server response to the list blobs request.
     * 
     * @return The {@link List} of {@link ListBlobsEntry} entries generated from
     *         the server response to the list blobs request.
     */
    @XmlElementWrapper(name = "Blobs")
    @XmlElementRefs({
            @XmlElementRef(name = "BlobPrefix", type = BlobPrefixEntry.class),
            @XmlElementRef(name = "Blob", type = BlobEntry.class) })
    public List<ListBlobsEntry> getEntries() {
        ArrayList<ListBlobsEntry> result = new ArrayList<ListBlobsEntry>();
        result.addAll(this.blobPrefixes);
        result.addAll(this.blobs);
        return result;
    }

    /**
     * Sets the lists of blob entries and blob prefix entries from a common list
     * of <code>ListBlobsEntry</code> entries generated from the server response
     * to the list blobs request.
     * 
     * @param entries
     *            The {@link List} of {@link ListBlobsEntry} entries to set the
     *            lists of blob entries and blob prefix entries from.
     */
    public void setEntries(List<ListBlobsEntry> entries) {
        // Split collection into "blobs" and "blobPrefixes" collections
        this.blobPrefixes = new ArrayList<BlobPrefixEntry>();
        this.blobs = new ArrayList<BlobEntry>();

        for (ListBlobsEntry entry : entries) {
            if (entry instanceof BlobPrefixEntry) {
                this.blobPrefixes.add((BlobPrefixEntry) entry);
            } else if (entry instanceof BlobEntry) {
                this.blobs.add((BlobEntry) entry);
            }
        }
    }

    /**
     * Gets the list of blob prefix entries that satisfied the request. Each
     * <code>BlobPrefixEntry</code> represents one or more blobs that have a
     * common substring up to the delimiter specified in the request options.
     * <p>
     * This list may contain only a portion of the blob prefix entries that
     * satisfy the request, limited by a server timeout or a maximum results
     * parameter. If there are more blob entries and blob prefix entries that
     * could satisfy the result, the server returns a
     * <strong>NextMarker</strong> element with the response.
     * <p>
     * The delimiter option enables the caller to traverse the blob namespace by
     * using a user-configured delimiter. In this way, you can traverse a
     * virtual hierarchy of blobs as though it were a file system. The delimiter
     * may be a single character or a string. When the request includes the
     * delimiter option, the response includes a <code>BlobPrefixEntry</code> in
     * place of all blobs whose names begin with the same substring up to the
     * appearance of the delimiter string. The value of
     * {@link BlobPrefixEntry#getName()} is <em>substring</em>+
     * <em>delimiter</em> , where <em>substring</em> is the common substring
     * that begins one or more blob names, and <em>delimiter</em> is the value
     * of the delimiter option.
     * <p>
     * To move down a level in the virtual hierarchy, you can use the
     * {@link BlobPrefixEntry#getName()} method to get the prefix value to use
     * to make a subsequent call to list blobs that begin with this prefix.
     * <p>
     * Use the {@link ListBlobsResult#getNextMarker() getNextMarker} method to
     * get this value and pass it as a marker option to a subsequent list blobs
     * request to get the next set of blob results.
     * <p>
     * Blobs are listed in alphabetical order in the response body, with
     * upper-case letters listed first.
     * 
     * @return A {@link List} of {@link BlobEntry} instances for the blobs that
     *         satisfied the request.
     */
    public List<BlobPrefixEntry> getBlobPrefixes() {
        return this.blobPrefixes;
    }

    /**
     * Gets the list of blobs that satisfied the request from the response. This
     * list may contain only a portion of the blobs that satisfy the request,
     * limited by a server timeout or a maximum results parameter. If there are
     * more blobs or blob prefixes that could satisfy the result, the server
     * returns a <strong>NextMarker</strong> element with the response.
     * <p>
     * Use the {@link ListBlobsResult#getNextMarker() getNextMarker} method to
     * get this value and pass it as a marker option to a subsequent list blobs
     * request to get the next set of blob results.
     * <p>
     * Blobs are listed in alphabetical order in the response body, with
     * upper-case letters listed first.
     * 
     * @return A {@link List} of {@link BlobEntry} instances for the blobs that
     *         satisfied the request.
     */
    public List<BlobEntry> getBlobs() {
        return this.blobs;
    }

    /**
     * Gets the value of the filter used to return only blobs beginning with the
     * prefix. This value is not set if a prefix was not specified in the list
     * blobs request.
     * 
     * @return A {@link String} containing the prefix used to filter the blob
     *         names returned, if any.
     */
    @XmlElement(name = "Prefix")
    public String getPrefix() {
        return prefix;
    }

    /**
     * Reserved for internal use. Sets the filter used to return only blobs
     * beginning with the prefix from the <strong>Prefix</strong> element
     * returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param prefix
     *            A {@link String} containing the prefix used to filter the blob
     *            names returned, if any.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the value of the marker that was used to specify the beginning of
     * the container list to return with the request. This value is not set if a
     * marker was not specified in the request.
     * <p>
     * The list blobs operation returns a marker value in a
     * <strong>NextMarker</strong> element if the blob list returned is not
     * complete. The marker value may then be used in a subsequent call to
     * request the next set of blob list items. The marker value is opaque to
     * the client.
     * <p>
     * Use the {@link ListBlobsResult#getNextMarker() getNextMarker} method to
     * get the marker value to set on a {@link ListBlobsOptions} instance using
     * a call to {@link ListBlobsOptions#setMarker(String)}. Pass the
     * {@link ListBlobsOptions} instance as a parameter to a
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobs(String, ListBlobsOptions) listBlobs} call
     * to get the next portion of the blob list.
     * 
     * @return A {@link String} containing the marker used to specify the
     *         beginning of the blob list returned, if any.
     */
    @XmlElement(name = "Marker")
    public String getMarker() {
        return marker;
    }

    /**
     * Reserved for internal use. Sets the marker used to specify the beginning
     * of the blob list to return from the <strong>Marker</strong> element
     * returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param marker
     *            A {@link String} containing the marker used to specify the
     *            beginning of the blob list returned.
     */
    public void setMarker(String marker) {
        this.marker = marker;
    }

    /**
     * Gets the next marker value needed to specify the beginning of the next
     * portion of the blob list to return, if any was set in the response from
     * the server.
     * <p>
     * The list blobs operation returns a marker value in a
     * <strong>NextMarker</strong> element if the blob list returned is not
     * complete. The marker value may then be used in a subsequent call to
     * request the next set of blob list items. The marker value is opaque to
     * the client.
     * <p>
     * Use the {@link ListBlobsResult#getNextMarker() getNextMarker} method to
     * get the marker value to set on a {@link ListBlobsOptions} instance using
     * a call to {@link ListBlobsOptions#setMarker(String)}. Pass the
     * {@link ListBlobsOptions} instance as a parameter to a
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobs(String, ListBlobsOptions) listBlobs} call
     * to get the next portion of the blob list.
     * 
     * @return A {@link String} containing the next marker value needed to
     *         specify the beginning of the next portion of the blob list to
     *         return, if any was set in the response from the server.
     */
    @XmlElement(name = "NextMarker")
    public String getNextMarker() {
        return nextMarker;
    }

    /**
     * Reserved for internal use. Sets the next marker value needed to specify
     * the beginning of the next portion of the blob list to return from the
     * <strong>NextMarker</strong> element returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param nextMarker
     *            A {@link String} containing the next marker value needed to
     *            specify the beginning of the next portion of the blob list to
     *            return.
     */
    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }

    /**
     * Gets the maximum results to return used to generate the response, if
     * present. The number of entries returned in the response will not exceed
     * this number, including all <strong>BlobPrefix</strong> elements. This
     * value is not set if a maximum number was not specified in the request. If
     * the request does not specify this parameter or specifies a value greater
     * than 5,000, the server will return up to 5,000 items. If there are more
     * blobs or blob prefixes that satisfy the request than this maximum value,
     * the server will include a next marker value in the response, which can be
     * used to get the remaining blobs with a subsequent request.
     * <p>
     * Use the {@link ListBlobsResult#getNextMarker() getNextMarker} method to
     * get the marker value to set on a {@link ListBlobsOptions} instance using
     * a call to {@link ListBlobsOptions#setMarker(String)}. Pass the
     * {@link ListBlobsOptions} instance as a parameter to a
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobs(String, ListBlobsOptions) listBlobs} call
     * to get the next portion of the blob list.
     * 
     * @return The maximum results to return value in the response, if any.
     */
    @XmlElement(name = "MaxResults")
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Reserved for internal use. Sets the maximum results to return value from
     * the <strong>MaxResults</strong> element of the
     * <strong>EnumerationResults</strong> element returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param maxResults
     *            The maximum results to return value in the response, if any.
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Gets the delimiter value used to generate the response, if present. When
     * the request includes this parameter, the operation returns
     * <strong>BlobPrefix</strong> elements in the response body that act as a
     * placeholder for all blobs whose names begin with the same substring up to
     * the appearance of the delimiter character.
     * 
     * @return A {@link String} containing the delimiter value used as a request
     *         parameter.
     */
    @XmlElement(name = "Delimiter")
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Reserved for internal use. Sets the delimiter value from the
     * <strong>Delimiter</strong> element of the
     * <strong>EnumerationResults</strong> element returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param delimiter
     *            A {@link String} containing the delimiter value in the
     *            response, if any.
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Gets the container URI. This value is returned in the
     * <strong>ContainerName</strong> attribute of the
     * <strong>EnumerationResults</strong> element returned in the response.
     * 
     * @return A {@link String} containing the container URI.
     */
    @XmlAttribute(name = "ContainerName")
    public String getContainerName() {
        return containerName;
    }

    /**
     * Reserved for internal use. Sets the container URI value from the
     * <strong>ContainerName</strong> attribute of the
     * <strong>EnumerationResults</strong> element returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param containerName
     *            A {@link String} containing the container URI.
     */
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * The abstract base class for <code>Blob</code> and <code>BlobPrefix</code>
     * entries in the list of results returned in the response.
     */
    public abstract static class ListBlobsEntry {

    }

    /**
     * Represents a <strong>BlobPrefix</strong> element returned in the
     * response. When the request includes a delimiter parameter, a single
     * <strong>BlobPrefix</strong> element is returned in place of all blobs
     * whose names begin with the same substring up to the appearance of the
     * delimiter character.
     */
    @XmlRootElement(name = "BlobPrefix")
    public static class BlobPrefixEntry extends ListBlobsEntry {
        private String name;

        /**
         * Gets the value of the <strong>Name</strong> element within a
         * <strong>BlobPrefix</strong> element returned in the response. The
         * value of the element is <em>substring</em>+<em>delimiter</em>, where
         * <em>substring</em> is the common substring that begins one or more
         * blob names, and <em>delimiter</em> is the value of the delimiter
         * parameter.
         * 
         * @return A {@link String} containing the common substring that begins
         *         one or more blob names up to and including the delimiter
         *         specified in the request.
         */
        @XmlElement(name = "Name")
        public String getName() {
            return name;
        }

        /**
         * Reserved for internal use. Sets the blob prefix name from the
         * <strong>Name</strong> element in the <strong>BlobPrefix</strong>
         * element in the <strong>Blobs</strong> list in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param name
         *            A {@link String} containing the value of the
         *            <strong>Name</strong> element within a
         *            <strong>BlobPrefix</strong> element.
         */
        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Represents a <strong>Blob</strong> element returned in the response. A
     * <code>BlobEntry</code> includes committed blobs, and can optionally
     * include blob metadata, blob snapshots, and uncommitted blobs, depending
     * on the request options set.
     */
    @XmlRootElement(name = "Blob")
    public static class BlobEntry extends ListBlobsEntry {
        private String name;
        private String url;
        private String snapshot;
        private HashMap<String, String> metadata = new HashMap<String, String>();
        private BlobProperties properties;

        /**
         * Gets the value of the <strong>Name</strong> element within a
         * <strong>Blob</strong> element returned in the response. The value of
         * the element is the complete name of the blob, including any virtual
         * hierarchy.
         * 
         * @return A {@link String} containing the complete blob name.
         */
        @XmlElement(name = "Name")
        public String getName() {
            return name;
        }

        /**
         * Reserved for internal use. Sets the blob name from the
         * <strong>Name</strong> element in the <strong>Blob</strong> element in
         * the <strong>Blobs</strong> list in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param name
         *            A {@link String} containing the value of the
         *            <strong>Name</strong> element within a
         *            <strong>Blob</strong> element.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the value of the <strong>Url</strong> element within a
         * <strong>Blob</strong> element returned in the response. The value of
         * the element is the complete URI address of the blob, including any
         * virtual hierarchy.
         * 
         * @return A {@link String} containing the complete URI address of the
         *         blob.
         */
        @XmlElement(name = "Url")
        public String getUrl() {
            return url;
        }

        /**
         * Reserved for internal use. Sets the blob URI address from the
         * <strong>Uri</strong> element in the <strong>Blob</strong> element in
         * the <strong>Blobs</strong> list in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param url
         *            A {@link String} containing the complete URI address of
         *            the blob.
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Gets a {@link BlobProperties} instance with the blob properties
         * returned in the response.
         * 
         * @return A {@link BlobProperties} instance with the blob properties
         *         returned in the response.
         */
        @XmlElement(name = "Properties")
        public BlobProperties getProperties() {
            return properties;
        }

        /**
         * Reserved for internal use. Sets the blob properties from the
         * <strong>Uri</strong> element in the <strong>Blob</strong> element in
         * the <strong>Blobs</strong> list in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param properties
         *            A {@link BlobProperties} instance with the blob properties
         *            returned in the response.
         */
        public void setProperties(BlobProperties properties) {
            this.properties = properties;
        }

        /**
         * Gets the snapshot timestamp value for a blob snapshot returned in the
         * response. The value is set from the <strong>Snapshot</strong> element
         * in the <strong>Blob</strong> element in the <strong>Blobs</strong>
         * list in the response. Snapshots are included in the enumeration only
         * if specified in the request options. Snapshots are listed from oldest
         * to newest in the response.
         * 
         * @return A {@link String} containing the snapshot timestamp of the
         *         blob snapshot.
         */
        @XmlElement(name = "Snapshot")
        public String getSnapshot() {
            return snapshot;
        }

        /**
         * Reserved for internal use. Sets the blob snapshot timestamp from the
         * <strong>Snapshot</strong> element in the <strong>Blob</strong>
         * element in the <strong>Blobs</strong> list in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param snapshot
         *            A {@link String} containing the snapshot timestamp of the
         *            blob snapshot.
         */
        public void setSnapshot(String snapshot) {
            this.snapshot = snapshot;
        }

        /**
         * Gets the blob's metadata collection set in the
         * <strong>Metadata</strong> element in the <strong>Blob</strong>
         * element in the <strong>Blobs</strong> list in the response. Metadata
         * is included in the enumeration only if specified in the request
         * options.
         * 
         * @return A {@link HashMap} of name-value pairs of {@link String}
         *         containing the blob metadata set, if any.
         */
        @XmlElement(name = "Metadata")
        @XmlJavaTypeAdapter(MetadataAdapter.class)
        public HashMap<String, String> getMetadata() {
            return metadata;
        }

        /**
         * Reserved for internal use. Sets the blob metadata from the
         * <strong>Metadata</strong> element in the <strong>Blob</strong>
         * element in the <strong>Blobs</strong> list in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param metadata
         *            A {@link java.util.HashMap} of name-value pairs of
         *            {@link String} containing the names and values of the blob
         *            metadata, if present.
         */
        public void setMetadata(HashMap<String, String> metadata) {
            this.metadata = metadata;
        }
    }
}
