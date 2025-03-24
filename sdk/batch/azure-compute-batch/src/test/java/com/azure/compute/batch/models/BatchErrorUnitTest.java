// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BatchErrorUnitTest {

    @Test
    public void testFromExceptionWithValidJson() throws IOException {
        // Arrange
        String json = "{\"code\":\"InvalidRequest\",\"message\":{\"value\":\"Invalid request\"},\"values\":[]}";
        HttpResponse response = mock(HttpResponse.class);
        when(response.getBodyAsString()).thenReturn(Mono.just(json));
        HttpResponseException exception = new HttpResponseException("Error", response);

        // Act
        BatchError batchError = BatchError.fromException(exception);

        // Assert
        assertNotNull(batchError);
        assertEquals("InvalidRequest", batchError.getCode());
        assertNotNull(batchError.getMessage());
        assertEquals("Invalid request", batchError.getMessage().getValue());
        assertTrue(batchError.getValues().isEmpty());
    }

    @Test
    public void testFromExceptionWithValidJsonMultiValues() throws IOException {
        // Arrange
        String json
            = "{\"code\":\"InvalidRequest\",\"message\":{\"value\":\"Error message\"},\"values\":[{\"key\": \"key1\",\"value\":\"value1\"},{\"key\": \"key2\",\"value\":\"value2\"}]}";
        HttpResponse response = mock(HttpResponse.class);
        when(response.getBodyAsString()).thenReturn(Mono.just(json));
        HttpResponseException exception = new HttpResponseException("Error", response);

        // Act
        BatchError batchError = BatchError.fromException(exception);

        // Assert
        assertNotNull(batchError);
        assertEquals("InvalidRequest", batchError.getCode());
        assertNotNull(batchError.getMessage());
        assertEquals("Error message", batchError.getMessage().getValue());
        assertNotNull(batchError.getValues());
        assertEquals("key1", batchError.getValues().get(0).getKey());
        assertEquals("value1", batchError.getValues().get(0).getValue());
        assertEquals("key2", batchError.getValues().get(1).getKey());
        assertEquals("value2", batchError.getValues().get(1).getValue());
    }

    @Test
    public void testFromExceptionWithInvalidJson() {
        // Arrange
        String invalidJson = "Invalid JSON";
        HttpResponse response = mock(HttpResponse.class);
        when(response.getBodyAsString()).thenReturn(Mono.just(invalidJson));
        HttpResponseException exception = new HttpResponseException("Error", response);

        // Act
        BatchError batchError = BatchError.fromException(exception);

        // Assert
        assertNull(batchError);
    }

    @Test
    public void testFromExceptionWithNullResponse() {
        // Arrange
        HttpResponse response = mock(HttpResponse.class);
        when(response.getBodyAsString()).thenReturn(Mono.empty());
        HttpResponseException exception = new HttpResponseException("Error", response);

        // Act
        BatchError batchError = BatchError.fromException(exception);

        // Assert
        assertNull(batchError);
    }
}
