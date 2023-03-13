// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.implementation.changefeed.pkversion.IncrementalChangeFeedProcessorImpl;
import com.azure.cosmos.implementation.changefeed.epkversion.FullFidelityChangeFeedProcessorImpl;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.function.Consumer;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Helper class to build a {@link ChangeFeedProcessor} instance.
 *
 * Below is an example of building ChangeFeedProcessor for LatestVersion mode.
 *
 * <!-- src_embed com.azure.cosmos.changeFeedProcessor.builder -->
 * <pre>
 * ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder&#40;&#41;
 *     .hostName&#40;hostName&#41;
 *     .feedContainer&#40;feedContainer&#41;
 *     .leaseContainer&#40;leaseContainer&#41;
 *     .handleChanges&#40;docs -&gt; &#123;
 *         for &#40;JsonNode item : docs&#41; &#123;
 *             &#47;&#47; Implementation for handling and processing of each JsonNode item goes here
 *         &#125;
 *     &#125;&#41;
 *     .buildChangeFeedProcessor&#40;&#41;;
 * </pre>
 * <!-- end com.azure.cosmos.changeFeedProcessor.builder -->
 *
 * Below is an example of building ChangeFeedProcessor for AllVersionsAndDeletes mode.
 *
 * <!-- src_embed com.azure.cosmos.allVersionsAndDeletesChangeFeedProcessor.builder -->
 * <pre>
 * ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder&#40;&#41;
 *     .hostName&#40;hostName&#41;
 *     .feedContainer&#40;feedContainer&#41;
 *     .leaseContainer&#40;leaseContainer&#41;
 *     .handleAllVersionsAndDeletesChanges&#40;docs -&gt; &#123;
 *         for &#40;ChangeFeedProcessorItem item : docs&#41; &#123;
 *             &#47;&#47; Implementation for handling and processing of each ChangeFeedProcessorItem item goes here
 *         &#125;
 *     &#125;&#41;
 *     .buildChangeFeedProcessor&#40;&#41;;
 * </pre>
 * <!-- end com.azure.cosmos.allVersionsAndDeletesChangeFeedProcessor.builder -->
 */
public class ChangeFeedProcessorBuilder {
    private String hostName;
    private CosmosAsyncContainer feedContainer;
    private CosmosAsyncContainer leaseContainer;
    private ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private Consumer<List<JsonNode>> incrementalModeLeaseConsumerPkRangeIdVersion;
    private Consumer<List<ChangeFeedProcessorItem>> incrementalModeLeaseConsumerEpkVersion;
    private Consumer<List<ChangeFeedProcessorItem>> fullFidelityModeLeaseConsumer;
    private ChangeFeedMode changeFeedMode = ChangeFeedMode.INCREMENTAL;
    private LeaseVersion leaseVersion = LeaseVersion.PARTITION_KEY_BASED_LEASE;

    /**
     * Instantiates a new Cosmos a new ChangeFeedProcessor builder.
     */
    public ChangeFeedProcessorBuilder() {
    }

