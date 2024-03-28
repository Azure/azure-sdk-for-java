// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.encryption.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.ObjectNodeMap;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.query.Transformer;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosEncryptionQueryTransformer<T> implements Transformer<T> {
    private final Scheduler encryptionScheduler;
    private final EncryptionProcessor encryptionProcessor;
    private final CosmosItemSerializer effectiveItemSerializer;
    private final Class<T> classType;
    private final boolean isChangeFeed;

    public CosmosEncryptionQueryTransformer(
        Scheduler encryptionScheduler,
        EncryptionProcessor encryptionProcessor,
        CosmosItemSerializer effectiveItemSerializer,
        Class<T> classType,
        Boolean isChangeFeed) {

        checkNotNull(effectiveItemSerializer, "Argument 'effectiveItemSerializer' must not be null.");
        this.encryptionScheduler = encryptionScheduler;
        this.encryptionProcessor = encryptionProcessor;
        this.effectiveItemSerializer = effectiveItemSerializer;
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
                            item -> {
                                if (item instanceof ObjectNode) {
                                    return this.effectiveItemSerializer.deserialize(new ObjectNodeMap((ObjectNode) item), classType);
                                }

                                return Utils.getSimpleObjectMapper().convertValue(item, classType);
                            }
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
