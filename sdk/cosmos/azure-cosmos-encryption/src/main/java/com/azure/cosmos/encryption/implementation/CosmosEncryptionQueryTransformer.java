// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.encryption.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.ObjectNodeMap;
import com.azure.cosmos.implementation.PrimitiveJsonNodeMap;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosEncryptionQueryTransformer<T> implements Transformer<T> {
    private final static ImplementationBridgeHelpers.CosmosItemSerializerHelper.CosmosItemSerializerAccessor itemSerializerAccessor =
        ImplementationBridgeHelpers.CosmosItemSerializerHelper.getCosmosItemSerializerAccessor();
    private final Scheduler encryptionScheduler;
    private final EncryptionProcessor encryptionProcessor;
    private final Class<T> classType;
    private final boolean isChangeFeed;

    public CosmosEncryptionQueryTransformer(
        Scheduler encryptionScheduler,
        EncryptionProcessor encryptionProcessor,
        Class<T> classType,
        Boolean isChangeFeed) {

        this.encryptionScheduler = encryptionScheduler;
        this.encryptionProcessor = encryptionProcessor;
        this.classType = classType;
        this.isChangeFeed = isChangeFeed;
    }

    @Override
    public Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> transform(
        Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func,
        CosmosItemSerializer effectiveSerializer) {

        return queryDecryptionTransformer(this.classType, this.isChangeFeed, func, effectiveSerializer);
    }

    private <TTransform> Function<CosmosPagedFluxOptions, Flux<FeedResponse<TTransform>>> queryDecryptionTransformer(
        Class<TTransform> classType,
        boolean isChangeFeed,
        Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func,
        CosmosItemSerializer effectiveSerializer) {
        return func.andThen(flux ->
            flux.publishOn(encryptionScheduler)
                .flatMap(
                    page -> {
                        boolean useEtagAsContinuation = isChangeFeed;
                        boolean isNoChangesResponse = isChangeFeed ?
                            ModelBridgeInternal.getNoChangesFromFeedResponse(page)
                            : false;
                        List<Mono<JsonNode>> jsonNodeArrayMonoList =
                            page.getResults().stream().map(jsonNode -> decryptResponseNode(jsonNode))
                                .collect(Collectors.toList());
                        return Flux.concat(jsonNodeArrayMonoList).map(
                            item -> {
                                Map<String, Object> decryptedJsonTree;

                                if (item instanceof ObjectNode) {
                                    decryptedJsonTree = new ObjectNodeMap((ObjectNode)item);
                                } else if (item.isValueNode()) {
                                    decryptedJsonTree = new PrimitiveJsonNodeMap(item);
                                } else {
                                    return Utils.getSimpleObjectMapper()
                                                             .convertValue(
                                                                 item,
                                                                 classType);
                                }

                                return itemSerializerAccessor.deserializeSafe(
                                    effectiveSerializer,
                                    decryptedJsonTree,
                                    classType);
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
