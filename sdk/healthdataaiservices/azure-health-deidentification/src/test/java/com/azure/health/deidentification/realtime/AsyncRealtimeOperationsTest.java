// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.realtime;

import com.azure.health.deidentification.DeidentificationAsyncClient;
import com.azure.health.deidentification.batch.BatchOperationTestBase;
import com.azure.health.deidentification.models.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncRealtimeOperationsTest extends BatchOperationTestBase {
    protected DeidentificationAsyncClient deidServicesAsyncClient;

    @Test
    void testSurrogateReturnsExpected() {
        deidServicesAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String inputText = "Hello, my name is John Smith.";
        DeidentificationContent content = new DeidentificationContent(inputText);
        content.setOperationType(DeidentificationOperationType.SURROGATE);
        DeidentificationCustomizationOptions options = new DeidentificationCustomizationOptions();
        options.setSurrogateLocale("en-US");
        content.setCustomizations(options);

        Mono<DeidentificationResult> result = deidServicesAsyncClient.deidentifyText(content);
        DeidentificationResult asyncResult = result.block();

        assertNotNull(asyncResult);
        assertNull(asyncResult.getTaggerResult());
        assertNotNull(asyncResult.getOutputText());
        assertTrue(asyncResult.getOutputText().length() > 21);
        assertNotEquals(inputText, asyncResult.getOutputText());
    }

    @Test
    void testTagReturnsExpected() {
        deidServicesAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String inputText = "Hello, my name is John Smith.";
        DeidentificationContent content = new DeidentificationContent(inputText);
        content.setOperationType(DeidentificationOperationType.TAG);

        Mono<DeidentificationResult> result = deidServicesAsyncClient.deidentifyText(content);
        DeidentificationResult asyncResult = result.block();

        assertNotNull(asyncResult);
        assertNotNull(asyncResult.getTaggerResult());
        assertNull(asyncResult.getOutputText());
        assertFalse(asyncResult.getTaggerResult().getEntities().isEmpty());
        assertTrue(asyncResult.getTaggerResult().getEntities().get(0).getCategory().equals(PhiCategory.DOCTOR)
            || asyncResult.getTaggerResult().getEntities().get(0).getCategory().equals(PhiCategory.PATIENT));
        assertEquals("John Smith", asyncResult.getTaggerResult().getEntities().get(0).getText());
        assertEquals(18, asyncResult.getTaggerResult().getEntities().get(0).getOffset().getUtf8());
        assertEquals(10, asyncResult.getTaggerResult().getEntities().get(0).getLength().getUtf8());
    }

    @Test
    void testRedactReturnsExpected() {
        deidServicesAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String inputText = "Hello, my name is John Smith.";
        DeidentificationContent content = new DeidentificationContent(inputText);
        content.setOperationType(DeidentificationOperationType.REDACT);
        DeidentificationCustomizationOptions options = new DeidentificationCustomizationOptions();
        options.setInputLocale("en-US");
        content.setCustomizations(options);

        Mono<DeidentificationResult> result = deidServicesAsyncClient.deidentifyText(content);
        DeidentificationResult asyncResult = result.block();

        assertNotNull(asyncResult);
        assertNull(asyncResult.getTaggerResult());
        assertNotNull(asyncResult.getOutputText());
        assertEquals("Hello, my name is [patient].", asyncResult.getOutputText());
    }

    @Test
    void testSurrogateOnlyReturnsExpected() {
        deidServicesAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String inputText = "Hello, my name is John Smith.";
        DeidentificationContent content = new DeidentificationContent(inputText);
        content.setOperationType(DeidentificationOperationType.SURROGATE_ONLY);
        content.setTaggedEntities(
            new TaggedPhiEntities(Collections.singletonList(new SimplePhiEntity(PhiCategory.PATIENT, 18, 10))));

        Mono<DeidentificationResult> result = deidServicesAsyncClient.deidentifyText(content);
        DeidentificationResult asyncResult = result.block();

        assertNotNull(asyncResult);
        assertNull(asyncResult.getTaggerResult());
        assertNotNull(asyncResult.getOutputText());
        assertTrue(asyncResult.getOutputText().length() > 21);
        assertNotEquals(inputText, asyncResult.getOutputText());
    }
}
