// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.BatchRequest;
import com.azure.ai.translation.document.models.SourceInput;
import com.azure.ai.translation.document.models.TargetInput;
import com.azure.ai.translation.document.models.TranslationStatus;
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
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";
        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);
        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        // Cancel Translation
        String translationId = poller.poll().getValue().getId();
        documentTranslationClient.cancelTranslation(translationId);

        // GetTranslation Status
        TranslationStatus translationStatus = documentTranslationClient.getTranslationStatus(translationId);
        Assertions.assertEquals(translationId, translationStatus.getId());
        String status = translationStatus.getStatus().toString();
        Assertions.assertTrue("Cancelled".equals(status) || "Cancelling".equals(status) || "NotStarted".equals(status));
    }
}
