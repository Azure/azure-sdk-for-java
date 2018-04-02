/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

/**
 * Specifies the options associated with feed methods (enumeration operations) in the Azure Cosmos DB database service.
 */
public final class FeedOptions extends FeedOptionsBase {
    private String sessionToken;
    private String partitionKeyRangeId;
    private Boolean enableScanInQuery;
    private Boolean emitVerboseTracesInQuery;
    private Boolean enableCrossPartitionQuery;
    private int maxDegreeOfParallelism;
    private int maxBufferedItemCount;

    public FeedOptions() {}

    public FeedOptions(FeedOptions options) {
        super(options);
        this.sessionToken = options.sessionToken;
        this.partitionKeyRangeId = options.partitionKeyRangeId;
        this.enableScanInQuery = options.enableScanInQuery;
        this.emitVerboseTracesInQuery = options.emitVerboseTracesInQuery;
        this.enableCrossPartitionQuery = options.enableCrossPartitionQuery;
        this.maxDegreeOfParallelism = options.maxDegreeOfParallelism;
        this.maxBufferedItemCount = options.maxBufferedItemCount;
    }

    /**
     * Gets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    public String getPartitionKeyRangeIdInternal() {
        return this.partitionKeyRangeId;
    }

    /**
     * Sets the partitionKeyRangeId.
     *
     * @param partitionKeyRangeId
     *           the partitionKeyRangeId.
     */
    public void setPartitionKeyRangeIdInternal(String partitionKeyRangeId) {
        this.partitionKeyRangeId = partitionKeyRangeId;
    }

    /**
     * Gets the session token for use with session consistency.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return this.sessionToken;
    }

    /**
     * Sets the session token for use with session consistency.
     *
     * @param sessionToken
     *            the session token.
     */
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    /**
     * Gets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @return the option of enable scan in query.
     */
    public Boolean getEnableScanInQuery() {
        return this.enableScanInQuery;
    }

    /**
     * Sets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @param enableScanInQuery
     *            the option of enable scan in query.
     */
    public void setEnableScanInQuery(Boolean enableScanInQuery) {
        this.enableScanInQuery = enableScanInQuery;
    }

    /**
     * Gets the option to allow queries to emit out verbose traces for
     * investigation.
     *
     * @return the emit verbose traces in query.
     */
    public Boolean getEmitVerboseTracesInQuery() {
        return this.emitVerboseTracesInQuery;
    }

    /**
     * Sets the option to allow queries to emit out verbose traces for
     * investigation.
     *
     * @param emitVerboseTracesInQuery
     *            the emit verbose traces in query.
     */
    public void setEmitVerboseTracesInQuery(Boolean emitVerboseTracesInQuery) {
        this.emitVerboseTracesInQuery = emitVerboseTracesInQuery;
    }

    /**
     * Gets the option to allow queries to run across all partitions of the
     * collection.
     *
     * @return whether to allow queries to run across all partitions of the
     *         collection.
     */
    public Boolean getEnableCrossPartitionQuery() {
        return this.enableCrossPartitionQuery;
    }

    /**
     * Sets the option to allow queries to run across all partitions of the
     * collection.
     *
     * @param enableCrossPartitionQuery
     *            whether to allow queries to run across all partitions of the
     *            collection.
     */
    public void setEnableCrossPartitionQuery(Boolean enableCrossPartitionQuery) {
        this.enableCrossPartitionQuery = enableCrossPartitionQuery;
    }

    /**
     * Gets the number of concurrent operations run client side during parallel
     * query execution.
     *
     * @return number of concurrent operations run client side during parallel
     *         query execution.
     */
    public int getMaxDegreeOfParallelism() {
        return maxDegreeOfParallelism;
    }

    /**
     * Sets the number of concurrent operations run client side during parallel
     * query execution.
     *
     * @param maxDegreeOfParallelism
     *            number of concurrent operations.
     */
    public void setMaxDegreeOfParallelism(int maxDegreeOfParallelism) {
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
    }

    /**
     * Gets the maximum number of items that can be buffered client side during
     * parallel query execution.
     *
     * @return maximum number of items that can be buffered client side during
     *         parallel query execution.
     */
    public int getMaxBufferedItemCount() {
        return maxBufferedItemCount;
    }

    /**
     * Sets the maximum number of items that can be buffered client side during
     * parallel query execution.
     *
     * @param maxBufferedItemCount
     *            maximum number of items.
     */
    public void setMaxBufferedItemCount(int maxBufferedItemCount) {
        this.maxBufferedItemCount = maxBufferedItemCount;
    }
}
