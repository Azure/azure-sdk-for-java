// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.PartitionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class BenchmarkHelper {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkHelper.class);
    public static PojoizedJson generateDocument(String idString, String dataFieldValue, String partitionKey,
                                         int dataFieldCount) {
        PojoizedJson instance = new PojoizedJson();
        Map<String, Object> properties = instance.getInstance();
        properties.put("id", idString);
        properties.put(partitionKey, idString);
        for (int i = 0; i < dataFieldCount; i++) {
            properties.put("dataField" + i, dataFieldValue);
        }

        return instance;
    }

    public static boolean shouldContinue(long startTimeMillis, long iterationCount, TenantWorkloadConfig workloadConfig) {
        Duration maxDurationTime = workloadConfig.getMaxRunningTimeDuration();
        int maxNumberOfOperations = workloadConfig.getNumberOfOperations();

        if (maxDurationTime == null) {
            return iterationCount < maxNumberOfOperations;
        }

        return startTimeMillis + maxDurationTime.toMillis() > System.currentTimeMillis();
    }

    /**
     * Retries failed bulk operation responses by falling back to individual createItem calls.
     * Ignores 409 (Conflict) errors since the document already exists.
     *
     * @param failedResponses list of failed bulk operation responses
     * @param container the container to retry against
     * @param partitionKeyName the partition key property name
     */
    public static <TContext> void retryFailedBulkOperations(
        List<CosmosBulkOperationResponse<TContext>> failedResponses,
        CosmosAsyncContainer container,
        String partitionKeyName) {

        if (failedResponses.isEmpty()) {
            return;
        }

        logger.info("Retrying {} failed bulk operations with individual createItem calls", failedResponses.size());

        Flux.fromIterable(failedResponses)
            .flatMap(failedResponse -> {
                CosmosItemOperation operation = failedResponse.getOperation();
                PojoizedJson item = operation.getItem();
                PartitionKey pk = operation.getPartitionKeyValue();

                return container.createItem(item, pk, null)
                    .retryWhen(Retry.max(5).filter(error -> {
                        if (!(error instanceof CosmosException)) {
                            return false;
                        }
                        int statusCode = ((CosmosException) error).getStatusCode();
                        return statusCode == 410
                            || statusCode == 408
                            || statusCode == 429
                            || statusCode == 500
                            || statusCode == 503;
                    }))
                    .onErrorResume(error -> {
                        if (error instanceof CosmosException
                            && ((CosmosException) error).getStatusCode() == 409) {
                            return Mono.empty();
                        }
                        logger.error("Failed to create item on retry: {}", error.getMessage());
                        return Mono.empty();
                    });
            }, 100)
            .blockLast(Duration.ofMinutes(10));

        logger.info("Finished retrying failed bulk operations");
    }
}
