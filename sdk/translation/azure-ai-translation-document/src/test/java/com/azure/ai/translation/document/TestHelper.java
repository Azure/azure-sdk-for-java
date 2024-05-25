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
    
    
    public static SourceInput CreateSourceInput(String sourceUrl, DocumentFilter filter, String sourceLanguage, StorageSource storageSource) {
        SourceInput sourceInput = new SourceInput(sourceUrl);
        if(filter != null)
        { 
            sourceInput.setFilter(filter);
        }
        if(sourceLanguage != null)
        { 
            sourceInput.setLanguage(sourceLanguage);
        }
        if(storageSource != null)
        { 
            sourceInput.setStorageSource(storageSource);
        }
        return sourceInput;
    }
    
    public static TargetInput CreateTargetInput(String targetUrl, String targetLanguageCode, String category, List<Glossary> glossaries, StorageSource storageSource) {
        TargetInput targetInput = new TargetInput(targetUrl, targetLanguageCode);
        if(glossaries != null)
        {   
            targetInput.setGlossaries(glossaries);
        }
        if(category != null)
        {
            targetInput.setCategory(category);
        }
        if(storageSource != null)
        {
            targetInput.setStorageSource(storageSource);
        }         
        return targetInput;
    }
            
    public static StartTranslationDetails GetStartTranslationDetails(BatchRequest... batchRequests){          
        List<BatchRequest> inputs = new ArrayList<>();
        inputs.addAll(Arrays.asList(batchRequests));        

        StartTranslationDetails startTranslationDetails = new StartTranslationDetails(inputs);

        return  startTranslationDetails;
    }
    
}
