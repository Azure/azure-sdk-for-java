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
package com.microsoft.windowsazure.services.queue.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.services.blob.implementation.MetadataAdapter;

/**
 * A wrapper class for the results returned in response to Queue service REST
 * API operations to list queues. This is returned by calls to implementations
 * of {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues()} and
 * {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions)}.
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179466.aspx"
 * >List Queues</a> documentation on MSDN for details of the underlying Queue
 * service REST API operation.
 */
@XmlRootElement(name = "EnumerationResults")
public class ListQueuesResult {
    private List<Queue> queues = new ArrayList<Queue>();
    private String accountName;
    private String prefix;
    private String marker;
    private String nextMarker;
    private int maxResults;

    /**
     * Gets the list of queues returned by a {@link com.microsoft.windowsazure.services.queue.QueueContract}
     * <em>.listQueues</em> request.
     * 
     * @return A {@link List} of {@link Queue} instances representing the queues
     *         returned by the request.
     */
    @XmlElementWrapper(name = "Queues")
    @XmlElement(name = "Queue")
    public List<Queue> getQueues() {
        return queues;
    }

    /**
     * Reserved for internal use. Sets the list of queues returned by a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract}<em>.listQueues</em> request. This method is invoked
     * by the API as part of the response generation from the Queue service REST
     * API operation to set the value from the queue list returned by the
     * server.
     * 
     * @param value
     *            A {@link List} of {@link Queue} instances representing the
     *            queues returned by the request.
     */
    public void setQueues(List<Queue> value) {
        this.queues = value;
    }

    /**
     * Gets the base URI for Queue service REST API operations on the storage
     * account. The URI consists of the protocol along with the DNS prefix name
     * for the account followed by ".queue.core.windows.net". For example, if
     * the DNS prefix name for the storage account is "myaccount" then the value
     * returned by this method is "http://myaccount.queue.core.windows.net".
     * 
     * @return A {@link String} containing the base URI for Queue service REST
     *         API operations on the storage account.
     */
    @XmlAttribute(name = "AccountName")
    public String getAccountName() {
        return accountName;
    }

    /**
     * Reserved for internal use. Sets the base URI for Queue service REST API
     * operations on the storage account. This method is invoked by the API as
     * part of the response generation from the Queue service REST API operation
     * to set the value from the response returned by the server.
     * 
     * @param accountName
     *            A {@link String} containing the base URI for Queue service
     *            REST API operations on the storage account.
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * Gets the prefix {@link String} used to qualify the results returned by
     * the {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues}
     * request. Only queues with names that start with the prefix are returned
     * by the request. By default, the prefix is empty and all queues are
     * returned.
     * 
     * @return The {@link String} prefix used to qualify the names of the queues
     *         returned.
     */
    @XmlElement(name = "Prefix")
    public String getPrefix() {
        return prefix;
    }

    /**
     * Reserved for internal use. Sets the prefix {@link String} used to qualify
     * the results returned by the Queue service REST API list queues operation
     * invoked with a call to
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues}. This
     * method is invoked by the API as part of the response generation from the
     * Queue service REST API operation to set the value from the
     * <strong>Prefix</strong> element returned by the server.
     * 
     * @param prefix
     *            The {@link String} prefix used to qualify the names of the
     *            queues returned.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the marker value for the beginning of the queue results returned by
     * the {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues}
     * request. The marker is used by the server to specify the place to resume
     * a query for queues. The marker value is a {@link String} opaque to the
     * client. A {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues}
     * request response may include a <strong>NextMarker</strong> value if there
     * are more queue results than can be returned in a single response. Call
     * the {@link ListQueuesResult#getNextMarker() getNextMarker} method to get
     * this value. The client can request the next set of queue results by
     * setting the marker to this value in the {@link ListQueuesOptions}
     * parameter. By default, this value is empty and the server responds with
     * the first queues that match the request.
     * 
     * @return A {@link String} containing the marker value used for the
     *         response.
     */
    @XmlElement(name = "Marker")
    public String getMarker() {
        return marker;
    }

    /**
     * Reserved for internal use. Sets the marker value specifying the beginning
     * of the results returned by the Queue service REST API list queues
     * operation invoked with a call to
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues}. This
     * method is invoked by the API as part of the response generation from the
     * Queue service REST API operation to set the value from the
     * <strong>Marker</strong> element returned by the server.
     * 
     * @param marker
     *            A {@link String} containing the marker value used for the
     *            response.
     */
    public void setMarker(String marker) {
        this.marker = marker;
    }

