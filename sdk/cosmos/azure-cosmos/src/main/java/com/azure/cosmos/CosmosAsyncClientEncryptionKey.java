// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * The type Cosmos async clientEncryptionKey. This contains methods to operate on a cosmos clientEncryptionKey asynchronously
 */
public final class CosmosAsyncClientEncryptionKey {
    private final CosmosAsyncDatabase database;
    private final String id;

    CosmosAsyncClientEncryptionKey(String id, CosmosAsyncDatabase database) {
        this.id = id;
        this.database = database;
    }

    /**
     * Get the id of the {@link CosmosAsyncClientEncryptionKey}
     *
     * @return the id of the {@link CosmosAsyncClientEncryptionKey}
     */
    public String getId() {
        return id;
    }

    /**
     * Reads a cosmos client encryption key
     *
     * @return a {@link Mono} containing the single resource response with the read client encryption key or an error.
     */
    public Mono<CosmosClientEncryptionKeyResponse> read() {
        return withContext(context -> readInternal(context, null));
    }

    /**
     * Reads a cosmos client encryption key
     *
     * @param requestOptions  the request options.
     * @return a {@link Mono} containing the single resource response with the read client encryption key or an error.
     */
    Mono<CosmosClientEncryptionKeyResponse> read(RequestOptions requestOptions) {
        return withContext(context -> readInternal(context, requestOptions));
    }

    private Mono<CosmosClientEncryptionKeyResponse> readInternal(Context context, RequestOptions requestOptions) {
        String spanName = "readClientEncryptionKey." + getId();
        Mono<CosmosClientEncryptionKeyResponse> responseMono =
            this.database
                .getDocClientWrapper()
                .readClientEncryptionKey(getLink(), requestOptions)
                .map(ModelBridgeInternal::createCosmosClientEncryptionKeyResponse).single();

        CosmosAsyncClient client = database
            .getClient();

        return database
            .getClient()
            .getDiagnosticsProvider()
            .traceEnabledCosmosResponsePublisher(
            responseMono,
            context,
            spanName,
            database.getId(),
            null,
            client,
            null,
            OperationType.Read,
            ResourceType.ClientEncryptionKey,
            client.getEffectiveDiagnosticsThresholds(
                requestOptions != null ? requestOptions.getDiagnosticsThresholds() : null));
    }

    /**
     * Replace a cosmos client encryption key.
     * This method is not meant to be invoked directly. It is used within CosmosEncryptionAsyncDatabase.rewrapClientEncryptionKey
     * @param keyProperties the client encryption key properties to create.
     * @return a {@link Mono} containing the single resource response with the read client encryption key or an error.
     */
    public Mono<CosmosClientEncryptionKeyResponse> replace(CosmosClientEncryptionKeyProperties keyProperties) {
        return withContext(context -> replaceInternal(keyProperties, context));
    }

    private Mono<CosmosClientEncryptionKeyResponse> replaceInternal(
        CosmosClientEncryptionKeyProperties keyProperties,
        Context context) {

        String spanName = "replaceClientEncryptionKey." + getId();
        Mono<CosmosClientEncryptionKeyResponse> responseMono =
            this.database
                .getDocClientWrapper()
                .replaceClientEncryptionKey(
                    ModelBridgeInternal.getClientEncryptionKey(keyProperties), getLink(), null)
                .map(ModelBridgeInternal::createCosmosClientEncryptionKeyResponse).single();

        CosmosAsyncClient client = database
            .getClient();

        return database.getClient().getDiagnosticsProvider().traceEnabledCosmosResponsePublisher(
            responseMono,
            context,
            spanName,
            database.getId(),
            null,
            client,
            null,
            OperationType.Replace,
            ResourceType.ClientEncryptionKey,
            client.getEffectiveDiagnosticsThresholds(null));
    }

    String getURIPathSegment() {
        return Paths.CLIENT_ENCRYPTION_KEY_PATH_SEGMENT;
    }

    String getParentLink() {
        return database.getLink();
    }

    String getLink() {
        return getParentLink()
            + "/"
            + getURIPathSegment()
            + "/"
            + getId();
    }

    static void initialize() {
        ImplementationBridgeHelpers.CosmosAsyncClientEncryptionKeyHelper.setCosmosAsyncClientEncryptionKeyAccessor(
            CosmosAsyncClientEncryptionKey::read);
    }

    static { initialize(); }
}
