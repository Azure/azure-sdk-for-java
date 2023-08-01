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

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.core.RFC1123DateAdapter;
import com.microsoft.windowsazure.services.blob.implementation.MetadataAdapter;

/**
 * A wrapper class for the response returned from a Blob Service REST API List
 * Containers operation. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#listContainers} and
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#listContainers(ListContainersOptions)}.
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179352.aspx"
 * >List Containers</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operation.
 */
@XmlRootElement(name = "EnumerationResults")
public class ListContainersResult {
    private List<Container> containers;
    private String accountName;
    private String prefix;
    private String marker;
    private String nextMarker;
    private int maxResults;

    /**
     * Gets the container list returned in the response.
     * 
     * @return A {@link List} of {@link Container} instances representing the
     *         blob containers returned by the request.
     */
    @XmlElementWrapper(name = "Containers")
    @XmlElement(name = "Container")
    public List<Container> getContainers() {
        return containers;
    }

    /**
     * Reserved for internal use. Sets the container list from the
     * <strong>Containers</strong> element returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param value
     *            A {@link List} of {@link Container} instances representing the
     *            blob containers returned by the request.
     */
    public void setContainers(List<Container> value) {
        this.containers = value;
    }

    /**
     * Gets the base URI for invoking Blob Service REST API operations on the
     * storage account.
     * 
     * @return A {@link String} containing the base URI for Blob Service REST
     *         API operations on the storage account.
     */
    @XmlAttribute(name = "AccountName")
    public String getAccountName() {
        return accountName;
    }

    /**
     * Reserved for internal use. Sets the base URI for invoking Blob Service
     * REST API operations on the storage account from the value of the
     * <strong>AccountName</strong> attribute of the
     * <strong>EnumerationResults</strong> element returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param accountName
     *            A {@link String} containing the base URI for Blob Service REST
     *            API operations on the storage account.
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * Gets the value of the filter used to return only containers beginning
     * with the prefix. This value is not set if a prefix was not specified in
     * the list containers request.
     * 
     * @return A {@link String} containing the prefix used to filter the
     *         container names returned, if any.
     */
    @XmlElement(name = "Prefix")
    public String getPrefix() {
        return prefix;
    }

    /**
     * Reserved for internal use. Sets the filter used to return only containers
     * beginning with the prefix from the <strong>Prefix</strong> element
     * returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param prefix
     *            A {@link String} containing the prefix used to filter the
     *            container names returned, if any.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the value of the marker that was used to specify the beginning of
     * the container list to return with the request. This value is not set if a
     * marker was not specified in the request.
     * <p>
     * The List Containers operation returns a marker value in a
     * <strong>NextMarker</strong> element if the container list returned is not
     * complete. The marker value may then be used in a subsequent call to
     * request the next set of container list items. The marker value is opaque
     * to the client.
     * <p>
     * Use the {@link ListContainersResult#getNextMarker() getNextMarker} method
     * to get the marker value to set on a {@link ListContainersOptions}
     * instance using a call to {@link ListContainersOptions#setMarker(String)}.
     * Pass the {@link ListContainersOptions} instance as a parameter to a
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#listContainers(ListContainersOptions)} call to get
     * the next portion of the container list.
     * 
     * @return A {@link String} containing the marker used to specify the
     *         beginning of the container list returned, if any.
     */
    @XmlElement(name = "Marker")
    public String getMarker() {
        return marker;
    }

    /**
     * Reserved for internal use. Sets the marker used to specify the beginning
     * of the container list to return from the <strong>Marker</strong> element
     * returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param marker
     *            A {@link String} containing the marker used to specify the
     *            beginning of the container list returned.
     */
    public void setMarker(String marker) {
        this.marker = marker;
    }

    /**
     * Gets the next marker value needed to specify the beginning of the next
     * portion of the container list to return, if any was set in the response
     * from the server.
     * <p>
     * The List Containers operation returns a marker value in a
     * <strong>NextMarker</strong> element if the container list returned is not
     * complete. The marker value may then be used in a subsequent call to
     * request the next set of container list items. The marker value is opaque
     * to the client.
     * <p>
     * Use the {@link ListContainersResult#getNextMarker() getNextMarker} method
     * to get the marker value to set on a {@link ListContainersOptions}
     * instance using a call to {@link ListContainersOptions#setMarker(String)}.
     * Pass the {@link ListContainersOptions} instance as a parameter to a
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#listContainers(ListContainersOptions)} call to get
     * the next portion of the container list.
     * 
     * @return A {@link String} containing the next marker value needed to
     *         specify the beginning of the next portion of the container list
     *         to return, if any was set in the response from the server.
     */
    @XmlElement(name = "NextMarker")
    public String getNextMarker() {
        return nextMarker;
    }

