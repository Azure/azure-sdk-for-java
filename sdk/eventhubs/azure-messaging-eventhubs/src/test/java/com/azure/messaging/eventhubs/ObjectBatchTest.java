// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ObjectBatchTest {
    @Mock
    private ErrorContextProvider errorContextProvider;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void nullObject() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ObjectBatch<Integer> batch = new ObjectBatch<Integer>(
                1024,
                null,
                null,
                Integer.class,
                errorContextProvider,
                null,
                new ObjectSerializer() {

                    @Override
                    public <T> T deserialize(InputStream inputStream, TypeReference<T> typeReference) {
                        return null;
                    }

                    @Override
                    public <T> Mono<T> deserializeAsync(InputStream inputStream, TypeReference<T> typeReference) {
                        return null;
                    }

                    @Override
                    public void serialize(OutputStream outputStream, Object o) {

                    }

                    @Override
                    public Mono<Void> serializeAsync(OutputStream outputStream, Object o) {
                        return null;
                    }
                },
                null,
                null);
            batch.tryAdd((Integer) null);
        });
    }
}
