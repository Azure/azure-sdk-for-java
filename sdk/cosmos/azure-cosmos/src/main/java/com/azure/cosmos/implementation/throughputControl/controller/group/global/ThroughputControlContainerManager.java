// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.global;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputGlobalControlGroup;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetrySpec;

import java.util.UUID;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * In Throughput global control mode, in order to coordinate with other clients and share the throughput defined in the group,
 * there is a need to create/read/replace related items in the control container. This class contains all those related operations.
 */
public class ThroughputControlContainerManager {
    private static final Logger logger = LoggerFactory.getLogger(ThroughputControlContainerManager.class);

    private static final String CLINT_ITEM_ID_SUFFIX = "." + UUID.randomUUID();
    private static final String CLIENT_ITEM_PARTITION_KEY_VALUE_SUFFIX = ".client";
    private static final String CONFIG_ITEM_ID_SUFFIX = ".info";
    private static final String CONFIG_ITEM_PARTITION_KEY_VALUE_SUFFIX = ".config";
    private static final String PARTITION_KEY_PATH = "/group";

    private final String clientItemId;
    private final String clientItemPartitionKeyValue;
    private final String configItemId;
    private final String configItemPartitionKeyValue;
    private final CosmosAsyncContainer globalControlContainer;
    private final ThroughputGlobalControlGroup group;

    private ThroughputGlobalControlConfigItem configItem;
    private ThroughputGlobalControlClientItem clientItem;

    public ThroughputControlContainerManager(ThroughputGlobalControlGroup group) {
        checkNotNull(group, "Global control group config can not be null");

        this.globalControlContainer = group.getGlobalControlContainer();
        this.group = group;

        this.clientItemId = this.group.getId()  + CLINT_ITEM_ID_SUFFIX;
        this.clientItemPartitionKeyValue = this.group.getGroupName() + CLIENT_ITEM_PARTITION_KEY_VALUE_SUFFIX;
        this.configItemId = this.group.getId() + CONFIG_ITEM_ID_SUFFIX;
        this.configItemPartitionKeyValue = this.group.getGroupName() + CONFIG_ITEM_PARTITION_KEY_VALUE_SUFFIX;
    }

    public Mono<ThroughputGlobalControlClientItem> createGroupClientItem(double loadFactor) {
        return Mono.just(new ThroughputGlobalControlClientItem(
                                this.clientItemId, this.clientItemPartitionKeyValue, loadFactor, this.group.getControlItemExpireInterval()))
            .flatMap(groupClientItem -> this.globalControlContainer.createItem(groupClientItem))
            .flatMap(itemResponse -> {
                this.clientItem = itemResponse.getItem();
                return Mono.just(this.clientItem);
            });
    }

    /**
     * Get or create the throughput global control config item.
     * This is to make sure all the clients are using the same configuration for the group.
     *
     * The config item in the control container will be used as the source of truth.
     * If the client has a different config, it will be overwritten by the one in the control container.
     *
     * @return A {@link ThroughputGlobalControlClientItem}.
     */
    public Mono<ThroughputGlobalControlConfigItem> getOrCreateConfigItem() {
        ThroughputGlobalControlConfigItem expectedConfigItem =
            new ThroughputGlobalControlConfigItem(
                this.configItemId,
                this.configItemPartitionKeyValue,
                this.group.getTargetThroughput(),
                this.group.getTargetThroughputThreshold(),
                this.group.isDefault());

        return this.globalControlContainer.readItem(
                    this.configItemId,
                    new PartitionKey(this.configItemPartitionKeyValue),
                    ThroughputGlobalControlConfigItem.class)
            .onErrorResume(throwable -> {
                CosmosException cosmosException = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
                if (cosmosException != null && cosmosException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    // hooray, you are the first one, needs to create the config file now
                    return this.globalControlContainer.createItem(expectedConfigItem);
                }

                return Mono.error(throwable);
            })
            .retryWhen(RetrySpec.max(10).filter(throwable -> {
                CosmosException cosmosException = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
                return cosmosException != null && cosmosException.getStatusCode() == HttpConstants.StatusCodes.CONFLICT;
            }))
            .flatMap(itemResponse -> {
                this.configItem = itemResponse.getItem();

                if (!expectedConfigItem.equals(configItem)) {
                    logger.warn(
                        "Group config using by this client is different than the one in control container, will be ignored. Using following config: {}",
                        this.configItem.toString());
                }

                return Mono.just(this.configItem);
            });
    }

    /**
     * Query the load factor of all clients for the group.
     *
     * @return The sum of load factor from all clients.
     */
    public Mono<Double> queryLoadFactorFromAllClients() {
        // The current design is using ttl to expire client items, so there is no need to check whether the client item is expired.
        return this.globalControlContainer.readAllItems(new PartitionKey(this.clientItemPartitionKeyValue), ThroughputGlobalControlClientItem.class)
            .collectList()
            .flatMapMany(clientItemList -> Flux.fromIterable(clientItemList))
            .map(clientItem -> clientItem.getLoadFactor())
            .reduce((x1, x2) -> x1 + x2 );
    }

    /**
     * Update the existing group client item.
     *
     * If resource not found, then create a new client item.
     * The client item may get deleted based on the ttl if the client can not keep updating the item due to unexpected failure (for example, network failure).
     *
     * @param loadFactor The new load factor of the client.
     * @return A {@link ThroughputGlobalControlClientItem};
     */
    public Mono<ThroughputGlobalControlClientItem> replaceOrCreateGroupClientItem(double loadFactor) {
        return Mono.just(this.clientItem)
            .flatMap(groupClientItem -> {
                groupClientItem.setLoadFactor(loadFactor);
                return this.globalControlContainer.replaceItem(groupClientItem, groupClientItem.getId(), new PartitionKey(groupClientItem.getGroup()));
            })
            .onErrorResume(throwable -> {
                CosmosException cosmosException = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
                if (cosmosException != null && cosmosException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    logger.warn("Can not find the expected client item {}, will recreate a new one", this.clientItem.getId());
                    return this.globalControlContainer.createItem(this.clientItem);
                }

                return Mono.error(throwable);
            })
            .flatMap(itemResponse -> {
                this.clientItem = itemResponse.getItem();
                return Mono.just(this.clientItem);
            });
    }

    /**
     * Make sure the control container provided is partitioned as expected.
     *
     * @return A {@link ThroughputControlContainerManager}.
     */
    public Mono<ThroughputControlContainerManager> validateControlContainer() {
        return this.globalControlContainer.read()
            .map(containerResponse -> containerResponse.getProperties())
            .flatMap(containerProperties -> {
                boolean isPartitioned =
                    containerProperties.getPartitionKeyDefinition() != null &&
                        containerProperties.getPartitionKeyDefinition().getPaths() != null &&
                        containerProperties.getPartitionKeyDefinition().getPaths().size() > 0;
                if (!isPartitioned
                        || (containerProperties.getPartitionKeyDefinition().getPaths().size() != 1
                        || !containerProperties.getPartitionKeyDefinition().getPaths().get(0).equals(PARTITION_KEY_PATH))) {
                    return Mono.error(new IllegalArgumentException("The control container must have partition key equal to " + PARTITION_KEY_PATH));
                }

                return Mono.empty();
            })
            .thenReturn(this);
    }
}
