// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cognitiveservices.translator;

import com.azure.cognitiveservices.translator.authentication.AzureRegionalKeyCredential;
import com.azure.cognitiveservices.translator.implementation.TranslatorClientImpl;
import com.azure.cognitiveservices.translator.models.InputTextElement;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.test.annotation.DoNotRecord;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TranslateTests {

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void getLanguagesTest() throws Exception {
        
//        assertEquals(1, 1);
        var clientImpl = new TranslatorClientImpl("https://api.cognitive.microsofttranslator.com", TranslatorServiceVersion.V3_0);
        var clientAsync = new TranslatorAsyncClient(clientImpl);
        var client = new TranslatorClient(clientAsync);
                
        var languages = client.getLanguages();
        assertEquals(111, languages.getTranslation().size());
    }
    
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void translateTest() throws Exception {
        var regionalCredential = new AzureRegionalKeyCredential(new AzureKeyCredential(""), "westus2");
        
        var client = new TranslatorClientBuilder()
                .credential(regionalCredential)
                .endpoint("https://api.cognitive.microsofttranslator.com")
                .buildClient();
        
        var targetLanguages = new ArrayList<String>();
        targetLanguages.add("cs");
        
        var content = new ArrayList<InputTextElement>();
        content.add(new InputTextElement("This is a test."));
        
        var translation = client.translate(targetLanguages, content);
        assertEquals(1, translation.size());
    }
}
