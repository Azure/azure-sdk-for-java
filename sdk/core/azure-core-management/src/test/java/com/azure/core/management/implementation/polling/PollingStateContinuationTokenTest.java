// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerAdapter;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PollingState continuation token serialization and deserialization.
 */
@SuppressWarnings("deprecation")
public class PollingStateContinuationTokenTest {

    private static final SerializerAdapter SERIALIZER = SerializerFactory.createDefaultManagementSerializerAdapter();

    @Test
    public void testToContinuationTokenAndFromContinuationToken() throws Exception {
        // Arrange - create a PollingState
        URL lroUrl = new URL(
            "https://management.azure.com/subscriptions/sub1/resourceGroups/rg1/providers/Microsoft.Compute/virtualMachines/vm1");
        HttpRequest request = new HttpRequest(HttpMethod.PUT, lroUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Azure-AsyncOperation",
            "https://management.azure.com/subscriptions/sub1/providers/Microsoft.Compute/locations/westus/operations/op1");
        headers.set("Retry-After", "5");

        String responseBody = "{\"id\":\"vm1\",\"name\":\"myVM\",\"properties\":{\"provisioningState\":\"Creating\"}}";

        PollingState originalState = PollingState.create(SERIALIZER, request, 201, headers, responseBody);

        // Act - serialize to token
        String token = originalState.toContinuationToken();

        // Assert - token should not be null or empty
        assertNotNull(token, "Continuation token should not be null");
        assertFalse(token.isEmpty(), "Continuation token should not be empty");

        // Act - deserialize from token
        PollingState deserializedState = PollingState.fromContinuationToken(token, SERIALIZER);

        // Assert - verify the deserialized state matches the original
        assertNotNull(deserializedState, "Deserialized state should not be null");
        assertEquals(originalState.getOperationStatus(), deserializedState.getOperationStatus(),
            "Operation status should match");
        assertNotNull(deserializedState.getPollDelay(), "Poll delay should not be null");
        assertEquals(Duration.ofSeconds(5), deserializedState.getPollDelay(), "Poll delay should be 5 seconds");
    }

