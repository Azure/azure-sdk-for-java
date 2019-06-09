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

import com.azure.data.cosmos.changefeed.CheckpointFrequency;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.ExecutorService;

public class ChangeFeedProcessorOptions {
    private static final int DefaultQueryPartitionsMaxBatchSize = 100;
    private static Duration DefaultRenewInterval =  Duration.ofMillis(0).plusSeconds(17);
    private static Duration DefaultAcquireInterval = Duration.ofMillis(0).plusSeconds(13);
    private static Duration DefaultExpirationInterval = Duration.ofMillis(0).plusSeconds(60);
    private static Duration DefaultFeedPollDelay = Duration.ofMillis(0).plusSeconds(5);

    private Duration leaseRenewInterval;
    private Duration leaseAcquireInterval;
    private Duration leaseExpirationInterval;
    private Duration feedPollDelay;
    private CheckpointFrequency checkpointFrequency;

    private String leasePrefix;
    private int maxItemCount;
    private String startContinuation;
    private OffsetDateTime startTime;
    private boolean startFromBeginning;
    private String sessionToken;
    private int minPartitionCount;
    private int maxPartitionCount;
    private boolean discardExistingLeases;
    private int queryPartitionsMaxBatchSize;
    private int degreeOfParallelism;
    private ExecutorService executorService;

    public ChangeFeedProcessorOptions() {
        this.maxItemCount = 100;
        this.startFromBeginning = false;
        this.leaseRenewInterval = DefaultRenewInterval;
        this.leaseAcquireInterval = DefaultAcquireInterval;
        this.leaseExpirationInterval = DefaultExpirationInterval;
        this.feedPollDelay = DefaultFeedPollDelay;
        this.queryPartitionsMaxBatchSize = DefaultQueryPartitionsMaxBatchSize;
        this.checkpointFrequency = new CheckpointFrequency();
        this.maxPartitionCount = 0; // unlimited
        this.degreeOfParallelism = 25; // default
        this.executorService = null;
    }

    /**
     * Gets the renew interval for all leases for partitions currently held by {@link ChangeFeedProcessor} instance.
     *
     * @return the renew interval for all leases for partitions.
     */
    public Duration leaseRenewInterval() {
        return this.leaseRenewInterval;
    }

    /**
     * Sets the renew interval for all leases for partitions currently held by {@link ChangeFeedProcessor} instance.
     *
     * @param leaseRenewInterval the renew interval for all leases for partitions currently held by {@link ChangeFeedProcessor} instance.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions leaseRenewInterval(Duration leaseRenewInterval) {
        this.leaseRenewInterval = leaseRenewInterval;
        return this;
    }

    /**
     * Gets the interval to kick off a task to compute if partitions are distributed evenly among known host instances.
     *
     * @return the interval to kick off a task to compute if partitions are distributed evenly among known host instances.
     */
    public Duration leaseAcquireInterval() {
        return this.leaseAcquireInterval;
    }

    /**
     * Sets he interval to kick off a task to compute if partitions are distributed evenly among known host instances.
     * @param leaseAcquireInterval he interval to kick off a task to compute if partitions are distributed evenly among known host instances.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions leaseAcquireInterval(Duration leaseAcquireInterval) {
        this.leaseAcquireInterval = leaseAcquireInterval;
        return this;
    }

    /**
     * Gets the interval for which the lease is taken on a lease representing a partition.
     *
     * <p>
     * If the lease is not renewed within this interval, it will cause it to expire and ownership of the partition will
     * move to another {@link ChangeFeedProcessor} instance.
     *
     * @return the interval for which the lease is taken on a lease representing a partition.
     */
    public Duration leaseExpirationInterval() {
        return this.leaseExpirationInterval;
    }

    /**
     * Sets the interval for which the lease is taken on a lease representing a partition.
     *
     * <p>
     * If the lease is not renewed within this interval, it will cause it to expire and ownership of the partition will
     * move to another {@link ChangeFeedProcessor} instance.
     *
     * @param leaseExpirationInterval the interval for which the lease is taken on a lease representing a partition.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions leaseExpirationInterval(Duration leaseExpirationInterval) {
        this.leaseExpirationInterval = leaseExpirationInterval;
        return this;
    }

    /**
     * Gets the delay in between polling a partition for new changes on the feed, after all current changes are drained.
     *
     * @return the delay in between polling a partition for new changes on the feed.
     */
    public Duration feedPollDelay() {
        return this.feedPollDelay;
    }

