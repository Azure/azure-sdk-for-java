// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.EncryptionKeyUnwrapResult;
import com.azure.cosmos.encryption.EncryptionKeyWrapMetadata;
import com.azure.cosmos.encryption.EncryptionKeyWrapResult;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.encryption.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.DataEncryptionKey;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.util.Arrays;

class DataEncryptionKeyContainerCore implements DataEncryptionKeyContainer {
    private final CosmosDataEncryptionKeyProvider dekProvider;

    public DataEncryptionKeyContainerCore(CosmosDataEncryptionKeyProvider dekProvider) {
        this.dekProvider = dekProvider;
    }

    public Mono<CosmosItemResponse<DataEncryptionKeyProperties>> createDataEncryptionKey(String id,
                                                                                         String encryptionAlgorithm,
                                                                                         EncryptionKeyWrapMetadata encryptionKeyWrapMetadata,
                                                                                         CosmosItemRequestOptions requestOptions) {

        Preconditions.checkArgument(StringUtils.isNotEmpty(id), "id is missing");
        Preconditions.checkArgument(StringUtils.equals(encryptionAlgorithm,
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED), "Unsupported Encryption Algorithm " + encryptionAlgorithm);
        Preconditions.checkNotNull(encryptionKeyWrapMetadata, "encryptionKeyWrapMetadata is missing");

        byte[] rawDek = DataEncryptionKey.generate(encryptionAlgorithm);

        Mono<Tuple3<byte[], EncryptionKeyWrapMetadata, InMemoryRawDek>> wrapResultMono = this.wrap(
            id,
            rawDek,
            encryptionAlgorithm,
            encryptionKeyWrapMetadata);

        return wrapResultMono.flatMap(
            wrapResult -> {
                byte[] wrappedDek = wrapResult.getLeft();
                EncryptionKeyWrapMetadata updatedMetadata = wrapResult.getMiddle();
                InMemoryRawDek inMemoryRawDek = wrapResult.getRight();

                DataEncryptionKeyProperties dekProperties = new DataEncryptionKeyProperties(id, encryptionAlgorithm, wrappedDek, updatedMetadata, Instant.now());

                Mono<CosmosItemResponse<DataEncryptionKeyProperties>> dekResponseMono =
                    this.dekProvider.getContainer().createItem(dekProperties, new PartitionKey(dekProperties.id), requestOptions);

                return dekResponseMono.flatMap(
                    dekResponse -> {

                        this.dekProvider.getDekCache().setDekProperties(id, dekResponse.getItem());
                        this.dekProvider.getDekCache().setRawDek(id, inMemoryRawDek);
                        return Mono.just(dekResponse);
                    }
                );
            }
        );
    }

    @Override
    public Mono<CosmosItemResponse<DataEncryptionKeyProperties>> readDataEncryptionKey(
        String id,
        CosmosItemRequestOptions requestOptions) {
        Mono<CosmosItemResponse<DataEncryptionKeyProperties>> responseMono = this.readInternal(
            id,
            requestOptions);

        return responseMono.flatMap(
            response -> {

                this.dekProvider.getDekCache().setDekProperties(id, response.getItem());
                return Mono.just(response);
            }
        );
    }

    @Override
    public Mono<CosmosItemResponse<DataEncryptionKeyProperties>> rewrapDataEncryptionKey(
        String id,
        EncryptionKeyWrapMetadata newWrapMetadata,
        final CosmosItemRequestOptions requestOptions) {

        Preconditions.checkNotNull(newWrapMetadata, "newWrapMetadata is missing");
        Mono<Tuple2<DataEncryptionKeyProperties, InMemoryRawDek>> resultMono = this.fetchUnwrapped(
            id);

        return resultMono.flatMap(
            result -> {
                DataEncryptionKeyProperties dekProperties = result.getT1();
                InMemoryRawDek inMemoryRawDek = result.getT2();

                Mono<Tuple3<byte[], EncryptionKeyWrapMetadata, InMemoryRawDek>> wrapResultMono = this.wrap(
                    id,
                    inMemoryRawDek.getDataEncryptionKey().getRawKey(),
                    dekProperties.encryptionAlgorithm,
                    newWrapMetadata);

                return wrapResultMono.flatMap(
                    wrapResult -> {
                        byte[] wrappedDek = wrapResult.getLeft();
                        EncryptionKeyWrapMetadata updatedMetadata = wrapResult.getMiddle();
                        InMemoryRawDek updatedRawDek = wrapResult.getRight();

                        CosmosItemRequestOptions effectiveRequestOptions = requestOptions != null ?
                            ModelBridgeInternal.clone(requestOptions) :
                            new CosmosItemRequestOptions();
                        effectiveRequestOptions.setIfMatchETag(dekProperties.eTag);

                        DataEncryptionKeyProperties newDekProperties = new DataEncryptionKeyProperties(dekProperties);
                        newDekProperties.wrappedDataEncryptionKey = wrappedDek;
                        newDekProperties.encryptionKeyWrapMetadata = updatedMetadata;

                        Mono<CosmosItemResponse<DataEncryptionKeyProperties>> responseMono = this.dekProvider.getContainer().replaceItem(
                            newDekProperties,
                            newDekProperties.id,
                            new PartitionKey(newDekProperties.id),
                            effectiveRequestOptions);

                        return responseMono.flatMap(
                            response -> {
                                DataEncryptionKeyProperties item = response.getItem();

                                assert (item != null);
                                this.dekProvider.getDekCache().setDekProperties(id, item);
                                this.dekProvider.getDekCache().setRawDek(id, updatedRawDek);
                                return Mono.just(response);
                            }
                        );
                    }
                );
            });
    }

