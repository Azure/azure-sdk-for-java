// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.encryption.api.CosmosEncryptionAlgorithm;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKey;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.util.Arrays;

class DataEncryptionKeyContainerCore implements DataEncryptionKeyContainer {
    private final CosmosDataEncryptionKeyProvider DekProvider;

    public DataEncryptionKeyContainerCore(CosmosDataEncryptionKeyProvider dekProvider) {
        this.DekProvider = dekProvider;
    }

    public Mono<CosmosItemResponse<DataEncryptionKeyProperties>> createDataEncryptionKeyAsync(String id,
                                                                                                           String encryptionAlgorithm,
                                                                                                           EncryptionKeyWrapMetadata encryptionKeyWrapMetadata,
                                                                                                           CosmosItemRequestOptions requestOptions) {

        Preconditions.checkArgument(StringUtils.isNotEmpty(id), "id is missing");
        Preconditions.checkArgument(StringUtils.equals(encryptionAlgorithm,
            CosmosEncryptionAlgorithm.AEAes256CbcHmacSha256Randomized), "Unsupported Encryption Algorithm " + encryptionAlgorithm);
        Preconditions.checkNotNull(encryptionKeyWrapMetadata, "encryptionKeyWrapMetadata is missing");

        byte[] rawDek = DataEncryptionKey.generate(encryptionAlgorithm);

        Tuple3<byte[], EncryptionKeyWrapMetadata, InMemoryRawDek> wrapResult =
            this.WrapAsync(
                id,
                rawDek,
                encryptionAlgorithm,
                encryptionKeyWrapMetadata);

        byte[] wrappedDek = wrapResult.getLeft();
        EncryptionKeyWrapMetadata updatedMetadata = wrapResult.getMiddle();
        InMemoryRawDek inMemoryRawDek = wrapResult.getRight();

        DataEncryptionKeyProperties dekProperties = new DataEncryptionKeyProperties(id, encryptionAlgorithm, wrappedDek, updatedMetadata, Instant.now());

        Mono<CosmosItemResponse<DataEncryptionKeyProperties>> dekResponseMono =
            this.DekProvider.getContainer().createItem(dekProperties, new PartitionKey(dekProperties.id), requestOptions);

        return dekResponseMono.flatMap(
            dekResponse -> {

                this.DekProvider.getDekCache().setDekProperties(id, dekResponse.getItem());
                this.DekProvider.getDekCache().setRawDek(id, inMemoryRawDek);
                return Mono.just(dekResponse);
            }
        );
    }

    @Override
    public Mono<CosmosItemResponse<DataEncryptionKeyProperties>> readDataEncryptionKeyAsync(
        String id,
        CosmosItemRequestOptions requestOptions) {
        Mono<CosmosItemResponse<DataEncryptionKeyProperties>> responseMono = this.ReadInternalAsync(
            id,
            requestOptions);

        return responseMono.flatMap(
            response -> {

                this.DekProvider.getDekCache().setDekProperties(id, response.getItem());
                return Mono.just(response);
            }
        );
    }

    @Override
    public Mono<CosmosItemResponse<DataEncryptionKeyProperties>> rewrapDataEncryptionKeyAsync(
        String id,
        EncryptionKeyWrapMetadata newWrapMetadata,
        final CosmosItemRequestOptions requestOptions) {

        Preconditions.checkNotNull(newWrapMetadata, "newWrapMetadata is missing");
        Mono<Tuple2<DataEncryptionKeyProperties, InMemoryRawDek>> resultMono = this.FetchUnwrappedAsync(
            id);

        return resultMono.flatMap(
            result -> {
                DataEncryptionKeyProperties dekProperties = result.getT1();
                InMemoryRawDek inMemoryRawDek = result.getT2();

                Tuple3<byte[], EncryptionKeyWrapMetadata, InMemoryRawDek> wrapResult =
                    this.WrapAsync(
                        id,
                        inMemoryRawDek.getDataEncryptionKey().getRawKey(),
                        dekProperties.encryptionAlgorithm,
                        newWrapMetadata);

                byte[] wrappedDek = wrapResult.getLeft();
                EncryptionKeyWrapMetadata updatedMetadata = wrapResult.getMiddle();
                InMemoryRawDek updatedRawDek = wrapResult.getRight();

                CosmosItemRequestOptions effectiveRequestOptions = requestOptions != null ? requestOptions : new CosmosItemRequestOptions();

                effectiveRequestOptions.setIfMatchETag(dekProperties.eTag);

                DataEncryptionKeyProperties newDekProperties = new DataEncryptionKeyProperties(dekProperties);
                newDekProperties.wrappedDataEncryptionKey = wrappedDek;
                newDekProperties.encryptionKeyWrapMetadata = updatedMetadata;

                Mono<CosmosItemResponse<DataEncryptionKeyProperties>> responseMono = this.DekProvider.getContainer().replaceItem(
                    newDekProperties,
                    newDekProperties.id,
                    new PartitionKey(newDekProperties.id),
                    effectiveRequestOptions);

                return responseMono.flatMap(
                    response -> {
                        DataEncryptionKeyProperties item = response.getItem();

                        assert (item != null);
                        this.DekProvider.getDekCache().setDekProperties(id, item);
                        this.DekProvider.getDekCache().setRawDek(id, updatedRawDek);
                        return Mono.just(response);
                    }
                );
            });
    }

