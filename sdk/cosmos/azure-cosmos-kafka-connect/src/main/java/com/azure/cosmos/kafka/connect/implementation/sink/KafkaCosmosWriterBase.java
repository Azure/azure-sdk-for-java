package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.kafka.connect.implementation.CosmosExceptionsHelper;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public abstract class KafkaCosmosWriterBase implements IWriter{
    protected String getId(Object recordValue) {
        checkArgument(recordValue instanceof Map, "Argument 'recordValue' is not valid map format.");
        return ((Map<String, Object>) recordValue).get("id").toString();
    }

    protected String getEtag(Object recordValue) {
        checkArgument(recordValue instanceof Map, "Argument 'recordValue' is not valid map format.");
        return ((Map<String, Object>) recordValue).get("_etag").toString();
    }

    protected PartitionKey getPartitionKeyValue(Object recordValue, PartitionKeyDefinition partitionKeyDefinition) {
        checkArgument(recordValue instanceof Map, "Argument 'recordValue' is not valid map format.");

        //TODO[Public Preview]: examine the code here for sub-partition
        String partitionKeyPath = StringUtils.join(partitionKeyDefinition.getPaths(), "");
        Map<String, Object> recordMap = (Map<String, Object>) recordValue;
        Object partitionKeyValue = recordMap.get(partitionKeyPath.substring(1));
        PartitionKeyInternal partitionKeyInternal = PartitionKeyInternal.fromObjectArray(Collections.singletonList(partitionKeyValue), false);

        return ImplementationBridgeHelpers
            .PartitionKeyHelper
            .getPartitionKeyAccessor()
            .toPartitionKey(partitionKeyInternal);
    }

    protected boolean shouldRetry(Throwable exception, int attemptedCount, int maxRetryCount) {
        if (attemptedCount > maxRetryCount) {
            return false;
        }

        return CosmosExceptionsHelper.isTransientFailure(exception);
    }

    protected Mono<PartitionKeyDefinition> getPartitionKeyDefinition(CosmosAsyncContainer container) {
        return Mono.just(CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase()).getCollectionCache())
            .flatMap(collectionCache -> {
                return collectionCache
                    .resolveByNameAsync(
                        null,
                        ImplementationBridgeHelpers
                            .CosmosAsyncContainerHelper
                            .getCosmosAsyncContainerAccessor()
                            .getLinkWithoutTrailingSlash(container),
                        null,
                        new DocumentCollection())
                    .map(documentCollection -> documentCollection.getPartitionKey());
            });
    }
}