    Mono<Tuple2<DataEncryptionKeyProperties, InMemoryRawDek>> fetchUnwrapped(
        String id) {
        Mono<DataEncryptionKeyProperties> dekPropertiesMono = this.dekProvider.getDekCache().getOrAddDekProperties(
            id,
            this::readResource);

        return dekPropertiesMono.flatMap(
            dekProperties -> {
                Mono<InMemoryRawDek> inMemoryRawDek = this.dekProvider.getDekCache().getOrAddRawDek(
                    dekProperties,
                    dp ->this.unwrap(dp));

                return Mono.zip(Mono.just(dekProperties), inMemoryRawDek);
            }
        );
    }

    static class Tuple3<A, B, C> {
        private A a;
        private B b;
        private C c;

        public Tuple3(A a, B b, C c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public A getLeft() {
            return this.a;
        }
        public B getMiddle() {
            return this.b;
        }
        public C getRight() {
            return this.c;
        }
    }

    Mono<Tuple3<byte[], EncryptionKeyWrapMetadata, InMemoryRawDek>> wrap(
        String id,
        byte[] key,
        String encryptionAlgorithm,
        EncryptionKeyWrapMetadata metadata) {

        Mono<EncryptionKeyWrapResult> keyWrapResponseMono =
            this.dekProvider.getEncryptionKeyWrapProvider().wrapKey(key, metadata);

        return keyWrapResponseMono.flatMap(
            keyWrapResponse -> {
                // Verify
                DataEncryptionKeyProperties tempDekProperties = new DataEncryptionKeyProperties(id, encryptionAlgorithm, keyWrapResponse.getWrappedDataEncryptionKey(), keyWrapResponse.getEncryptionKeyWrapMetadata(), Instant.now());
                Mono<InMemoryRawDek> roundTripResponseMono = this.unwrap(tempDekProperties);
                return roundTripResponseMono.map(
                    roundTripResponse -> {
                        if (!Arrays.equals(roundTripResponse.getDataEncryptionKey().getRawKey(), key)) {
                            throw new IllegalStateException("The key wrapping provider configured was unable to unwrap the wrapped key correctly.");
                        }

                        return new Tuple3<>(keyWrapResponse.getWrappedDataEncryptionKey(), keyWrapResponse.getEncryptionKeyWrapMetadata(), roundTripResponse);
                    }
                );
            }
        );
    }

    Mono<InMemoryRawDek> unwrap(
        DataEncryptionKeyProperties dekProperties) {

        Mono<EncryptionKeyUnwrapResult> unwrapResultMono = this.dekProvider.getEncryptionKeyWrapProvider().unwrapKey(
            dekProperties.wrappedDataEncryptionKey,
            dekProperties.encryptionKeyWrapMetadata);

        return unwrapResultMono.map(
            unwrapResult -> {
                DataEncryptionKey dek = DataEncryptionKey.create(unwrapResult.getDataEncryptionKey(), dekProperties.encryptionAlgorithm);
                return new InMemoryRawDek(dek, unwrapResult.getClientCacheTimeToLive());
            }
        );
    }

    private Mono<DataEncryptionKeyProperties> readResource(
        String id) {
        return this.readInternal(
            id,
            null).map(CosmosItemResponse::getItem);
    }

    private Mono<CosmosItemResponse<DataEncryptionKeyProperties>> readInternal(
        String id,
        CosmosItemRequestOptions requestOptions) {
        return this.dekProvider.getContainer()
                               .readItem(
                                   id,
                                   new PartitionKey(id),
                                   requestOptions,
                                   DataEncryptionKeyProperties.class);
    }
}