    /**
     * Gets the next marker value needed to retrieve additional queues. If more
     * queues are available that satisfy a <em>listQueues</em> request than can
     * be returned in the response, the server generates a marker value to
     * specify the beginning of the queues to return in a subsequent request.
     * The client can request the next set of queue results by setting the
     * marker to this value in the {@link ListQueuesOptions} parameter. This
     * value is empty if there are no more queues that satisfy the request than
     * are included in the response.
     * 
     * @return A {@link String} containing the marker value to use to resume the
     *         list queues request.
     */
    @XmlElement(name = "NextMarker")
    public String getNextMarker() {
        return nextMarker;
    }

    /**
     * Reserved for internal use. Sets the next marker value specifying the
     * place to resume a list queues query if more results are available than
     * have been returned by the Queue service REST API list queues operation
     * response. This method is invoked by the API as part of the response
     * generation from the Queue service REST API operation to set the value
     * from the <strong>NextMarker</strong> element returned by the server.
     * 
     * @param nextMarker
     *            A {@link String} containing the marker value to use to resume
     *            the list queues request.
     */
    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }

    /**
     * Gets the value specified for the number of queue results to return for
     * the {@link com.microsoft.windowsazure.services.queue.QueueContract#listQueues(ListQueuesOptions) listQueues}
     * request. The server will not return more than this number of queues in
     * the response. If the value is not specified, the server will return up to
     * 5,000 items.
     * <p>
     * If there are more queues available that match the request than the number
     * returned, the response will include a next marker value to specify the
     * beginning of the queues to return in a subsequent request. Call the
     * {@link ListQueuesResult#getNextMarker() getNextMarker} method to get this
     * value. The client can request the next set of queue results by setting
     * the marker to this value in the {@link ListQueuesOptions} parameter.
     * 
     * @return The maximum number of results to return specified by the request.
     */
    @XmlElement(name = "MaxResults")
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Reserved for internal use. Sets the value returned by the Queue service
     * REST API list queues operation response for the maximum number of queues
     * to return. This method is invoked by the API as part of the response
     * generation from the Queue service REST API operation to set the value
     * from the <strong>MaxResults</strong> element returned by the server.
     * 
     * @param maxResults
     *            The maximum number of results to return specified by the
     *            request.
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Represents a queue in the storage account returned by the server. A
     * {@link Queue} instance contains a copy of the queue name, URI, and
     * metadata in the storage service as of the time the queue was requested.
     */
    public static class Queue {
        private String name;
        private String url;
        private HashMap<String, String> metadata = new HashMap<String, String>();

        /**
         * Gets the name of this queue.
         * 
         * @return A {@link String} containing the name of this queue.
         */
        @XmlElement(name = "Name")
        public String getName() {
            return name;
        }

        /**
         * Reserved for internal use. Sets the name of this queue. This method
         * is invoked by the API as part of the response generation from the
         * Queue service REST API operation to set the value from the
         * <strong>Name</strong> element returned by the server.
         * 
         * @param name
         *            A {@link String} containing the name of this queue.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the URI for Queue service REST API operations on this queue.
         * 
         * @return A {@link String} containing the URI for Queue service REST
         *         API operations on this queue.
         */
        @XmlElement(name = "Url")
        public String getUrl() {
            return url;
        }

        /**
         * Reserved for internal use. Sets the URI of this queue. This method is
         * invoked by the API as part of the response generation from the Queue
         * service REST API operation to set the value from the
         * <strong>Url</strong> element returned by the server.
         * 
         * @param url
         *            A {@link String} containing the URI for Queue service REST
         *            API operations on this queue.
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Gets the metadata collection of key-value {@link String} pairs
         * associated with this queue.
         * 
         * @return A {@link java.util.HashMap} of key-value {@link String} pairs
         *         containing the queue metadata.
         */
        @XmlElement(name = "Metadata")
        @XmlJavaTypeAdapter(MetadataAdapter.class)
        public HashMap<String, String> getMetadata() {
            return metadata;
        }

        /**
         * Reserved for internal use. Sets the metadata of this queue. This
         * method is invoked by the API as part of the response generation from
         * the Queue service REST API operation to set the value from the
         * <strong>Metadata</strong> element returned by the server.
         * 
         * @param metadata
         *            A {@link java.util.HashMap} of key-value {@link String}
         *            pairs containing the queue metadata.
         */
        public void setMetadata(HashMap<String, String> metadata) {
            this.metadata = metadata;
        }
    }
}
