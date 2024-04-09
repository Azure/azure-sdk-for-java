// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.encryption.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.query.Transformer;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CosmosEncryptionQueryTransformer<T> implements Transformer<T> {
    private final Scheduler encryptionScheduler;
    private final EncryptionProcessor encryptionProcessor;
    private final ItemDeserializer itemDeserializer;
    private final Class<T> classType;
    private final boolean isChangeFeed;

    public CosmosEncryptionQueryTransformer(
        Scheduler encryptionScheduler,
        EncryptionProcessor encryptionProcessor,
        ItemDeserializer itemDeserializer,
        Class<T> classType,
        Boolean isChangeFeed) {
        this.encryptionScheduler = encryptionScheduler;
        this.encryptionProcessor = encryptionProcessor;
        this.itemDeserializer = itemDeserializer;
        this.classType = classType;
        this.isChangeFeed = isChangeFeed;
    }

    @Override
    public Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> transform(Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func) {
        return queryDecryptionTransformer(this.classType, this.isChangeFeed, func);
    }

    private <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryDecryptionTransformer(
        Class<T> classType,
        boolean isChangeFeed,
        Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func) {
        return func.andThen(flux ->
            flux.publishOn(encryptionScheduler)
                .flatMap(
                    page -> {
                        boolean useEtagAsContinuation = isChangeFeed;
                        boolean isNoChangesResponse = isChangeFeed ?
                            ModelBridgeInternal.getNoChangesFromFeedResponse(page)
                            : false;
                        List<Mono<JsonNode>> jsonNodeArrayMonoList =
                            page.getResults().stream().map(jsonNode -> decryptResponseNode(jsonNode)).collect(Collectors.toList());
                        return Flux.concat(jsonNodeArrayMonoList).map(
                            item -> this.itemDeserializer.convert(classType, item)
                        ).collectList().map(itemList -> BridgeInternal.createFeedResponseWithQueryMetrics(itemList,
                            page.getResponseHeaders(),
                            BridgeInternal.queryMetricsFromFeedResponse(page),
                            ModelBridgeInternal.getQueryPlanDiagnosticsContext(page),
                            useEtagAsContinuation,
                            isNoChangesResponse,
                            page.getCosmosDiagnostics())
                        );
                    }
                )
        );
    }

    Mono<JsonNode> decryptResponseNode(
        JsonNode jsonNode) {

        if (jsonNode == null) {
            return Mono.empty();
        }

        return this.encryptionProcessor.decryptJsonNode(
            jsonNode);
    }
}
