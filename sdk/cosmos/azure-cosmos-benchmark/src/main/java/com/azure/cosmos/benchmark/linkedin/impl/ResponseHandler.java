// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.linkedin.impl.exceptions.CosmosDBDataAccessorException;
import com.azure.cosmos.benchmark.linkedin.impl.keyextractor.KeyExtractor;
import com.azure.cosmos.benchmark.linkedin.impl.models.BatchGetResult;
import com.azure.cosmos.benchmark.linkedin.impl.models.Entity;
import com.azure.cosmos.benchmark.linkedin.impl.models.EntityAttributes;
import com.azure.cosmos.benchmark.linkedin.impl.models.Result;
import com.azure.cosmos.benchmark.linkedin.impl.models.ResultMetadata;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AtomicDouble;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import static com.azure.cosmos.benchmark.linkedin.impl.Constants.DELETED_INDICATOR;


/**
 * Class for mapping the response from the CosmosDB SQL API to the internal representation used by the application
 *
 * @param <K> The key for the entity stored in the data store
 * @param <V> The entity stored in the data store
 */
@ThreadSafe
public class ResponseHandler<K, V> {

    // Transformer that can convert the Entity to a CosmosDB representation and back.
    private final DocumentTransformer<V, JsonNode> _transformer;
    private final KeyExtractor<K> _keyExtractor;

    public ResponseHandler(final DocumentTransformer<V, JsonNode> transformer, final KeyExtractor<K> keyExtractor) {
        _transformer = Preconditions.checkNotNull(transformer,
            "The DocumentTransformer is required for mapping the Document stored in CosmosDB to the"
                + "Entity representation used by the application");
        _keyExtractor = Preconditions.checkNotNull(keyExtractor, "The CosmosDBKeyExtractorV3 is null!");
    }

    Result<K, V> convertResponse(@Nonnull final K key, @Nullable final CosmosItemResponse<?> response,
        final boolean includeTombstone) {
        Preconditions.checkNotNull(key, "The key requested is null!");

        final Optional<? extends CosmosItemResponse<?>> maybeResponse = Optional.ofNullable(response);
        final ObjectNode document = (ObjectNode) maybeResponse.map(CosmosItemResponse::getItem)
            .filter(item -> item instanceof ObjectNode)
            .orElse(null);
        double requestCharges = maybeResponse.map(CosmosItemResponse::getRequestCharge)
            .orElse(0.0);

        // Initialize it assuming there is no result returned. If we received a
        // valid result from CosmosDB, we will update the entry in the result.
        final Result.Builder<K, V> resultBuilder = new Result.Builder<K, V>()
            .setResult(key, null);

        // Process the entity in the response, and set it in the result.
        convertDocument(document, includeTombstone)
            .ifPresent(entity -> resultBuilder.setResult(key, entity));

        // Process the response metadata, and set it in the result.
        final ResultMetadata.Builder metadata = new ResultMetadata.Builder()
            .setCostUnits(requestCharges);
        return resultBuilder.setMetadata(metadata.build())
            .build();
    }

    BatchGetResult<K, V> convertFeedResponse(final List<FeedResponse<ObjectNode>> responseList) {
        Preconditions.checkNotNull(responseList, "The responseList from the CosmosDB SQL API is null!");
        return convertFeedResponse(responseList, key -> false);
    }

    Result<K, V> convertException(@Nonnull final K key, @Nonnull final CosmosException exception) {
        Preconditions.checkNotNull(exception,
            "Only a non-null CosmosException can be mapped to " + "the Result object representation.");

        final ResultMetadata.Builder metadata = new ResultMetadata.Builder();
        extractCostUnits(exception).ifPresent(metadata::setCostUnits);
        final Result.Builder<K, V> response =
            new Result.Builder<K, V>().setMetadata(metadata.build()).setResult(key, null);

        return response.build();
    }

    CosmosDBDataAccessorException createException(@Nonnull final String message,
        @Nonnull final CosmosException exception) {
        Preconditions.checkNotNull(message, "The error message describing the response is null!");
        Preconditions.checkNotNull(exception,
            "Only a non-null CosmosException can be mapped to " + "the CosmosDBDataAccessorException");

        final Optional<Double> maybeCostUnits = extractCostUnits(exception);
        final CosmosDBDataAccessorException.Builder builder =
            new CosmosDBDataAccessorException.Builder().setMessage(message)
                .setStatusCode(exception.getStatusCode())
                .setRetryWaitTime(exception.getRetryAfterDuration())
                .setCostUnits(maybeCostUnits.orElse(0.0d))
                .setCause(exception);

        // exception.message() returns the activityId.
        Optional.ofNullable(exception.getMessage()).ifPresent(builder::setActivityId);
        return builder.build();
    }

    private BatchGetResult<K, V> convertFeedResponse(@Nonnull final List<FeedResponse<ObjectNode>> responseList,
        @Nonnull Predicate<K> includeTombstones) {

        final AtomicDouble requestCharge = new AtomicDouble(0);
        final BatchGetResult.Builder<K, V> resultBuilder = new BatchGetResult.Builder<>();
        for (FeedResponse<ObjectNode> response : responseList) {
            requestCharge.addAndGet(response.getRequestCharge());
            response.getResults()
                .forEach(objectNode -> Optional.ofNullable(objectNode)
                    .map(_keyExtractor::getKey)
                    // Set the result only if the entity is present after the tombstone filtering
                    .ifPresent(key -> convertDocument(objectNode, includeTombstones.test(key))
                        .ifPresent(entity -> resultBuilder.addResult(key, entity))));
        }

        // Capture all the meters corresponding to this request
        final ResultMetadata.Builder metadata = new ResultMetadata.Builder()
            .addCostUnits(requestCharge.get());
        return resultBuilder.setMetadata(metadata.build())
            .build();
    }

    private Optional<Entity<V>> convertDocument(@Nullable final ObjectNode document,
        final boolean includeTombstone) {

        final Optional<ObjectNode> maybeDocument = Optional.ofNullable(document);
        return maybeDocument.map(_transformer::deserialize)
            .map(entity -> {
                final boolean isTombstoned = maybeDocument.map(doc -> doc.get(DELETED_INDICATOR)).isPresent();
                if (!includeTombstone && isTombstoned) {
                    return null;
                }

                final EntityAttributes.Builder entityAttributes = new EntityAttributes.Builder();
                maybeDocument.map(doc -> doc.get(Constants.Properties.E_TAG))
                    .map(JsonNode::asText)
                    .ifPresent(entityAttributes::setEtag);
                maybeDocument.map(doc -> doc.get(Constants.Properties.LAST_MODIFIED))
                    .map(JsonNode::asLong)
                    .ifPresent(entityAttributes::setTs);
                maybeDocument.map(doc -> doc.get(Constants.Properties.TTL))
                    .map(JsonNode::asInt)
                    .map(Duration::ofSeconds)
                    .ifPresent(entityAttributes::setTtl);

                if (isTombstoned) {
                    // MOD: The deleted indicator is not removed here
                    entityAttributes.setIsTombstoned(true);
                }

                return new Entity<>(entity, entityAttributes.build());
            });
    }

    private Optional<Double> extractCostUnits(@Nonnull final CosmosException exception) {
        return Optional.of(exception)
            .map(CosmosException::getResponseHeaders)
            .map(headers -> headers.get(HttpConstants.HttpHeaders.REQUEST_CHARGE))
            .map(Double::valueOf);
    }
}
