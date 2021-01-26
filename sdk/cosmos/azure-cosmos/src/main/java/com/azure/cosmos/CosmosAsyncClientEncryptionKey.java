// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.withContext;

public class CosmosAsyncClientEncryptionKey {
    private final CosmosAsyncDatabase database;
    private String id;

    CosmosAsyncClientEncryptionKey(String id, CosmosAsyncDatabase database) {
        this.id = id;
        this.database = database;
    }

    /**
     * Get the id of the {@link CosmosAsyncUser}
     *
     * @return the id of the {@link CosmosAsyncUser}
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncUser}
     *
     * @param id the id of the {@link CosmosAsyncUser}
     * @return the same {@link CosmosAsyncUser} that had the id set
     */
    CosmosAsyncClientEncryptionKey setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Reads a cosmos client encryption key
     *
     * @return a {@link Mono} containing the single resource response with the read client encryption key or an error.
     */
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
     * Replace a cosmos client encryption key
     *
     * @return a {@link Mono} containing the single resource response with the read client encryption key or an error.
     */
    public Mono<CosmosClientEncryptionKeyResponse> replace(CosmosClientEncryptionKeyProperties keyProperties) {
        return withContext(context -> replaceInternal(keyProperties, context));
    }

    private Mono<CosmosClientEncryptionKeyResponse> replaceInternal(CosmosClientEncryptionKeyProperties keyProperties
        , Context context) {
        String spanName = "replaceClientEncryptionKey." + getId();
        Mono<CosmosClientEncryptionKeyResponse> responseMono = this.database.getDocClientWrapper()
            .replaceClientEncryptionKey(ModelBridgeInternal.getClientEncryptionPolicys(keyProperties), getLink(), null)
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
