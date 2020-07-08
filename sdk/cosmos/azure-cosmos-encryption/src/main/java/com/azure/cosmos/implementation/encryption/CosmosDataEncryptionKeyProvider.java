// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKey;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKeyProvider;
import com.azure.cosmos.models.CosmosContainerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.List;

public class CosmosDataEncryptionKeyProvider implements DataEncryptionKeyProvider {
    // TODO: proper sample and documentation on container
    private static final String ContainerPartitionKeyPath = "/id";
    private final DataEncryptionKeyContainerCore dataEncryptionKeyContainerCore;
    private final DekCache DekCache;
    private final EncryptionKeyWrapProvider EncryptionKeyWrapProvider;
    private CosmosAsyncContainer container;

    public CosmosDataEncryptionKeyProvider(EncryptionKeyWrapProvider encryptionKeyWrapProvider) {
        this(encryptionKeyWrapProvider, null);
    }

    public CosmosDataEncryptionKeyProvider(EncryptionKeyWrapProvider encryptionKeyWrapProvider,
                                           Duration dekPropertiesTimeToLive) {
        this.EncryptionKeyWrapProvider = encryptionKeyWrapProvider;
        this.dataEncryptionKeyContainerCore = new DataEncryptionKeyContainerCore(this);
        this.DekCache = new DekCache(dekPropertiesTimeToLive);
    }

    CosmosAsyncContainer getContainer() {
        if (this.container != null) {
            return this.container;
        }

        throw new IllegalStateException("The CosmosDataEncryptionKeyProvider was not initialized.");
    }

    EncryptionKeyWrapProvider getEncryptionKeyWrapProvider() {
        return EncryptionKeyWrapProvider;
    }

    DataEncryptionKeyContainer getDataEncryptionKeyContainer() {
        return dataEncryptionKeyContainerCore;
    }

    DekCache getDekCache() {
        return DekCache;
    }

    // TODO: @moderakh look into if this method needs to be async.
    void initialize(CosmosAsyncDatabase database,
                           String containerId) {
        if (this.container != null) {
            throw new IllegalStateException("CosmosDataEncryptionKeyProvider has already been initialized.");
        }

        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerId, CosmosDataEncryptionKeyProvider.ContainerPartitionKeyPath).block();
        List<String> partitionKeyPath = containerResponse.getProperties().getPartitionKeyDefinition().getPaths();

        if (partitionKeyPath.size() != 1 || !StringUtils.equals(partitionKeyPath.get(0), CosmosDataEncryptionKeyProvider.ContainerPartitionKeyPath)) {
            throw new IllegalArgumentException(String.format("Provided container %s did not have the appropriate partition key definition. " +
                    "The container needs to be created with PartitionKeyPath set to %s.",
                containerId, ContainerPartitionKeyPath));
        }

        this.container = database.getContainer(containerId);
    }

    @Override
    public DataEncryptionKey getDataEncryptionKey(String id,
                                                  String encryptionAlgorithm) {
        Mono<Tuple2<DataEncryptionKeyProperties, InMemoryRawDek>> fetchUnwrapMono = this
            .dataEncryptionKeyContainerCore.FetchUnwrappedAsync(id);

        return fetchUnwrapMono
            .map(fetchUnwrap -> fetchUnwrap.getT2().getDataEncryptionKey())
            .block(); // TODO: @moderakh I will be looking at if we should do this API async or non async.
    }
}
