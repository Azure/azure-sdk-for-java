// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.realtime;

import com.azure.health.deidentification.DeidServicesAsyncClient;
import com.azure.health.deidentification.batch.BatchOperationTestBase;
import com.azure.health.deidentification.models.DeidentificationContent;
import com.azure.health.deidentification.models.DeidentificationResult;
import com.azure.health.deidentification.models.DocumentDataType;
import com.azure.health.deidentification.models.OperationType;
import com.azure.health.deidentification.models.PhiCategory;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncRealtimeOperationsTest extends BatchOperationTestBase {
    protected DeidServicesAsyncClient deidServicesAsyncClient;

    @Test
    void testSurrogateReturnsExpected() {
        deidServicesAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String inputText = "Hello, my name is John Smith.";
        DeidentificationContent content = new DeidentificationContent(inputText, OperationType.SURROGATE, DocumentDataType.PLAINTEXT);
        Mono<DeidentificationResult> result = deidServicesAsyncClient.deidentify(content);
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
        DeidentificationContent content = new DeidentificationContent(inputText, OperationType.TAG, DocumentDataType.PLAINTEXT);
        Mono<DeidentificationResult> result = deidServicesAsyncClient.deidentify(content);
        DeidentificationResult asyncResult = result.block();

        assertNotNull(asyncResult);
        assertNotNull(asyncResult.getTaggerResult());
        assertNull(asyncResult.getOutputText());
        assertNull(asyncResult.getTaggerResult().getEtag());
        assertNull(asyncResult.getTaggerResult().getPath());
        assertFalse(asyncResult.getTaggerResult().getEntities().isEmpty());
        assertTrue(asyncResult.getTaggerResult().getEntities().get(0).getCategory().equals(PhiCategory.DOCTOR) || asyncResult.getTaggerResult().getEntities().get(0).getCategory().equals(PhiCategory.PATIENT));
        assertEquals("John Smith", asyncResult.getTaggerResult().getEntities().get(0).getText());
        assertEquals(18, asyncResult.getTaggerResult().getEntities().get(0).getOffset().getUtf8());
        assertEquals(10, asyncResult.getTaggerResult().getEntities().get(0).getLength().getUtf8());
    }

}
