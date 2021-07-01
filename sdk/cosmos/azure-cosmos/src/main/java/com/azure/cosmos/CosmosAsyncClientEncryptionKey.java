// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.util.Beta;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * The type Cosmos async clientEncryptionKey. This contains methods to operate on a cosmos clientEncryptionKey asynchronously
 */
@Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosAsyncClientEncryptionKey {
    private final CosmosAsyncDatabase database;
    private String id;

    CosmosAsyncClientEncryptionKey(String id, CosmosAsyncDatabase database) {
        this.id = id;
        this.database = database;
    }

    /**
     * Get the id of the {@link CosmosAsyncClientEncryptionKey}
     *
     * @return the id of the {@link CosmosAsyncClientEncryptionKey}
     */
    @Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getId() {
        return id;
    }

    /**
     * Reads a cosmos client encryption key
     *
     * @return a {@link Mono} containing the single resource response with the read client encryption key or an error.
     */
    @Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Mono<CosmosClientEncryptionKeyResponse> read() {
        return withContext(context -> readInternal(context));
    }

    private Mono<CosmosClientEncryptionKeyResponse> readInternal(Context context) {
        String spanName = "readClientEncryptionKey." + getId();
        Mono<CosmosClientEncryptionKeyResponse> responseMono = this.database.getDocClientWrapper()
            .readClientEncryptionKey(getLink(), null)
            .map(response -> ModelBridgeInternal.createCosmosClientEncryptionKeyResponse(response)).single();
        return database.getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    /**
     * Replace a cosmos client encryption key.
     * This method is not meant to be invoked directly. It is used within CosmosEncryptionAsyncDatabase.rewrapClientEncryptionKey
     * @param keyProperties the client encryption key properties to create.
     * @return a {@link Mono} containing the single resource response with the read client encryption key or an error.
     */
    @Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Mono<CosmosClientEncryptionKeyResponse> replace(CosmosClientEncryptionKeyProperties keyProperties) {
        return withContext(context -> replaceInternal(keyProperties, context));
    }

    private Mono<CosmosClientEncryptionKeyResponse> replaceInternal(CosmosClientEncryptionKeyProperties keyProperties
        , Context context) {
        String spanName = "replaceClientEncryptionKey." + getId();
        Mono<CosmosClientEncryptionKeyResponse> responseMono = this.database.getDocClientWrapper()
            .replaceClientEncryptionKey(ModelBridgeInternal.getClientEncryptionKey(keyProperties), getLink(), null)
            .map(response -> ModelBridgeInternal.createCosmosClientEncryptionKeyResponse(response)).single();
        return database.getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName,
            database.getId(),
            database.getClient().getServiceEndpoint());
    }

    String getURIPathSegment() {
        return Paths.CLIENT_ENCRYPTION_KEY_PATH_SEGMENT;
    }

    String getParentLink() {
        return database.getLink();
    }

    String getLink() {
        StringBuilder builder = new StringBuilder();
        builder.append(getParentLink());
        builder.append("/");
        builder.append(getURIPathSegment());
        builder.append("/");
        builder.append(getId());
        return builder.toString();
    }
}
