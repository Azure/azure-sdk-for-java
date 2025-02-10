// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.DocumentTranslationInput;
import com.azure.ai.translation.document.models.TranslationSource;
import com.azure.ai.translation.document.models.TranslationTarget;
import com.azure.ai.translation.document.models.TranslationStatusResult;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

public class CancelTranslationTests extends DocumentTranslationClientTestBase {

    @RecordWithoutRequestBody
    @Test
    public void testCancelTranslation() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        TranslationSource sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";
        TranslationTarget targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TranslationTarget> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        DocumentTranslationInput batchRequest = new DocumentTranslationInput(sourceInput, targetInputs);
        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller = setPlaybackSyncPollerPollInterval(
            documentTranslationClient.beginTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

        // Cancel Translation
        String translationId = poller.poll().getValue().getId();
        documentTranslationClient.cancelTranslation(translationId);

        // GetTranslation Status
        TranslationStatusResult translationStatus = documentTranslationClient.getTranslationStatus(translationId);
        Assertions.assertEquals(translationId, translationStatus.getId());
        String status = translationStatus.getStatus().toString();
        Assertions.assertTrue("Cancelled".equals(status) || "Cancelling".equals(status) || "NotStarted".equals(status),
            "Expected status to be one of 'Cancelled', 'Cancelling', or 'NotStarted', but was: " + status);
    }
}
