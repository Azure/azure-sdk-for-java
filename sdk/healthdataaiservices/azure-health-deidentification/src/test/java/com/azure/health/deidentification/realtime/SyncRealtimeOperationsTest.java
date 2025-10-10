// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.realtime;

import com.azure.health.deidentification.DeidentificationClient;
import com.azure.health.deidentification.batch.BatchOperationTestBase;
import com.azure.health.deidentification.models.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyncRealtimeOperationsTest extends BatchOperationTestBase {
    private DeidentificationClient deidentificationClient;

    @Test
    void testSurrogateReturnsExpected() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String inputText = "Hello, my name is John Smith.";
        DeidentificationContent content = new DeidentificationContent(inputText);
        content.setOperationType(DeidentificationOperationType.SURROGATE);
        DeidentificationCustomizationOptions options = new DeidentificationCustomizationOptions();
        options.setSurrogateLocale("en-US");
        content.setCustomizations(options);

        DeidentificationResult result = deidentificationClient.deidentifyText(content);

        assertNull(result.getTaggerResult());
        assertNotNull(result.getOutputText());
        assertTrue(result.getOutputText().length() > 21);
        assertNotEquals(inputText, result.getOutputText());
    }

    @Test
    void testTagReturnsExpected() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String inputText = "Hello, my name is John Smith.";
        DeidentificationContent content = new DeidentificationContent(inputText);
        content.setOperationType(DeidentificationOperationType.TAG);

        DeidentificationResult result = deidentificationClient.deidentifyText(content);

        assertNotNull(result.getTaggerResult());
        assertNull(result.getOutputText());
        assertFalse(result.getTaggerResult().getEntities().isEmpty());
        assertTrue(result.getTaggerResult().getEntities().get(0).getCategory().equals(PhiCategory.DOCTOR)
            || result.getTaggerResult().getEntities().get(0).getCategory().equals(PhiCategory.PATIENT));
        assertEquals("John Smith", result.getTaggerResult().getEntities().get(0).getText());
        assertEquals(18, result.getTaggerResult().getEntities().get(0).getOffset().getUtf8());
        assertEquals(10, result.getTaggerResult().getEntities().get(0).getLength().getUtf8());
    }

    @Test
    void testRedactReturnsExpected() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String inputText = "Hello, my name is John Smith.";
        DeidentificationContent content = new DeidentificationContent(inputText);
        content.setOperationType(DeidentificationOperationType.REDACT);
        DeidentificationCustomizationOptions options = new DeidentificationCustomizationOptions();
        options.setInputLocale("en-US");
        content.setCustomizations(options);

        DeidentificationResult result = deidentificationClient.deidentifyText(content);

        assertNotNull(result);
        assertNull(result.getTaggerResult());
        assertNotNull(result.getOutputText());
        assertEquals("Hello, my name is [patient].", result.getOutputText());
    }

    @Test
    void testSurrogateOnlyReturnsExpected() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String inputText = "Hello, my name is John Smith.";
        DeidentificationContent content = new DeidentificationContent(inputText);
        content.setOperationType(DeidentificationOperationType.SURROGATE_ONLY);
        content.setTaggedEntities(
            new TaggedPhiEntities(Collections.singletonList(new SimplePhiEntity(PhiCategory.PATIENT, 18, 10))));
        DeidentificationResult result = deidentificationClient.deidentifyText(content);

        assertNotNull(result);
        assertNull(result.getTaggerResult());
        assertNotNull(result.getOutputText());
        assertTrue(result.getOutputText().length() > 21);
        assertNotEquals(inputText, result.getOutputText());
    }
}
