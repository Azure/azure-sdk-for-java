// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.realtime;

import com.azure.health.deidentification.DeidentificationClient;
import com.azure.health.deidentification.batch.BatchOperationTestBase;
import com.azure.health.deidentification.models.DeidentificationContent;
import com.azure.health.deidentification.models.DeidentificationResult;
import com.azure.health.deidentification.models.DocumentDataType;
import com.azure.health.deidentification.models.OperationType;
import com.azure.health.deidentification.models.PhiCategory;
import org.junit.jupiter.api.Test;

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
        content.setOperation(OperationType.SURROGATE);
        content.setDataType(DocumentDataType.PLAINTEXT);

        DeidentificationResult result = deidentificationClient.deidentify(content);

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
        content.setOperation(OperationType.TAG);
        content.setDataType(DocumentDataType.PLAINTEXT);

        DeidentificationResult result = deidentificationClient.deidentify(content);

        assertNotNull(result.getTaggerResult());
        assertNull(result.getOutputText());
        assertNull(result.getTaggerResult().getEtag());
        assertNull(result.getTaggerResult().getPath());
        assertFalse(result.getTaggerResult().getEntities().isEmpty());
        assertTrue(result.getTaggerResult().getEntities().get(0).getCategory().equals(PhiCategory.DOCTOR) || result.getTaggerResult().getEntities().get(0).getCategory().equals(PhiCategory.PATIENT));
        assertEquals("John Smith", result.getTaggerResult().getEntities().get(0).getText());
        assertEquals(18, result.getTaggerResult().getEntities().get(0).getOffset().getUtf8());
        assertEquals(10, result.getTaggerResult().getEntities().get(0).getLength().getUtf8());
    }

}
