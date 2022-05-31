// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.implementation.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.search.implementation.models.ReverseSearchAddressBatchResult;
import com.azure.maps.search.implementation.models.SearchAddressBatchResult;
import com.azure.maps.search.models.BatchReverseSearchResult;
import com.azure.maps.search.models.BatchSearchResult;

import reactor.core.publisher.Mono;

/**
 * Jackson based implementation of the {@link JsonSerializer}.
 *
 * This implementation first deserializes the response into a private {@link SearchAddressResult}
 * then converts it to a public {@link BatchSearchResult} with the right properties and methods.
 *
 */
public final class BatchResponseSerializer implements JsonSerializer {
    private final SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
    private final ClientLogger logger = new ClientLogger(BatchResponseSerializer.class);

    /**
     * Performs deserialization from {@link SearchAddressBatchResult} or {@link ReverseSearchAddressBatchResult}
     * and conversion to {@link BatchSearchResult}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
        try {
            if (typeReference.getJavaType().getTypeName().contains("BatchSearchResult")) {
                TypeReference<SearchAddressBatchResult> interimType = new TypeReference<SearchAddressBatchResult>(){};
                SearchAddressBatchResult interimResult = jacksonAdapter
                    .<SearchAddressBatchResult>deserialize(data, interimType.getJavaType(),
                        SerializerEncoding.JSON);
                BatchSearchResult result = Utility.toBatchSearchResult(interimResult);
                if (result == null) {
                    result = new BatchSearchResult();
                }
                return (T) result;
            }
            else if (typeReference.getJavaType().getTypeName().contains("BatchReverseSearchResult")){
                TypeReference<ReverseSearchAddressBatchResult> interimType = new TypeReference<ReverseSearchAddressBatchResult>(){};
                ReverseSearchAddressBatchResult interimResult = jacksonAdapter
                    .<ReverseSearchAddressBatchResult>deserialize(data, interimType.getJavaType(),
                        SerializerEncoding.JSON);
                BatchReverseSearchResult result = Utility.toBatchReverseSearchResult(interimResult);
                if (result == null) {
                    result = new BatchReverseSearchResult();
                }
                return (T) result;
            }
            else {
                return jacksonAdapter.deserialize(data, typeReference.getJavaType(), SerializerEncoding.JSON);
            }
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        try {
            return jacksonAdapter.deserialize(stream, typeReference.getJavaType(), SerializerEncoding.JSON);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public <T> Mono<T> deserializeFromBytesAsync(byte[] data, TypeReference<T> typeReference) {
        return Mono.defer(() -> Mono.fromCallable(() -> deserializeFromBytes(data, typeReference)));
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
        return Mono.defer(() -> Mono.fromCallable(() -> deserialize(stream, typeReference)));
    }

    @Override
    public byte[] serializeToBytes(Object value) {
        try {
            return jacksonAdapter.serializeToBytes(value, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
        try {
            jacksonAdapter.serialize(value, SerializerEncoding.JSON, stream);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public Mono<byte[]> serializeToBytesAsync(Object value) {
        return Mono.defer(() -> Mono.fromCallable(() -> serializeToBytes(value)));
    }

    @Override
    public Mono<Void> serializeAsync(OutputStream stream, Object value) {
        return Mono.fromRunnable(() -> serialize(stream, value));
    }
}
