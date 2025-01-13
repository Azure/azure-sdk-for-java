// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.DocumentFilter;
import com.azure.ai.translation.document.models.DocumentTranslationInput;
import com.azure.ai.translation.document.models.TranslationGlossary;
import com.azure.ai.translation.document.models.TranslationSource;
import com.azure.ai.translation.document.models.TranslationBatch;
import com.azure.ai.translation.document.models.TranslationStorageSource;
import com.azure.ai.translation.document.models.TranslationTarget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestHelper {

    public static TranslationSource createSourceInput(String sourceUrl, DocumentFilter filter, String sourceLanguage,
        TranslationStorageSource storageSource) {
        TranslationSource sourceInput = new TranslationSource(sourceUrl);
        if (filter != null) {
            sourceInput.setFilter(filter);
        }
        if (sourceLanguage != null) {
            sourceInput.setLanguage(sourceLanguage);
        }
        if (storageSource != null) {
            sourceInput.setStorageSource(storageSource);
        }
        return sourceInput;
    }

    public static TranslationTarget createTargetInput(String targetUrl, String targetLanguageCode, String category,
        List<TranslationGlossary> glossaries, TranslationStorageSource storageSource) {
        TranslationTarget targetInput = new TranslationTarget(targetUrl, targetLanguageCode);
        if (glossaries != null) {
            targetInput.setGlossaries(glossaries);
        }
        if (category != null) {
            targetInput.setCategory(category);
        }
        if (storageSource != null) {
            targetInput.setStorageSource(storageSource);
        }
        return targetInput;
    }

    public static TranslationBatch getStartTranslationDetails(DocumentTranslationInput... batchRequests) {
        return new TranslationBatch(new ArrayList<>(Arrays.asList(batchRequests)));
    }

}