    /**
     * Reserved for internal use. Sets the next marker value needed to specify
     * the beginning of the next portion of the container list to return from
     * the <strong>NextMarker</strong> element returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param nextMarker
     *            A {@link String} containing the next marker value needed to
     *            specify the beginning of the next portion of the container
     *            list to return.
     */
    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }

    /**
     * Gets the maximum number of container list items to return that was
     * specified in the request. The number of containers returned in a single
     * response will not exceed this value. This value is not set if a maximum
     * number was not specified in the request. If there are more containers
     * that satisfy the request than this maximum value, the server will include
     * a next marker value in the response, which can be used to get the
     * remaining containers with a subsequent request.
     * <p>
     * Use the {@link ListContainersResult#getNextMarker() getNextMarker} method
     * to get the marker value to set on a {@link ListContainersOptions}
     * instance using a call to {@link ListContainersOptions#setMarker(String)}.
     * Pass the {@link ListContainersOptions} instance as a parameter to a
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#listContainers(ListContainersOptions)} call to get
     * the next portion of the container list.
     * 
     * @return The maximum number of container list items to return that was
     *         specified in the request, if any.
     */
    @XmlElement(name = "MaxResults")
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Reserved for internal use. Sets the maximum number of container list
     * items to return that was specified in the request from the
     * <strong>MaxResults</strong> element returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param maxResults
     *            The maximum number of container list items to return that was
     *            specified in the request, if any.
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Represents a container for blob storage returned by the server. A
     * {@link Container} instance contains a copy of the container properties
     * and metadata in the storage service as of the time the container list was
     * requested.
     */
    public static class Container {
        private String name;
        private String url;
        private HashMap<String, String> metadata = new HashMap<String, String>();
        private ContainerProperties properties;

        /**
         * Gets the name of the container.
         * 
         * @return A {@link String} containing the name of the container.
         */
        @XmlElement(name = "Name")
        public String getName() {
            return name;
        }

        /**
         * Reserved for internal use. Sets the name of the container from the
         * <strong>Name</strong> element returned in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param name
         *            A {@link String} containing the name of the container.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the URI of the container.
         * 
         * @return A {@link String} containing the URI of the container.
         */
        @XmlElement(name = "Url")
        public String getUrl() {
            return url;
        }

        /**
         * Reserved for internal use. Sets the URI of the container from the
         * <strong>Url</strong> element returned in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param url
         *            A {@link String} containing the URI of the container.
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Gets the container properties. The container properties include the
         * last modified time and an ETag value.
         * 
         * @return A {@link ContainerProperties} instance containing the
         *         properties associated with the container.
         */
        @XmlElement(name = "Properties")
        public ContainerProperties getProperties() {
            return properties;
        }

        /**
         * Reserved for internal use. Sets the container properties from the
         * <strong>Properties</strong> element returned in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param properties
         *            A {@link ContainerProperties} instance containing the
         *            properties associated with the container.
         */
        public void setProperties(ContainerProperties properties) {
            this.properties = properties;
        }

        /**
         * Gets the container metadata as a map of name and value pairs. The
         * container metadata is for client use and is opaque to the server.
         * 
         * @return A {@link java.util.HashMap} of key-value pairs of
         *         {@link String} containing the names and values of the
         *         container metadata.
         */
        @XmlElement(name = "Metadata")
        @XmlJavaTypeAdapter(MetadataAdapter.class)
        public HashMap<String, String> getMetadata() {
            return metadata;
        }

        /**
         * Reserved for internal use. Sets the container metadata from the
         * <strong>Metadata</strong> element returned in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param metadata
         *            A {@link java.util.HashMap} of key-value pairs of
         *            {@link String} containing the names and values of the
         *            container metadata.
         */
        public void setMetadata(HashMap<String, String> metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * Represents the properties of a container for blob storage returned by the
     * server. A {@link ContainerProperties} instance contains a copy of the
     * container properties in the storage service as of the time the container
     * list was requested.
     */
    public static class ContainerProperties {
        private Date lastModified;
        private String etag;

        /**
         * Gets the last modifed time of the container. This value can be used
         * when updating or deleting a container using an optimistic concurrency
         * model to prevent the client from modifying data that has been changed
         * by another client.
         * 
         * @return A {@link java.util.Date} containing the last modified time of
         *         the container.
         */
        @XmlElement(name = "Last-Modified")
        @XmlJavaTypeAdapter(RFC1123DateAdapter.class)
        public Date getLastModified() {
            return lastModified;
        }

        /**
         * Reserved for internal use. Sets the last modified time of the
         * container from the <strong>Last-Modified</strong> element returned in
         * the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param lastModified
         *            A {@link java.util.Date} containing the last modified time
         *            of the container.
         */
        public void setLastModified(Date lastModified) {
            this.lastModified = lastModified;
        }

        /**
         * Gets the ETag of the container. This value can be used when updating
         * or deleting a container using an optimistic concurrency model to
         * prevent the client from modifying data that has been changed by
         * another client.
         * 
         * @return A {@link String} containing the server-assigned ETag value
         *         for the container.
         */
        @XmlElement(name = "Etag")
        public String getEtag() {
            return etag;
        }

        /**
         * Reserved for internal use. Sets the ETag of the container from the
         * <strong>ETag</strong> element returned in the response.
         * <p>
         * This method is invoked by the API to set the value from the Blob
         * Service REST API operation response returned by the server.
         * 
         * @param etag
         *            A {@link String} containing the server-assigned ETag value
         *            for the container.
         */
        public void setEtag(String etag) {
            this.etag = etag;
        }
    }
}
