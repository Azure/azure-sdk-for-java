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

package com.azure.data.cosmos;

/**
 * Specifies the options associated with feed methods (enumeration operations)
 * in the Azure Cosmos DB database service.
 */
public final class FeedOptions extends FeedOptionsBase {
    private String sessionToken;
    private String partitionKeyRangeId;
    private Boolean enableScanInQuery;
    private Boolean emitVerboseTracesInQuery;
    private Boolean enableCrossPartitionQuery;
    private int maxDegreeOfParallelism;
    private int maxBufferedItemCount;
    private int responseContinuationTokenLimitInKb;

    public FeedOptions() {
    }

    public FeedOptions(FeedOptions options) {
        super(options);
        this.sessionToken = options.sessionToken;
        this.partitionKeyRangeId = options.partitionKeyRangeId;
        this.enableScanInQuery = options.enableScanInQuery;
        this.emitVerboseTracesInQuery = options.emitVerboseTracesInQuery;
        this.enableCrossPartitionQuery = options.enableCrossPartitionQuery;
        this.maxDegreeOfParallelism = options.maxDegreeOfParallelism;
        this.maxBufferedItemCount = options.maxBufferedItemCount;
        this.responseContinuationTokenLimitInKb = options.responseContinuationTokenLimitInKb;
    }

    /**
     * Gets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    public String partitionKeyRangeIdInternal() {
        return this.partitionKeyRangeId;
    }

    // TODO: make private
    /**
     * Sets the partitionKeyRangeId.
     *
     * @param partitionKeyRangeId the partitionKeyRangeId.
     * @return the FeedOptions.
     */
    public FeedOptions partitionKeyRangeIdInternal(String partitionKeyRangeId) {
        this.partitionKeyRangeId = partitionKeyRangeId;
        return this;
    }

    /**
     * Gets the session token for use with session consistency.
     *
     * @return the session token.
     */
    public String sessionToken() {
        return this.sessionToken;
    }

    /**
     * Sets the session token for use with session consistency.
     *
     * @param sessionToken the session token.
     * @return the FeedOptions.
     */
    public FeedOptions sessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    /**
     * Gets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @return the option of enable scan in query.
     */
    public Boolean enableScanInQuery() {
        return this.enableScanInQuery;
    }

    /**
     * Sets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @param enableScanInQuery the option of enable scan in query.
     * @return the FeedOptions.
     */
    public FeedOptions enableScanInQuery(Boolean enableScanInQuery) {
        this.enableScanInQuery = enableScanInQuery;
        return this;
    }

    /**
     * Gets the option to allow queries to emit out verbose traces for
     * investigation.
     *
     * @return the emit verbose traces in query.
     */
    public Boolean emitVerboseTracesInQuery() {
        return this.emitVerboseTracesInQuery;
    }

    /**
     * Sets the option to allow queries to emit out verbose traces for
     * investigation.
     *
     * @param emitVerboseTracesInQuery the emit verbose traces in query.
     * @return the FeedOptions.
     */
    public FeedOptions emitVerboseTracesInQuery(Boolean emitVerboseTracesInQuery) {
        this.emitVerboseTracesInQuery = emitVerboseTracesInQuery;
        return this;
    }

    /**
     * Gets the option to allow queries to run across all partitions of the
     * collection.
     *
     * @return whether to allow queries to run across all partitions of the
     *         collection.
     */
    public Boolean enableCrossPartitionQuery() {
        return this.enableCrossPartitionQuery;
    }

    /**
     * Sets the option to allow queries to run across all partitions of the
     * collection.
     *
     * @param enableCrossPartitionQuery whether to allow queries to run across all
     *                                  partitions of the collection.
     * @return the FeedOptions.
     */
    public FeedOptions enableCrossPartitionQuery(Boolean enableCrossPartitionQuery) {
        this.enableCrossPartitionQuery = enableCrossPartitionQuery;
        return this;
    }

    /**
     * Gets the number of concurrent operations run client side during parallel
     * query execution.
     *
     * @return number of concurrent operations run client side during parallel query
     *         execution.
     */
    public int maxDegreeOfParallelism() {
        return maxDegreeOfParallelism;
    }

    /**
     * Sets the number of concurrent operations run client side during parallel
     * query execution.
     *
     * @param maxDegreeOfParallelism number of concurrent operations.
     * @return the FeedOptions.
     */
    public FeedOptions maxDegreeOfParallelism(int maxDegreeOfParallelism) {
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
        return this;
    }

    /**
     * Gets the maximum number of items that can be buffered client side during
     * parallel query execution.
     *
     * @return maximum number of items that can be buffered client side during
     *         parallel query execution.
     */
    public int maxBufferedItemCount() {
        return maxBufferedItemCount;
    }

    /**
     * Sets the maximum number of items that can be buffered client side during
     * parallel query execution.
     *
     * @param maxBufferedItemCount maximum number of items.
     * @return the FeedOptions.
     */
    public FeedOptions maxBufferedItemCount(int maxBufferedItemCount) {
        this.maxBufferedItemCount = maxBufferedItemCount;
        return this;
    }

    /**
     * Sets the ResponseContinuationTokenLimitInKb request option for document query
     * requests in the Azure Cosmos DB service.
     *
     * ResponseContinuationTokenLimitInKb is used to limit the length of
     * continuation token in the query response. Valid values are &gt;= 1.
     *
     * The continuation token contains both required and optional fields. The
     * required fields are necessary for resuming the execution from where it was
     * stooped. The optional fields may contain serialized index lookup work that
     * was done but not yet utilized. This avoids redoing the work again in
     * subsequent continuations and hence improve the query performance. Setting the
     * maximum continuation size to 1KB, the Azure Cosmos DB service will only
     * serialize required fields. Starting from 2KB, the Azure Cosmos DB service
     * would serialize as much as it could fit till it reaches the maximum specified
     * size.
     *
     * @param limitInKb continuation token size limit.
     * @return the FeedOptions.
     */
    public FeedOptions responseContinuationTokenLimitInKb(int limitInKb) {
        this.responseContinuationTokenLimitInKb = limitInKb;
        return this;
    }

    /**
     * Gets the ResponseContinuationTokenLimitInKb request option for document query
     * requests in the Azure Cosmos DB service. If not already set returns 0.
     *
     * ResponseContinuationTokenLimitInKb is used to limit the length of
     * continuation token in the query response. Valid values are &gt;= 1.
     *
     * @return return set ResponseContinuationTokenLimitInKb, or 0 if not set
     */
    public int responseContinuationTokenLimitInKb() {
        return responseContinuationTokenLimitInKb;
    }
}
