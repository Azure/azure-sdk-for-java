// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.global;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.throughputControl.config.GlobalThroughputControlGroup;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetrySpec;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * In Throughput global control mode, in order to coordinate with other clients and share the throughput defined in the group,
 * there is a need to create/read/replace related items in the control container. This class contains all those related operations.
 */
public class ThroughputControlContainerManager {
    private static final Logger logger = LoggerFactory.getLogger(ThroughputControlContainerManager.class);

    private static final String CLIENT_ITEM_PARTITION_KEY_VALUE_SUFFIX = ".client";
    private static final String CONFIG_ITEM_ID_SUFFIX = ".info";
    private static final String CONFIG_ITEM_PARTITION_KEY_VALUE_SUFFIX = ".config";
    private static final String PARTITION_KEY_PATH = "/groupId";

    private final String clientItemId;
    private final String clientItemPartitionKeyValue;
    private final String configItemId;
    private final String configItemPartitionKeyValue;
    private final CosmosAsyncContainer globalControlContainer;
    private final GlobalThroughputControlGroup group;

    private GlobalThroughputControlConfigItem configItem;
    private GlobalThroughputControlClientItem clientItem;

    public ThroughputControlContainerManager(GlobalThroughputControlGroup group) {
        checkNotNull(group, "Global control group config can not be null");

        this.globalControlContainer = group.getGlobalControlContainer();
        this.group = group;

        String encodedGroupId = Utils.encodeUrlBase64String(this.group.getId().getBytes(StandardCharsets.UTF_8));

        this.clientItemId = encodedGroupId + UUID.randomUUID();
        this.clientItemPartitionKeyValue = this.group.getId() + CLIENT_ITEM_PARTITION_KEY_VALUE_SUFFIX;
        this.configItemId = encodedGroupId + CONFIG_ITEM_ID_SUFFIX;
        this.configItemPartitionKeyValue = this.group.getId() + CONFIG_ITEM_PARTITION_KEY_VALUE_SUFFIX;
    }

    public Mono<GlobalThroughputControlClientItem> createGroupClientItem(double loadFactor, double allocatedThroughput) {
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);

        return Mono.just(
                new GlobalThroughputControlClientItem(
                    this.clientItemId,
                    this.clientItemPartitionKeyValue,
                    loadFactor,
                    allocatedThroughput,
                    this.group.getControlItemExpireInterval()))
            .flatMap(groupClientItem -> this.globalControlContainer.createItem(groupClientItem, requestOptions))
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
     * @return A {@link GlobalThroughputControlClientItem}.
     */
    public Mono<GlobalThroughputControlConfigItem> getOrCreateConfigItem() {
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);

        GlobalThroughputControlConfigItem expectedConfigItem =
            new GlobalThroughputControlConfigItem(
                this.configItemId,
                this.configItemPartitionKeyValue,
                this.group.getTargetThroughput(),
                this.group.getTargetThroughputThreshold(),
                this.group.isDefault());

        return this.globalControlContainer.readItem(
                    this.configItemId,
                    new PartitionKey(this.configItemPartitionKeyValue),
                    GlobalThroughputControlConfigItem.class)
            .onErrorResume(throwable -> {
                CosmosException cosmosException = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
                if (cosmosException != null && cosmosException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    // hooray, you are the first one, needs to create the config file now
                    return this.globalControlContainer.createItem(expectedConfigItem, requestOptions);
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
     * Query the load factor of all other clients except itself for the group, and then add the client load factor to the final sum.

     * @param clientLoadFactor The load factor for the current client.
     * @return The sum of load factor from all clients.
     */
    public Mono<Double> queryLoadFactorsOfAllClients(double clientLoadFactor) {
        // The current design is using ttl to expire client items, so there is no need to check whether the client item is expired.

        String sqlQueryTest = "SELECT VALUE SUM(c.loadFactor) FROM c WHERE c.groupId = @GROUPID AND c.id != @CLIENTITEMID";
        List<SqlParameter> parameters = new ArrayList<>();
        parameters.add(new SqlParameter("@GROUPID", this.clientItemPartitionKeyValue));
        parameters.add(new SqlParameter("@CLIENTITEMID", this.clientItemId));

        SqlQuerySpec querySpec = new SqlQuerySpec(sqlQueryTest, parameters);
        return this.globalControlContainer.queryItems(querySpec, Double.class)
            .single()
            .map(result -> result + clientLoadFactor);
    }

    /**
     * Update the existing group client item.
     *
     * If resource not found, then create a new client item.
     * The client item may get deleted based on the ttl if the client can not keep updating the item due to unexpected failure (for example, network failure).
     *
     * @param loadFactor The new load factor of the client.
     * @param clientAllocatedThroughput The new allocated throughput for the client.
     * @return A {@link GlobalThroughputControlClientItem};
     */
    public Mono<GlobalThroughputControlClientItem> replaceOrCreateGroupClientItem(double loadFactor, double clientAllocatedThroughput) {
        CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();
        itemRequestOptions.setContentResponseOnWriteEnabled(true);

        return Mono.just(this.clientItem)
            .flatMap(groupClientItem -> {
                groupClientItem.setLoadFactor(loadFactor);
                groupClientItem.setAllocatedThroughput(clientAllocatedThroughput);
                return this.globalControlContainer.replaceItem(
                    groupClientItem, groupClientItem.getId(), new PartitionKey(groupClientItem.getGroupId()), itemRequestOptions);
            })
            .onErrorResume(throwable -> {
                CosmosException cosmosException = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
                if (cosmosException != null && cosmosException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    logger.warn("Can not find the expected client item {}, will recreate a new one", this.clientItem.getId());
                    return this.globalControlContainer.createItem(this.clientItem, itemRequestOptions)
                        .retryWhen(RetrySpec.max(5));
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
