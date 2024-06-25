// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.BatchRequest;
import com.azure.ai.translation.document.models.DocumentFilter;
import com.azure.ai.translation.document.models.Glossary;
import com.azure.ai.translation.document.models.SourceInput;
import com.azure.ai.translation.document.models.StartTranslationDetails;
import com.azure.ai.translation.document.models.StorageSource;
import com.azure.ai.translation.document.models.TargetInput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestHelper {
    
    
    public static SourceInput createSourceInput(String sourceUrl, DocumentFilter filter, String sourceLanguage, StorageSource storageSource) {
        SourceInput sourceInput = new SourceInput(sourceUrl);
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
    
    public static TargetInput createTargetInput(String targetUrl, String targetLanguageCode, String category, List<Glossary> glossaries, StorageSource storageSource) {
        TargetInput targetInput = new TargetInput(targetUrl, targetLanguageCode);
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
            
    public static StartTranslationDetails getStartTranslationDetails(BatchRequest... batchRequests) {          
        List<BatchRequest> inputs = new ArrayList<>();
        inputs.addAll(Arrays.asList(batchRequests));        

        StartTranslationDetails startTranslationDetails = new StartTranslationDetails(inputs);

        return  startTranslationDetails;
    }
    
}