    /**
     * Sets the delay in between polling a partition for new changes on the feed, after all current changes are drained.
     *
     * @param feedPollDelay the delay in between polling a partition for new changes on the feed, after all current changes are drained.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions feedPollDelay(Duration feedPollDelay) {
        this.feedPollDelay = feedPollDelay;
        return this;
    }

    /**
     * Gets the frequency how often to checkpoint leases.
     *
     * @return the frequency how often to checkpoint leases.
     */
    public CheckpointFrequency checkpointFrequency() {
        return this.checkpointFrequency;
    }

    /**
     * Sets the frequency how often to checkpoint leases.
     *
     * @param checkpointFrequency the frequency how often to checkpoint leases.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions checkpointFrequency(CheckpointFrequency checkpointFrequency) {
        this.checkpointFrequency = checkpointFrequency;
        return this;
    }

    /**
     * Gets a prefix to be used as part of the lease ID.
     * <p>
     * This can be used to support multiple instances of {@link ChangeFeedProcessor} instances pointing at the same
     *   feed while using the same auxiliary collection.
     *
     * @return a prefix to be used as part of the lease ID.
     */
    public String leasePrefix() {
        return this.leasePrefix;
    }

    /**
     * Sets a prefix to be used as part of the lease ID.
     *
     * @param leasePrefix a prefix to be used as part of the lease ID.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions leasePrefix(String leasePrefix) {
        this.leasePrefix = leasePrefix;
        return this;
    }

    /**
     * Gets the maximum number of items to be returned in the enumeration operation in the Azure Cosmos DB service.
     *
     * @return the maximum number of items to be returned in the enumeration operation in the Azure Cosmos DB service.
     */
    public int maxItemCount() {
        return this.maxItemCount;
    }

    /**
     * Sets the maximum number of items to be returned in the enumeration operation.
     *
     * @param maxItemCount the maximum number of items to be returned in the enumeration operation.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions maxItemCount(int maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    /**
     * Gets the start request continuation token to start looking for changes after.
     * <p>
     * This is only used when lease store is not initialized and is ignored if a lease for partition exists and
     *   has continuation token. If this is specified, both StartTime and StartFromBeginning are ignored.
     *
     * @return the start request continuation token to start looking for changes after.
     */
    public String startContinuation() {
        return this.startContinuation;
    }

    /**
     * Sets the start request continuation token to start looking for changes after.
     * <p>
     * This is only used when lease store is not initialized and is ignored if a lease for partition exists and
     *   has continuation token. If this is specified, both StartTime and StartFromBeginning are ignored.
     *
     * @param startContinuation the start request continuation token to start looking for changes after.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions startContinuation(String startContinuation) {
        this.startContinuation= startContinuation;
        return this;
    }

    /**
     * Gets the time (exclusive) to start looking for changes after.
     * <p>
     * This is only used when:
     *   (1) Lease store is not initialized and is ignored if a lease for partition exists and has continuation token.
     *   (2) StartContinuation is not specified.
     * If this is specified, StartFromBeginning is ignored.
     *
     * @return the time (exclusive) to start looking for changes after.
     */
    public OffsetDateTime startTime() {
        return this.startTime;
    }

    /**
     * Sets the time (exclusive) to start looking for changes after (UTC time).
     * <p>
     * This is only used when:
     *   (1) Lease store is not initialized and is ignored if a lease for partition exists and has continuation token.
     *   (2) StartContinuation is not specified.
     * If this is specified, StartFromBeginning is ignored.
     *
     * @param startTime the time (exclusive) to start looking for changes after.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions startTime(OffsetDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Gets a value indicating whether change feed in the Azure Cosmos DB service should start from beginning (true)
     *   or from current (false). By default it's start from current (false).
     * <p>
     * This is only used when:
     *   (1) Lease store is not initialized and is ignored if a lease for partition exists and has continuation token.
     *   (2) StartContinuation is not specified.
     *   (3) StartTime is not specified.
     *
     * @return a value indicating whether change feed in the Azure Cosmos DB service should start from.
     */
    public boolean startFromBeginning() {
        return this.startFromBeginning;
    }