    Mono<Tuple2<DataEncryptionKeyProperties, InMemoryRawDek>> FetchUnwrappedAsync(
        String id) {
        Mono<DataEncryptionKeyProperties> dekPropertiesMono = this.DekProvider.getDekCache().getOrAddDekPropertiesAsync(
            id,
            this::ReadResourceAsync);

        return dekPropertiesMono.flatMap(
            dekProperties -> {
                Mono<InMemoryRawDek> inMemoryRawDek = this.DekProvider.getDekCache().getOrAddRawDekAsync(
                    dekProperties,
                    dp -> Mono.just(this.UnwrapAsync(dp)));

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

    Tuple3<byte[], EncryptionKeyWrapMetadata, InMemoryRawDek> WrapAsync(
        String id,
        byte[] key,
        String encryptionAlgorithm,
        EncryptionKeyWrapMetadata metadata) {
        EncryptionKeyWrapResult keyWrapResponse;

        keyWrapResponse = this.DekProvider.getEncryptionKeyWrapProvider().wrapKey(key, metadata);

        // Verify
        DataEncryptionKeyProperties tempDekProperties = new DataEncryptionKeyProperties(id, encryptionAlgorithm, keyWrapResponse.getWrappedDataEncryptionKey(), keyWrapResponse.getEncryptionKeyWrapMetadata(), Instant.now());
        InMemoryRawDek roundTripResponse = this.UnwrapAsync(tempDekProperties);
        if (!Arrays.equals(roundTripResponse.getDataEncryptionKey().getRawKey(), key)) {
            throw new IllegalStateException("The key wrapping provider configured was unable to unwrap the wrapped key correctly.");
        }

        return new Tuple3<>(keyWrapResponse.getWrappedDataEncryptionKey(), keyWrapResponse.getEncryptionKeyWrapMetadata(), roundTripResponse);
    }

    InMemoryRawDek UnwrapAsync(
        DataEncryptionKeyProperties dekProperties) {
        EncryptionKeyUnwrapResult unwrapResult;

        unwrapResult = this.DekProvider.getEncryptionKeyWrapProvider().unwrapKey(
            dekProperties.wrappedDataEncryptionKey,
            dekProperties.encryptionKeyWrapMetadata);

        DataEncryptionKey dek = DataEncryptionKey.create(unwrapResult.getDataEncryptionKey(), dekProperties.encryptionAlgorithm);
        return new InMemoryRawDek(dek, unwrapResult.getClientCacheTimeToLive());
    }

    private Mono<DataEncryptionKeyProperties> ReadResourceAsync(
        String id) {
        return this.ReadInternalAsync(
            id,
            null).map(CosmosItemResponse::getItem);
    }

    private Mono<CosmosItemResponse<DataEncryptionKeyProperties>> ReadInternalAsync(
        String id,
        CosmosItemRequestOptions requestOptions) {
        return this.DekProvider.getContainer()
            .readItem(
                id,
                new PartitionKey(id),
                requestOptions,
                DataEncryptionKeyProperties.class);
    }
}
