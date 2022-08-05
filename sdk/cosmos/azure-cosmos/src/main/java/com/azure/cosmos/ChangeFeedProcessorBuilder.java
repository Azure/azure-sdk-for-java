// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.changefeed.incremental.ChangeFeedProcessorBuilderImpl;
import com.azure.cosmos.models.ChangeFeedMode;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.function.Consumer;

/**
 * Helper class to build a {@link ChangeFeedProcessor} instance.
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
 */
public class ChangeFeedProcessorBuilder {
    private String hostName;
    private CosmosAsyncContainer feedContainer;
    private CosmosAsyncContainer leaseContainer;
    private ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private Consumer<List<JsonNode>> partitionKeyBasedLeaseConsumer;
    private Consumer<List<ChangeFeedProcessorItem>> epkRangeBasedLeaseConsumer;
    private ChangeFeedMode changeFeedMode = ChangeFeedMode.INCREMENTAL;

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
     * Sets a consumer function which will be called to process changes.
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
        this.partitionKeyBasedLeaseConsumer = consumer;

        return this;
    }

    /**
     * Sets a consumer function which will be called to process changes for full fidelity
     *
     * <!-- src_embed com.azure.cosmos.fullFidelityChangeFeedProcessor.handleChanges -->
     * <pre>
     * .handleChanges&#40;docs -&gt; &#123;
     *     for &#40;JsonNode item : docs&#41; &#123;
     *         &#47;&#47; Implementation for handling and processing of each JsonNode item goes here
     *     &#125;
     * &#125;&#41;
     * </pre>
     * <!-- end com.azure.cosmos.fullFidelityChangeFeedProcessor.handleChanges -->
     *
     * @param consumer the {@link Consumer} to call for handling the feeds.
     * @return current Builder.
     */
    //  TODO:(kuthapar) - finalize the name for this.
    //  handleChangesWithOperationLogs() ?
    //  handleChangesWithFullFidelity() ?
    //  handleChangesForFullFidelity() ?
    //  Future options -> handleChangesWithMergeSupport() ?
    //  Future options -> handleChangesWithLogicalPartition() ?
    //  Or keep it generic and open for future -> handleChangesV1() ?
    @Beta(value = Beta.SinceVersion.V4_34_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ChangeFeedProcessorBuilder handleFullFidelityChanges(Consumer<List<ChangeFeedProcessorItem>> consumer) {
        this.epkRangeBasedLeaseConsumer = consumer;

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
     * Sets the Change Feed Mode to be used with this {@link ChangeFeedProcessor}.
     * Unless specifically set, the default will be {@link ChangeFeedMode#INCREMENTAL} to support backward-compatibility.
     *
     * @param changeFeedMode changeFeedMode to be used for this {@link ChangeFeedProcessor}
     * @return current Builder
     */
    @Beta(value = Beta.SinceVersion.V4_34_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ChangeFeedProcessorBuilder changeFeedMode(ChangeFeedMode changeFeedMode) {
        this.changeFeedMode = changeFeedMode;
        return this;
    }

    /**
     * Builds a new instance of the {@link ChangeFeedProcessor} with the specified configuration.
     *
     * @return an instance of {@link ChangeFeedProcessor}.
     */
    public ChangeFeedProcessor buildChangeFeedProcessor() {
        validateChangeFeedProcessorBuilder();

        if (ChangeFeedMode.INCREMENTAL.equals(changeFeedMode)) {
            ChangeFeedProcessorBuilderImpl builder = new ChangeFeedProcessorBuilderImpl()
                .hostName(this.hostName)
                .feedContainer(this.feedContainer)
                .leaseContainer(this.leaseContainer)
                .handleChanges(this.partitionKeyBasedLeaseConsumer);

            if (this.changeFeedProcessorOptions != null) {
                builder.options(this.changeFeedProcessorOptions);
            }

            return builder.build();
        } else {
            com.azure.cosmos.implementation.changefeed.fullfidelity.ChangeFeedProcessorBuilderImpl builder =
                new com.azure.cosmos.implementation.changefeed.fullfidelity.ChangeFeedProcessorBuilderImpl()
                    .hostName(this.hostName)
                    .feedContainer(this.feedContainer)
                    .leaseContainer(this.leaseContainer)
                    .handleChanges(this.epkRangeBasedLeaseConsumer);
            if (this.changeFeedProcessorOptions != null) {
                builder.options(this.changeFeedProcessorOptions);
            }
            return builder.build();
        }
    }

    private void validateChangeFeedProcessorBuilder() {
        if (hostName == null || hostName.isEmpty()) {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }
        if (feedContainer == null) {
            throw new IllegalArgumentException("feedContainer cannot be null");
        }
        if (leaseContainer == null) {
            throw new IllegalArgumentException("leaseContainer cannot be null");
        }
        if ((partitionKeyBasedLeaseConsumer == null && epkRangeBasedLeaseConsumer == null)
            || (partitionKeyBasedLeaseConsumer != null && epkRangeBasedLeaseConsumer != null)) {
            throw new IllegalArgumentException("expecting either incremental or full fidelity consumer for handling change feed processor changes");
        }
        if (ChangeFeedMode.INCREMENTAL.equals(changeFeedMode) && partitionKeyBasedLeaseConsumer == null) {
            throw new IllegalArgumentException("consumer for handling change feed processor incremental changes cannot be null when using incremental mode");
        }
        if (ChangeFeedMode.FULL_FIDELITY.equals(changeFeedMode) && epkRangeBasedLeaseConsumer == null) {
            throw new IllegalArgumentException("consumer for handling change feed processor full fidelity changes cannot be null when using full fidelity mode");
        }
        validateChangeFeedProcessorOptions();
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
        //  Some extra checks for full fidelity mode
        if (ChangeFeedMode.FULL_FIDELITY.equals(changeFeedMode)) {
            if (this.changeFeedProcessorOptions.getStartTime() != null) {
                throw new IllegalStateException("changeFeedProcessorOptions: Full fidelity change feed is not supported for startTime option.");
            }

            if (this.changeFeedProcessorOptions.isStartFromBeginning()) {
                throw new IllegalStateException("changeFeedProcessorOptions: Full fidelity change feed is not supported for startFromBeginning option.");
            }
        }
    }
}