    /**
     * Sets the host name.
     *
     * @param hostName the name to be used for the host. When using multiple hosts, each host must have a unique
     * name.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilder hostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Sets and existing {@link CosmosAsyncContainer} to be used to read from the monitored container.
     *
     * @param feedContainer the instance of {@link CosmosAsyncContainer} to be used.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilder feedContainer(CosmosAsyncContainer feedContainer) {
        this.feedContainer = feedContainer;

        return this;
    }

    /**
     * Sets an existing {@link CosmosAsyncContainer} to be used to read from the leases container.
     *
     * @param leaseContainer the instance of {@link CosmosAsyncContainer} to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilder leaseContainer(CosmosAsyncContainer leaseContainer) {
        this.leaseContainer = leaseContainer;

        return this;
    }

    /**
     * Sets a consumer function which will be called to process changes for LatestVersion change feed mode.
     * Attention! This API is not merge proof, please use {@link #handleLatestVersionChanges(Consumer)} instead.
     *
     * <!-- src_embed com.azure.cosmos.changeFeedProcessor.handleChanges -->
     * <pre>
     * .handleChanges&#40;docs -&gt; &#123;
     *     for &#40;JsonNode item : docs&#41; &#123;
     *         &#47;&#47; Implementation for handling and processing of each JsonNode item goes here
     *     &#125;
     * &#125;&#41;
     * </pre>
     * <!-- end com.azure.cosmos.changeFeedProcessor.handleChanges -->
     *
     * @param consumer the {@link Consumer} to call for handling the feeds.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilder handleChanges(Consumer<List<JsonNode>> consumer) {
        checkNotNull(consumer, "Argument 'consumer' can not be null");
        checkArgument(
            this.incrementalModeLeaseConsumerEpkVersion == null,
            "handleLatestVersionChanges consumer has already been defined");

        this.incrementalModeLeaseConsumerPkRangeIdVersion = consumer;
        this.changeFeedMode = ChangeFeedMode.INCREMENTAL;
        this.leaseVersion = LeaseVersion.PARTITION_KEY_BASED_LEASE;
        return this;
    }

    /**
     * Sets a consumer function which will be called to process changes for LatestVersion change feed mode.
     *
     * <!-- src_embed com.azure.cosmos.latestVersionChanges.handleChanges -->
     * <pre>
     * .handleLatestVersionChanges&#40;changeFeedProcessorItems -&gt; &#123;
     *     for &#40;ChangeFeedProcessorItem item : changeFeedProcessorItems&#41; &#123;
     *         &#47;&#47; Implementation for handling and processing of each change feed item goes here
     *     &#125;
     * &#125;&#41;
     * </pre>
     * <!-- end com.azure.cosmos.latestVersionChanges.handleChanges -->
     *
     * @param consumer the {@link Consumer} to call for handling the feeds.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilder handleLatestVersionChanges(Consumer<List<ChangeFeedProcessorItem>> consumer) {
        checkNotNull(consumer, "Argument 'consumer' can not be null");
        checkArgument(
            this.incrementalModeLeaseConsumerPkRangeIdVersion == null,
            "handleChanges consumer has already been defined");

        this.incrementalModeLeaseConsumerEpkVersion = consumer;
        this.changeFeedMode = ChangeFeedMode.INCREMENTAL;
        this.leaseVersion = LeaseVersion.EPK_RANGE_BASED_LEASE;
        return this;
    }

    /**
     * Sets a consumer function which will be called to process changes for AllVersionsAndDeletes change feed mode.
     *
     * <!-- src_embed com.azure.cosmos.allVersionsAndDeletesChangeFeedProcessor.handleChanges -->
     * <pre>
     * .handleAllVersionsAndDeletesChanges&#40;docs -&gt; &#123;
     *     for &#40;ChangeFeedProcessorItem item : docs&#41; &#123;
     *         &#47;&#47; Implementation for handling and processing of each ChangeFeedProcessorItem item goes here
     *     &#125;
     * &#125;&#41;
     * </pre>
     * <!-- end com.azure.cosmos.allVersionsAndDeletesChangeFeedProcessor.handleChanges -->
     *
     * @param consumer the {@link Consumer} to call for handling the feeds.
     * @return current Builder.
     */
    @Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ChangeFeedProcessorBuilder handleAllVersionsAndDeletesChanges(Consumer<List<ChangeFeedProcessorItem>> consumer) {
        this.fullFidelityModeLeaseConsumer = consumer;
        this.changeFeedMode = ChangeFeedMode.FULL_FIDELITY;
        this.leaseVersion = LeaseVersion.EPK_RANGE_BASED_LEASE;
        return this;
    }

