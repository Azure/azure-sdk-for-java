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


/**
 * Represents the options that may be set on a
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#listContainers(ListContainersOptions) listContainers}
 * request. These options include a server response timeout for the request, a
 * container name prefix filter, a marker for continuing requests, the maximum
 * number of results to return in a request, and whether to include container
 * metadata in the results. Options that are not set will not be passed to the
 * server with a request.
 */
public class ListContainersOptions extends BlobServiceOptions {
    private String prefix;
    private String marker;
    private int maxResults;
    private boolean includeMetadata;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link ListContainersOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link ListContainersOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link ListContainersOptions} instance.
     */
    @Override
    public ListContainersOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the container name prefix filter value set in this
     * {@link ListContainersOptions} instance.
     * 
     * @return A {@link String} containing the container name prefix value, if
     *         any.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the optional container name prefix filter value to use in a request.
     * If this value is set, the server will return only container names that
     * match the prefix value in the response.
     * <p>
     * The <em>prefix</em> value only affects calls made on methods where this
     * {@link ListContainersOptions} instance is passed as a parameter.
     * 
     * @param prefix
     *            A {@link String} containing the container name prefix value to
     *            use to filter the request results.
     * @return A reference to this {@link ListContainersOptions} instance.
     */
    public ListContainersOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Gets the marker value set in this {@link ListContainersOptions} instance.
     * 
     * @return A {@link String} containing the marker value to use to specify
     *         the beginning of the request results.
     */
    public String getMarker() {
        return marker;
    }

    /**
     * Sets the optional marker value to use in a request. If this value is set,
     * the server will return container names beginning at the specified marker
     * in the response.
     * <p>
     * The List Containers operation returns a marker value in a
     * <strong>NextMarker</strong> element if the container list returned is not
     * complete. The marker value may then be used in a subsequent call to
     * request the next set of container list items. The marker value is opaque
     * to the client.
     * <p>
     * Use the {@link ListContainersResult#getNextMarker() getNextMarker} method
     * on a {@link ListContainersResult} instance to get the marker value to set
     * on a {@link ListContainersOptions} instance using a call to this method.
     * Pass the {@link ListContainersOptions} instance as a parameter to a
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#listContainers(ListContainersOptions)} call to get
     * the next portion of the container list.
     * <p>
     * The <em>marker</em> value only affects calls made on methods where this
     * {@link ListContainersOptions} instance is passed as a parameter.
     * 
     * @param marker
     *            A {@link String} containing the marker value to use to specify
     *            the beginning of the request results.
     * @return A reference to this {@link ListContainersOptions} instance.
     */
    public ListContainersOptions setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    /**
     * Gets the value of the maximum number of results to return set in this
     * {@link ListContainersOptions} instance.
     * 
     * @return The maximum number of results to return.
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Sets the optional maximum number of results to return for a request. If
     * this value is set, the server will return up to this number of results in
     * the response. If a value is not specified, or a value greater than 5,000
     * is specified, the server will return up to 5,000 items.
     * <p>
     * If there are more containers than this value that can be returned, the
     * server returns a marker value in a <strong>NextMarker</strong> element in
     * the response. The marker value may then be used in a subsequent call to
     * request the next set of container list items. The marker value is opaque
     * to the client.
     * <p>
     * Use the {@link ListContainersResult#getNextMarker() getNextMarker} method
     * on a {@link ListContainersResult} instance to get the marker value to set
     * on a {@link ListContainersOptions} instance using a call to
     * {@link ListContainersOptions#setMarker(String)}. Pass the
     * {@link ListContainersOptions} instance as a parameter to a
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#listContainers(ListContainersOptions)} call to get
     * the next portion of the container list.
     * <p>
     * The <em>maxResults</em> value only affects calls made on methods where
     * this {@link ListContainersOptions} instance is passed as a parameter.
     * 
     * @param maxResults
     *            The maximum number of results to return.
     * @return A reference to this {@link ListContainersOptions} instance.
     */
    public ListContainersOptions setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Gets the value of a flag set in this {@link ListContainersOptions}
     * instance indicating whether to include container metadata in the response
     * to the request.
     * 
     * @return <code>true</code> to include container metadata in the response
     *         to the request.
     */
    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    /**
     * Sets the value of an optional flag indicating whether to include
     * container metadata with the response to the request.
     * <p>
     * The <em>includeMetadata</em> value only affects calls made on methods
     * where this {@link ListContainersOptions} instance is passed as a
     * parameter.
     * 
     * @param includeMetadata
     *            Set to <code>true</code> to include container metadata in the
     *            response to the request.
     * @return A reference to this {@link ListContainersOptions} instance.
     */
    public ListContainersOptions setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }
}