    /**
     * Sets a value indicating whether change feed in the Azure Cosmos DB service should start from beginning.
     * <p>
     * This is only used when:
     *   (1) Lease store is not initialized and is ignored if a lease for partition exists and has continuation token.
     *   (2) StartContinuation is not specified.
     *   (3) StartTime is not specified.
     *
     * @param startFromBeginning Indicates to start from beginning if true
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions startFromBeginning(boolean startFromBeginning) {
        this.startFromBeginning = startFromBeginning;
        return this;
    }

    /**
     * Sets a value indicating whether change feed in the Azure Cosmos DB service should start from beginning.
     * <p>
     * This is only used when:
     *   (1) Lease store is not initialized and is ignored if a lease for partition exists and has continuation token.
     *   (2) StartContinuation is not specified.
     *   (3) StartTime is not specified.
     *
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions withoutStartFromBeginning() {
        this.startFromBeginning = false;
        return this;
    }

    /**
     * Gets the session token for use with session consistency in the Azure Cosmos DB service.
     *
     * @return the session token for use with session consistency in the Azure Cosmos DB service.
     */
    public String sessionToken() {
        return this.sessionToken;
    }

    /**
     * Sets the session token for use with session consistency in the Azure Cosmos DB service.
     *
     * @param sessionToken the session token for use with session consistency in the Azure Cosmos DB service.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions sessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    /**
     * Gets the minimum partition count for the host.
     * <p>
     * This can be used to increase the number of partitions for the host and thus override equal distribution (which
     *   is the default) of leases between hosts.
     *
     * @return the minimum partition count for the host.
     */
    public int minPartitionCount() {
        return this.minPartitionCount;
    }

    /**
     * Sets the minimum partition count for the host.
     * <p>
     * This can be used to increase the number of partitions for the host and thus override equal distribution (which
     *   is the default) of leases between hosts.
     *
     * @param minPartitionCount the minimum partition count for the host.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions minPartitionCount(int minPartitionCount) {
        this.minPartitionCount = minPartitionCount;
        return this;
    }

    /**
     * Gets the maximum number of partitions the host can serve.
     * <p>
     * This can be used property to limit the number of partitions for the host and thus override equal distribution
     *  (which is the default) of leases between hosts. DEFAULT is 0 (unlimited).
     *
     * @return the maximum number of partitions the host can serve.
     */
    public int maxPartitionCount() {
        return this.maxPartitionCount;
    }

    /**
     * Sets the maximum number of partitions the host can serve.
     *
     * @param maxPartitionCount the maximum number of partitions the host can serve.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions maxPartitionCount(int maxPartitionCount) {
        this.maxPartitionCount = maxPartitionCount;
        return this;
    }

    /**
     * Gets a value indicating whether on start of the host all existing leases should be deleted and the host
     *   should start from scratch.
     *
     * @return a value indicating whether on start of the host all existing leases should be deleted and the host should start from scratch.
     */
    public boolean discardExistingLeases() {
        return this.discardExistingLeases;
    }

    /**
     * Sets a value indicating whether on start of the host all existing leases should be deleted and the host
     *   should start from scratch.
     *
     * @param discardExistingLeases Indicates whether to discard all existing leases if true
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions discardExistingLeases(boolean discardExistingLeases) {
        this.discardExistingLeases = discardExistingLeases;
        return this;
    }

    /**
     * Gets the Batch size of query partitions API.
     *
     * @return the Batch size of query partitions API.
     */
    public int queryPartitionsMaxBatchSize() {
        return this.queryPartitionsMaxBatchSize;
    }

    /**
     * Sets the Batch size of query partitions API.
     *
     * @param queryPartitionsMaxBatchSize the Batch size of query partitions API.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions queryPartitionsMaxBatchSize(int queryPartitionsMaxBatchSize) {
        this.queryPartitionsMaxBatchSize = queryPartitionsMaxBatchSize;
        return this;
    }

    /**
     * Gets maximum number of tasks to use for auxiliary calls.
     *
     * @return maximum number of tasks to use for auxiliary calls.
     */
    public int degreeOfParallelism() {
        return this.degreeOfParallelism;
    }

    /**
     * Sets maximum number of tasks to use for auxiliary calls.
     *
     * @param defaultQueryPartitionsMaxBatchSize maximum number of tasks to use for auxiliary calls.
     * @return the current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions degreeOfParallelism(int defaultQueryPartitionsMaxBatchSize) {
        this.queryPartitionsMaxBatchSize = queryPartitionsMaxBatchSize;
        return this;
    }

    /**
     * Gets the current {@link ExecutorService} which will be used to control the thread pool.
     *
     * @return current ExecutorService instance.
     */
    public ExecutorService executorService() {
        return this.executorService;
    }

    /**
     * Sets the {@link ExecutorService} to be used to control the thread pool.
     *
     * @param executorService The instance of {@link ExecutorService} to use.
     * @return current ChangeFeedProcessorOptions instance.
     */
    public ChangeFeedProcessorOptions executorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

}