    /**
     * Sets the {@link ChangeFeedProcessorOptions} to be used.
     * Unless specifically set the default values that will be used are:
     * <ul>
     * <li>maximum items per page or FeedResponse: 100</li>
     * <li>lease renew interval: 17 seconds</li>
     * <li>lease acquire interval: 13 seconds</li>
     * <li>lease expiration interval: 60 seconds</li>
     * <li>feed poll delay: 5 seconds</li>
     * <li>maximum scale count: unlimited</li>
     * </ul>
     *
     * @param changeFeedProcessorOptions the change feed processor options to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilder options(ChangeFeedProcessorOptions changeFeedProcessorOptions) {
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;

        return this;
    }

    /**
     * Builds a new instance of the {@link ChangeFeedProcessor} with the specified configuration.
     *
     * @return an instance of {@link ChangeFeedProcessor}.
     */
    public ChangeFeedProcessor buildChangeFeedProcessor() {
        validateChangeFeedProcessorBuilder();

        ChangeFeedProcessor changeFeedProcessor = null;
        // Lease version will decide which version of the changeFeed processor we are going to use internally
        // PARTITION_KEY_BASED_LEASE -> ChangeFeedProcessor pkversion
        // EPK_RANGE_BASED_LEASE -> ChangeFeedProcessor epkversion
        if (this.leaseVersion == LeaseVersion.EPK_RANGE_BASED_LEASE) {
            switch (this.changeFeedMode) {
                case FULL_FIDELITY:
                    changeFeedProcessor = new FullFidelityChangeFeedProcessorImpl(
                            this.hostName,
                            this.feedContainer,
                            this.leaseContainer,
                            this.fullFidelityModeLeaseConsumer,
                            this.changeFeedProcessorOptions);
                    break;
                case INCREMENTAL:
                    changeFeedProcessor = new com.azure.cosmos.implementation.changefeed.epkversion.IncrementalChangeFeedProcessorImpl(
                            this.hostName,
                            this.feedContainer,
                            this.leaseContainer,
                            this.incrementalModeLeaseConsumerEpkVersion,
                            this.changeFeedProcessorOptions);
                    break;
                default:
                    throw new IllegalStateException("ChangeFeed mode " + this.changeFeedMode + " is not supported");
            }
        } else {
            changeFeedProcessor = new IncrementalChangeFeedProcessorImpl(
                    this.hostName,
                    this.feedContainer,
                    this.leaseContainer,
                    this.incrementalModeLeaseConsumerPkRangeIdVersion,
                    this.changeFeedProcessorOptions);
        }

        return changeFeedProcessor;
    }

    private void validateChangeFeedProcessorBuilder() {
        checkArgument(StringUtils.isNotEmpty(hostName), "hostName cannot be null or empty");
        checkNotNull(feedContainer, "Argument 'feedContainer' can not be null");
        checkNotNull(leaseContainer, "Argument 'leaseContainer' can not be null");

        if ((isIncrementalConsumerDefined() && isFullFidelityConsumerDefined())
            || (!isIncrementalConsumerDefined() && !isFullFidelityConsumerDefined())) {
            throw new IllegalArgumentException("expecting either LatestVersion or AllVersionsAndDeletes consumer for handling change feed processor changes");
        }
        validateChangeFeedProcessorOptions();
    }

    private boolean isFullFidelityConsumerDefined() {
        return this.fullFidelityModeLeaseConsumer != null;
    }

    private boolean isIncrementalConsumerDefined() {
        return this.incrementalModeLeaseConsumerEpkVersion != null || this.incrementalModeLeaseConsumerPkRangeIdVersion != null;
    }

    private void validateChangeFeedProcessorOptions() {
        if (this.changeFeedProcessorOptions == null) {
            return;
        }
        if (this.changeFeedProcessorOptions.getLeaseRenewInterval().compareTo(this.changeFeedProcessorOptions.getLeaseExpirationInterval()) >= 0) {
            // Lease renewer task must execute at a faster frequency than expiration setting; otherwise this will
            //  force a lot of resets and lead to a poor overall performance of ChangeFeedProcessor.
            throw new IllegalArgumentException("changeFeedProcessorOptions: expecting leaseRenewInterval less than leaseExpirationInterval");
        }
        //  Some extra checks for all versions and deletes mode
        if (ChangeFeedMode.FULL_FIDELITY.equals(changeFeedMode)) {
            if (this.changeFeedProcessorOptions.getStartTime() != null) {
                throw new IllegalStateException("changeFeedProcessorOptions: AllVersionsAndDeletes change feed mode is not supported for startTime option.");
            }

            if (this.changeFeedProcessorOptions.isStartFromBeginning()) {
                throw new IllegalStateException("changeFeedProcessorOptions: AllVersionsAndDeletes change feed mode is not supported for startFromBeginning option.");
            }
        }
    }
}