    @Test
    public void testContinuationTokenWithNullValue() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            PollingState.fromContinuationToken(null, SERIALIZER);
        }, "Should throw NullPointerException for null token");
    }

    @Test
    public void testContinuationTokenWithEmptyValue() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            PollingState.fromContinuationToken("", SERIALIZER);
        }, "Should throw exception for empty token");

        assertTrue(exception.getMessage().contains("cannot be empty")
            || exception.getCause() instanceof IllegalArgumentException, "Exception should indicate empty token");
    }

    @Test
    public void testContinuationTokenWithInvalidBase64() {
        // Arrange - create an invalid base64 string
        String invalidToken = "This is not valid base64!@#$%";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            PollingState.fromContinuationToken(invalidToken, SERIALIZER);
        }, "Should throw exception for invalid base64 token");
    }

    @Test
    public void testContinuationTokenWithInvalidJson() {
        // Arrange - create a valid base64 string but invalid JSON content
        String invalidJsonToken = java.util.Base64.getEncoder()
            .encodeToString("{this is not valid json".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            PollingState.fromContinuationToken(invalidJsonToken, SERIALIZER);
        }, "Should throw exception for invalid JSON in token");
    }

    @Test
    public void testContinuationTokenPreservesPollingUrl() throws Exception {
        // Arrange
        URL lroUrl = new URL(
            "https://management.azure.com/subscriptions/sub1/resourceGroups/rg1/providers/Microsoft.MySql/servers/server1");
        HttpRequest request = new HttpRequest(HttpMethod.PUT, lroUrl);
        HttpHeaders headers = new HttpHeaders();
        String asyncOpUrl
            = "https://management.azure.com/subscriptions/sub1/providers/Microsoft.MySql/locations/westus/operationStatuses/op123";
        headers.set("Azure-AsyncOperation", asyncOpUrl);

        String responseBody = "{\"id\":\"server1\",\"properties\":{\"provisioningState\":\"Provisioning\"}}";

        PollingState originalState = PollingState.create(SERIALIZER, request, 201, headers, responseBody);

        // Act
        String token = originalState.toContinuationToken();
        PollingState deserializedState = PollingState.fromContinuationToken(token, SERIALIZER);

        // Assert - verify polling URL is preserved
        assertEquals(new URL(asyncOpUrl), deserializedState.getPollUrl(),
            "Polling URL should be preserved in the continuation token");
    }

    @Test
    public void testContinuationTokenWithLocationHeader() throws Exception {
        // Arrange
        URL lroUrl = new URL(
            "https://management.azure.com/subscriptions/sub1/resourceGroups/rg1/providers/Microsoft.Compute/disks/disk1");
        HttpRequest request = new HttpRequest(HttpMethod.DELETE, lroUrl);
        HttpHeaders headers = new HttpHeaders();
        String locationUrl
            = "https://management.azure.com/subscriptions/sub1/providers/Microsoft.Compute/locations/westus/operationStatuses/op456";
        headers.set("Location", locationUrl);

        PollingState originalState = PollingState.create(SERIALIZER, request, 202, headers, null);

        // Act
        String token = originalState.toContinuationToken();
        PollingState deserializedState = PollingState.fromContinuationToken(token, SERIALIZER);

        // Assert
        assertEquals(new URL(locationUrl), deserializedState.getPollUrl(),
            "Location URL should be preserved in the continuation token");
    }

    @Test
    public void testContinuationTokenRoundTrip() throws Exception {
        // Arrange - create, poll, then serialize
        URL lroUrl = new URL(
            "https://management.azure.com/subscriptions/sub1/resourceGroups/rg1/providers/Microsoft.Storage/storageAccounts/sa1");
        HttpRequest request = new HttpRequest(HttpMethod.PUT, lroUrl);
        HttpHeaders initialHeaders = new HttpHeaders();
        initialHeaders.set("Azure-AsyncOperation",
            "https://management.azure.com/subscriptions/sub1/providers/Microsoft.Storage/locations/westus/asyncOperations/op789");

        String initialBody = "{\"id\":\"sa1\",\"properties\":{\"provisioningState\":\"Creating\"}}";

        PollingState state = PollingState.create(SERIALIZER, request, 201, initialHeaders, initialBody);

        // Simulate a poll update
        HttpHeaders pollHeaders = new HttpHeaders();
        pollHeaders.set("Retry-After", "10");
        String pollBody = "{\"status\":\"InProgress\"}";
        state.update(200, pollHeaders, pollBody);

        // Act - serialize and deserialize
        String token1 = state.toContinuationToken();
        PollingState state2 = PollingState.fromContinuationToken(token1, SERIALIZER);
        String token2 = state2.toContinuationToken();

        // Assert - tokens should be equivalent (both represent the same state)
        PollingState state3 = PollingState.fromContinuationToken(token2, SERIALIZER);
        assertEquals(state2.getOperationStatus(), state3.getOperationStatus(),
            "Round-trip serialization should preserve operation status");
        assertEquals(state2.getPollDelay(), state3.getPollDelay(),
            "Round-trip serialization should preserve poll delay");
    }

    @Test
    public void testContinuationTokenWithNullSerializer() throws Exception {
        // Arrange
        URL lroUrl = new URL("https://management.azure.com/subscriptions/sub1/test");
        HttpRequest request = new HttpRequest(HttpMethod.PUT, lroUrl);
        HttpHeaders headers = new HttpHeaders();

        PollingState state = PollingState.create(SERIALIZER, request, 200, headers, "{}");
        String token = state.toContinuationToken();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            PollingState.fromContinuationToken(token, null);
        }, "Should throw NullPointerException for null serializer");
    }
}
