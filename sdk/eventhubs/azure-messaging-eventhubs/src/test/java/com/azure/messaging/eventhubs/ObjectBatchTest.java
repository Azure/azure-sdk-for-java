// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.experimental.serializer.ObjectSerializer;
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
                    public <T> Mono<T> deserialize(InputStream stream, Class<T> clazz) {
                        return null;
                    }

                    @Override
                    public <S extends OutputStream> Mono<S> serialize(S stream, Object value) {
                        return null;
                    }
                },
                null,
                null);
            batch.tryAdd((Integer) null).block();
        });
    }
}
