/*
 * The MIT License
 *
 * Copyright 2024 Microsoft Corporation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.BatchRequest;
import com.azure.ai.translation.document.models.SourceInput;
import com.azure.ai.translation.document.models.TargetInput;
import com.azure.ai.translation.document.models.TranslationStatus;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

public class CancelTranslationTests extends DocumentTranslationClientTestBase {

    @Test
    public void testCancelTranslation() {
        
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        SourceInput sourceInput = TestHelper.CreateSourceInput(sourceUrl, null, null, null);
        
        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr"; 
        TargetInput targetInput = TestHelper.CreateTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>(); 
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);
        SyncPoller<TranslationStatus, Void> poller =
                 getDocumentTranslationClient().beginStartTranslation(TestHelper.GetStartTranslationDetails(batchRequest));   

        //Cancel Translation
        String translationId = poller.poll().getValue().getId();
        getDocumentTranslationClient().cancelTranslation(translationId);

        //GetTranslation Status
        TranslationStatus translationStatus = getDocumentTranslationClient().getTranslationStatus(translationId);

        System.out.println("Translation status ID is : " + translationStatus.getId());
        Assertions.assertEquals(translationId ,translationStatus.getId());

        String status = translationStatus.getStatus().toString();
        System.out.println("Translation status is : " + status);
        Assertions.assertTrue(status.equals("Cancelled") || status.equals("Cancelling") || status.equals("NotStarted"));
        
    }
    
}
