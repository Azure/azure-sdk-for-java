// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKey;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKeyProvider;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.models.CosmosContainerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.List;

/**
 *  Default implementation for a provider to get a data encryption key - wrapped keys are stored in a Cosmos DB container.
 *  See https://aka.ms/CosmosClientEncryption for more information on client-side encryption support in Azure Cosmos DB.
 */
public class CosmosDataEncryptionKeyProvider implements DataEncryptionKeyProvider {
    // TODO: proper sample and documentation on container
    private static final String ContainerPartitionKeyPath = "/id";
    // TODO: moderakh
    // Is it a requirement on container definition?
    //Then the code-docs and samples needs to explicit cover it.
    private final DataEncryptionKeyContainerCore dataEncryptionKeyContainerCore;
    private final DekCache DekCache;
    private final EncryptionKeyWrapProvider EncryptionKeyWrapProvider;
    private CosmosAsyncContainer container;

    public CosmosDataEncryptionKeyProvider(EncryptionKeyWrapProvider encryptionKeyWrapProvider) {
        this(encryptionKeyWrapProvider, null);
    }

    /**
     * Initializes a new instance of the {@link CosmosDataEncryptionKeyProvider}
     * @param encryptionKeyWrapProvider A provider that will be used to wrap (encrypt) and unwrap (decrypt) data encryption keys for envelope based encryption
     * @param dekPropertiesTimeToLive Time to live for DEK properties before having to refresh.
     */
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

    /**
     * Gets a provider that will be used to wrap (encrypt) and unwrap (decrypt) data encryption keys for envelope based encryption.
     * @return EncryptionKeyWrapProvider
     */
    EncryptionKeyWrapProvider getEncryptionKeyWrapProvider() {
        return EncryptionKeyWrapProvider;
    }

    /**
     * Gets Container for data encryption keys.
     * @return DataEncryptionKeyContainer
     */
    DataEncryptionKeyContainer getDataEncryptionKeyContainer() {
        return dataEncryptionKeyContainerCore;
    }

    DekCache getDekCache() {
        return DekCache;
    }

    // TODO: @moderakh look into if this method needs to be async.
    /**
     * Initialize Cosmos DB container for CosmosDataEncryptionKeyProvider to store wrapped DEKs
     * @param database Database
     * @param containerId ontainer id
     */
    void initialize(CosmosAsyncDatabase database,
                    String containerId) {
        Preconditions.checkNotNull(database, "database");
        Preconditions.checkNotNull(containerId, "containerId");

        Preconditions.checkState(this.container == null, "CosmosDataEncryptionKeyProvider has already been initialized.");
        Preconditions.checkNotNull(database, "database is null");

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
            .dataEncryptionKeyContainerCore.fetchUnwrappedAsync(id);

        return fetchUnwrapMono
            .map(fetchUnwrap -> fetchUnwrap.getT2().getDataEncryptionKey())
            .block(); // TODO: @moderakh I will be looking at if we should do this API async or non async.
    }
}
