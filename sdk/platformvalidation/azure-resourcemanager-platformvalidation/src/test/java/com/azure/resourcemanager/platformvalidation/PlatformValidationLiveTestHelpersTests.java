// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.platformvalidation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

public class PlatformValidationLiveTestHelpersTests {
    @Test
    public void missingResourceRequires404() {
        PlatformValidationLiveTests.assertMissing(() -> {
            throw responseError(404);
        });
        Assertions.assertThrows(AssertionError.class, () -> PlatformValidationLiveTests.assertMissing(() -> {
            throw responseError(403);
        }));
        Assertions.assertThrows(AssertionError.class, () -> PlatformValidationLiveTests.assertMissing(() -> {
            // A successful GET must block deletion of the parent.
        }));
    }

    @Test
    public void failedOperationIsNotSuccess() {
        SyncPoller<String, String> poller = SyncPoller.createPoller(Duration.ofMillis(1),
            context -> new PollResponse<>(LongRunningOperationStatus.FAILED, "failed"),
            context -> new PollResponse<>(LongRunningOperationStatus.FAILED, "failed"),
            (context, response) -> "cancelled", context -> "not-success");
        Assertions.assertThrows(AssertionError.class, () -> PlatformValidationLiveTests.complete(poller));
    }

    @Test
    public void successfulOperationReturnsFinalResult() {
        SyncPoller<String, String> poller = SyncPoller.createPoller(Duration.ofMillis(1),
            context -> new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, "done"),
            context -> new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, "done"),
            (context, response) -> "cancelled", context -> "result");
        Assertions.assertEquals("result", PlatformValidationLiveTests.complete(poller));
    }

    @Test
    public void planConfigurationEscapesImageAndSelectsBootTest() {
        String json
            = PlatformValidationLiveTests.planConfiguration("jplan123", "https://example.invalid/image.vhd?a=\"b\"");
        Map<?, ?> document = BinaryData.fromString(json).toObject(Map.class);
        Assertions.assertEquals("ExecutionPlan", document.get("kind"));
        Assertions.assertEquals("microsoft.validate/executionPlan.v0", document.get("apiVersion"));
        Assertions.assertTrue(json.contains("compute-vmimagevalidation-vm-boot-test"));
        Assertions.assertTrue(json.contains("sourceVhdUri"));
        Assertions.assertTrue(json.contains("\\\"b\\\""));
    }

    private static HttpResponseException responseError(int status) {
        return new HttpResponseException("Test response",
            new MockHttpResponse(new HttpRequest(HttpMethod.GET, "https://management.azure.com/"), status));
    }

    @Test
    public void imageExpiryCheckedBeforeCreatingResources() {
        OffsetDateTime now = OffsetDateTime.parse("2026-10-08T00:00:00Z");
        PlatformValidationLiveTests
            .validateImageSource("https://example.invalid/image.vhd?se=2026-10-10T00%3A00%3A00Z&sig=not-a-secret", now);
        Assertions.assertThrows(AssertionError.class, () -> PlatformValidationLiveTests
            .validateImageSource("https://example.invalid/image.vhd?se=2026-10-09T01%3A00%3A00Z", now));
        Assertions.assertThrows(AssertionError.class, () -> PlatformValidationLiveTests
            .validateImageSource("https://example.invalid/image.vhd?se=2026-10-08T23%3A00%3A00Z", now));
        Assertions.assertThrows(AssertionError.class,
            () -> PlatformValidationLiveTests.validateImageSource("http://example.invalid/image.vhd", now));
    }

    @Test
    public void operationStatusTargetDropsSignedQuery() {
        Assertions.assertArrayEquals(new String[] { "eastus", "operation123" },
            PlatformValidationLiveTests.operationStatusTarget(
                "https://management.azure.com/subscriptions/example/providers/Microsoft.PlatformValidation"
                    + "/locations/eastus/operationStatuses/operation123?sig=not-a-secret"));
        Assertions.assertNull(PlatformValidationLiveTests.operationStatusTarget(null));
        Assertions.assertNull(PlatformValidationLiveTests.operationStatusTarget("https://management.azure.com/other"));
    }
}
