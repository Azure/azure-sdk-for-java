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
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobs(String, ListBlobsOptions)} request. These
 * options include a server response timeout for the request, a prefix for blobs
 * to match, a marker to continue a list operation, a maximum number of results
 * to return with one list operation, a delimiter for structuring virtual blob
 * hierarchies, and whether to include blob metadata, blob snapshots, and
 * uncommitted blobs in the results.
 */
public class ListBlobsOptions extends BlobServiceOptions {
    private String prefix;
    private String marker;
    private int maxResults;
    private String delimiter;
    private boolean includeMetadata;
    private boolean includeSnapshots;
    private boolean includeUncommittedBlobs;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link ListBlobsOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link ListBlobsOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link ListBlobsOptions} instance.
     */
    @Override
    public ListBlobsOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the prefix filter associated with this {@link ListBlobsOptions}
     * instance. This value is used to return only blobs beginning with the
     * prefix from the container in methods where this {@link ListBlobsOptions}
     * instance is passed as a parameter.
     * 
     * @return A {@link String} containing the prefix used to filter the blob
     *         names returned, if any.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the optional blob name prefix filter value to use in a request. If
     * this value is set, the server will return only blob names that match the
     * prefix value in the response.
     * <p>
     * The <em>prefix</em> value only affects calls made on methods where this
     * {@link ListBlobsOptions} instance is passed as a parameter.
     * 
     * @param prefix
     *            A {@link String} containing a prefix to use to filter the blob
     *            names returned.
     */
    public ListBlobsOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Gets the marker value set in this {@link ListBlobsOptions} instance. If
     * this value is set, the server will return blob names beginning at the
     * specified marker in the response.
     * 
     * @return A {@link String} containing the marker value to use to specify
     *         the beginning of the request results, if any.
     */
    public String getMarker() {
        return marker;
    }

    /**
     * Sets the optional marker value to use in a request. If this value is set,
     * the server will return blob names beginning at the specified marker in
     * the response. Leave this value unset for an initial request.
     * <p>
     * The List Blobs operation returns a marker value in a
     * <strong>NextMarker</strong> element if the blob list returned is not
     * complete. The marker value may then be used in a subsequent call to
     * request the next set of blob list items. The marker value is opaque to
     * the client.
     * <p>
     * Use the {@link ListBlobsResult#getNextMarker() getNextMarker} method on a
     * {@link ListBlobsResult} instance to get the marker value to set on a
     * {@link ListBlobsOptions} instance using a call to this method. Pass the
     * {@link ListBlobsOptions} instance as a parameter to a
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobs(String, ListBlobsOptions)} call to get the
     * next portion of the blob list.
     * <p>
     * The <em>marker</em> value only affects calls made on methods where this
     * {@link ListBlobsOptions} instance is passed as a parameter.
     * 
     * @param marker
     *            A {@link String} containing the marker value to use to specify
     *            the beginning of the request results.
     * @return A reference to this {@link ListBlobsOptions} instance.
     */
    public ListBlobsOptions setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    /**
     * Gets the value of the maximum number of results to return set in this
     * {@link ListBlobsOptions} instance.
     * 
     * @return The maximum number of results to return. If the value is zero,
     *         the server will return up to 5,000 items.
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Sets the optional maximum number of results to return for a request. If
     * this value is set, the server will return up to this number of blob and
     * blob prefix results in the response. If a value is not specified, or a
     * value greater than 5,000 is specified, the server will return up to 5,000
     * items.
     * <p>
     * If there are more blobs and blob prefixes that can be returned than this
     * maximum, the server returns a marker value in a
     * <strong>NextMarker</strong> element in the response. The marker value may
     * then be used in a subsequent call to request the next set of blob list
     * items. The marker value is opaque to the client.
     * <p>
     * Use the {@link ListBlobsResult#getNextMarker() getNextMarker} method on a
     * {@link ListBlobsResult} instance to get the marker value to set on a
     * {@link ListBlobsOptions} instance using a call to
     * {@link ListBlobsOptions#setMarker(String)}. Pass the
     * {@link ListBlobsOptions} instance as a parameter to a
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobs(String, ListBlobsOptions)} call to get the
     * next portion of the blob list.
     * <p>
     * The <em>maxResults</em> value only affects calls made on methods where
     * this {@link ListBlobsOptions} instance is passed as a parameter.
     * 
     * @param maxResults
     *            The maximum number of results to return.
     * @return A reference to this {@link ListBlobsOptions} instance.
     */
    public ListBlobsOptions setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Gets the value of the delimiter to use for grouping virtual blob
     * hierarchy set in this {@link ListBlobsOptions} instance.
     * 
     * @return A {@link String} containing the delimiter value, if any.
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the value of the optional delimiter to use for grouping in a virtual
     * blob hierarchy.
     * <p>
     * When the request includes this optional parameter, the operation returns
     * a blob prefix in a <strong>BlobPrefix</strong> element in the response
     * that acts as a placeholder for all blobs whose names begin with the same
     * substring up to the appearance of the delimiter character. The delimiter
     * may be a single character or a string.
     * <p>
     * The <em>delimiter</em> parameter enables the caller to traverse the blob
     * namespace by using a user-configured delimiter. In this way, you can
     * traverse a virtual hierarchy of blobs as though it were a file system.
     * The delimiter is a string that may be one or more characters long. When
     * the request includes this parameter, the operation response includes one
     * <strong>BlobPrefix</strong> element for each set of blobs whose names
     * begin with a common substring up to the first appearance of the delimiter
     * string that comes after any prefix specified for the request. The value
     * of the <strong>BlobPrefix</strong> element is
     * <code>substring+delimiter</code>, where <code>substring</code> is the
     * common substring that begins one or more blob names, and
     * <code>delimiter</code> is the value of the delimiter parameter.
     * <p>
     * The <strong>BlobPrefix</strong> elements in the response are accessed
     * with the {@link ListBlobsResult#getBlobPrefixes()} method. You can use
     * each value in the list returned to make a list blobs request for the
     * blobs that begin with that blob prefix value, by specifying the value as
     * the prefix option with a call to the
     * {@link ListBlobsOptions#setPrefix(String) setPrefix} method on a
     * {@link ListBlobsOptions} instance passed as a parameter to the request.
     * <p>
     * Note that each blob prefix returned in the response counts toward the
     * maximum number of blob results, just as each blob does.
     * <p>
     * Note that if a delimiter is set, you cannot include snapshots. A request
     * that includes both returns an InvalidQueryParameter error (HTTP status
     * code 400 - Bad Request), which causes a {@link com.microsoft.windowsazure.exception.ServiceException} to be
     * thrown.
     * <p>
     * The <em>delimiter</em> value only affects calls made on methods where
     * this {@link ListBlobsOptions} instance is passed as a parameter.
     * 
     * @param delimiter
     *            A {@link String} containing the delimiter value to use for
     *            grouping virtual blob hierarchy.
     * @return A reference to this {@link ListBlobsOptions} instance.
     */
    public ListBlobsOptions setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * Gets the value of a flag indicating whether to include blob metadata with
     * a response set in this {@link ListBlobsOptions} instance.
     * 
     * @return A value of <code>true</code> to include blob metadata with a
     *         response, otherwise, <code>false</code>.
     */
    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    /**
     * Sets the value of an optional flag indicating whether to include blob
     * metadata with a response.
     * 
     * @param includeMetadata
     *            Set a value of <code>true</code> to include blob metadata with
     *            a response, otherwise, <code>false</code>.
     * @return A reference to this {@link ListBlobsOptions} instance.
     */
    public ListBlobsOptions setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }

    /**
     * Gets the value of a flag indicating whether to include blob snapshots
     * with a response set in this {@link ListBlobsOptions} instance.
     * 
     * @return A value of <code>true</code> to include blob metadata with a
     *         response, otherwise, <code>false</code>.
     */
    public boolean isIncludeSnapshots() {
        return includeSnapshots;
    }

    /**
     * Sets the value of an optional flag indicating whether to include blob
     * snapshots with a response.
     * <p>
     * Note that if this flag is set, you cannot set a delimiter. A request that
     * includes both returns an InvalidQueryParameter error (HTTP status code
     * 400 - Bad Request), which causes a {@link com.microsoft.windowsazure.exception.ServiceException} to be thrown.
     * 
     * @param includeSnapshots
     *            Set a value of <code>true</code> to include blob metadata with
     *            a response, otherwise, <code>false</code>.
     * @return A reference to this {@link ListBlobsOptions} instance.
     */
    public ListBlobsOptions setIncludeSnapshots(boolean includeSnapshots) {
        this.includeSnapshots = includeSnapshots;
        return this;
    }

    /**
     * Gets the value of a flag indicating whether to include uncommitted blobs
     * with a response set in this {@link ListBlobsOptions} instance.
     * 
     * @return A value of <code>true</code> to include uncommitted blobs with a
     *         response, otherwise, <code>false</code>.
     */
    public boolean isIncludeUncommittedBlobs() {
        return includeUncommittedBlobs;
    }

    /**
     * Sets the value of an optional flag indicating whether to include
     * uncommitted blobs with a response. Uncommitted blobs are blobs for which
     * blocks have been uploaded, but which have not been committed with a
     * request to
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#commitBlobBlocks(String, String, BlockList)} or
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#commitBlobBlocks(String, String, BlockList, CommitBlobBlocksOptions)}
     * .
     * 
     * @param includeUncommittedBlobs
     *            Set a value of <code>true</code> to include uncommitted blobs
     *            with a response, otherwise, <code>false</code>.
     * @return A reference to this {@link ListBlobsOptions} instance.
     */
    public ListBlobsOptions setIncludeUncommittedBlobs(
            boolean includeUncommittedBlobs) {
        this.includeUncommittedBlobs = includeUncommittedBlobs;
        return this;
    }
}
