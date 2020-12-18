package com.azure.cosmos.implementation.throughputBudget;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetrySpec;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ThroughputBudgetControlContainerManager {
    private final  CosmosAsyncContainer controlContainer;
    private final ThroughputBudgetGroupConfigInternal groupConfig;
    private final String groupConfigItemId;
    private final String groupClientItemId;
    private final String fullGroupName;
    private final String hostName;
    private ThroughputBudgetGroupConfigItem groupConfigItem;
    private ThroughputBudgetGroupClientItem groupClientItem;
    private final Integer groupClientItemExpireInSeconds;


    public ThroughputBudgetControlContainerManager(
        CosmosAsyncContainer controlContainer,
        ThroughputBudgetGroupConfigInternal groupConfig) {
        this.controlContainer = controlContainer;
        this.groupConfig = groupConfig;

        this.fullGroupName = this.getFullGroupName();
        this.groupConfigItemId = fullGroupName + ".info";
        this.groupClientItemId = fullGroupName + "." + UUID.randomUUID();
        this.hostName = this.groupConfig.getHostName();
        this.groupClientItemExpireInSeconds = (int)this.groupConfig.getDistributedControlConfig().getDocumentExpireInterval().getSeconds();
        this.groupConfigItem = new ThroughputBudgetGroupConfigItem(
            this.groupConfigItemId,
            this.groupConfigItemId, // TODO: change to use fullGroupName
            this.groupConfig.getThroughputLimit() == null ? StringUtils.EMPTY : String.valueOf(this.groupConfig.getThroughputLimit()),
            this.groupConfig.getThroughputLimitThreshold() == null ? StringUtils.EMPTY : String.valueOf(this.groupConfig.getThroughputLimitThreshold()),
            this.groupConfig.isUseByDefault());

        this.groupClientItem = new ThroughputBudgetGroupClientItem(
            this.groupClientItemId,
            this.fullGroupName,
            this.hostName);
    }

    private String getFullGroupName() {
        return this.groupConfig.getTargetContainer().getDatabase().getId() + "_" +
            this.groupConfig.getTargetContainer().getId() + "_" +
            this.groupConfig.getGroupName();
    }

    public Mono<Void> validateControllerContainer() {
        // make sure the container is configured as expected
        return this.controlContainer.read()
            .map(containerResponse -> containerResponse.getProperties())
            .flatMap(containerProperties -> {
                boolean isPartitioned =
                    containerProperties.getPartitionKeyDefinition() != null &&
                        containerProperties.getPartitionKeyDefinition().getPaths() != null &&
                        containerProperties.getPartitionKeyDefinition().getPaths().size() > 0;
                if (!isPartitioned || (containerProperties.getPartitionKeyDefinition().getPaths().size() != 1 || !containerProperties.getPartitionKeyDefinition().getPaths().get(0).equals("/group"))) {
                    return Mono.error(new IllegalArgumentException("The controller container must have partition key equal to group."));
                }

                return Mono.empty();
            });
    }

    // TODO: make sur
    public Mono<Void> validateGroupConfigItem() {
        return this.controlContainer.readItem(
            this.groupConfigItemId,
            new PartitionKey(this.fullGroupName),
            new CosmosItemRequestOptions(),
            ThroughputBudgetGroupConfigItem.class)
            .flatMap(itemResponse -> {
                if (this.groupConfigItem.equals(itemResponse)) {
                    return Mono.empty();
                } else {
                    return Mono.error(new IllegalArgumentException("Configuration this client use is different than other clients"));
                }
            })
            .onErrorResume(throwable -> {
                CosmosException cosmosException = Utils.as(throwable, CosmosException.class);
                if (cosmosException != null && cosmosException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    // hooray, you are the first one, needs to create the config file now
                    return this.controlContainer.createItem(this.groupConfigItem);
                }

                return Mono.error(throwable);
            })
            .retryWhen(RetrySpec.max(1).filter(throwable -> {
                CosmosException cosmosException = Utils.as(throwable, CosmosException.class);
                return cosmosException != null && cosmosException.getStatusCode() == HttpConstants.StatusCodes.CONFLICT;
            }))
            .then();
    }

    public Mono<Void> createGroupClientItem(double loadFactor) {
        return Mono.just(this.groupClientItem)
            .flatMap(groupClientItem -> {
                ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
                groupClientItem.setLoadFactor(loadFactor);
                groupClientItem.setTtl(this.groupClientItemExpireInSeconds);

                return this.controlContainer.createItem(groupClientItem);
            })
            .flatMap(itemResponse -> {
                this.groupClientItem.setEtag(itemResponse.getETag());
                return Mono.empty();
            });
    }

    public Mono<Void> upsertGroupClientItem(double loadFactor) {
        return Mono.just(this.groupClientItem)
            .flatMap(groupClientItem -> {
                ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
                groupClientItem.setLoadFactor(loadFactor);
                groupClientItem.setTtl(this.groupClientItemExpireInSeconds);

                return this.controlContainer.upsertItem(groupClientItem);
            })
            .flatMap(itemResponse -> {
                this.groupClientItem.setEtag(itemResponse.getETag());
                return Mono.empty();
            });
    }

    public Mono<Double> queryClientLoads() {
        String queryText = "select * from c where c.group = @group";
        SqlQuerySpec querySpec = new SqlQuerySpec(queryText);
        List<SqlParameter> parameters = Collections
            .singletonList(new SqlParameter("@group", fullGroupName));
        querySpec.setParameters(parameters);

        return this.controlContainer.readAllItems(new PartitionKey(this.fullGroupName), ThroughputBudgetGroupClientItem.class)
            .collectList()
            .flatMapMany(clientItemist -> Flux.fromIterable(clientItemist))
            .map(clientItem -> clientItem.getLoadFactor())
            .reduce((x1, x2) -> x1 + x2 );
    }
}
