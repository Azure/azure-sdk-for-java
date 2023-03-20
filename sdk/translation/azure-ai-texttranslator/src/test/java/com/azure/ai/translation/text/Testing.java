// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class Testing extends TextTranslationClientBase{
    
     @Test    
    public void TranslateBasic() throws Exception {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("It is a beautiful morning"));

        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content);
        
        assertEquals(1, response.get(0).getTranslations().size());
        assertNotNull(response.get(0).getTranslations().get(0).getText());
        assertEquals("en",response.get(0).getDetectedLanguage().getLanguage());
    }
    @Test    
    public void TranslateWithCustomEndpoint() throws Exception {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("It is a beautiful morning"));

        List<TranslatedTextItem> response = getTranslationClientWithCustomEndpoint().translate(targetLanguages, content);
        
        assertEquals(1, response.get(0).getTranslations().size());
        assertNotNull(response.get(0).getTranslations().get(0).getText());
        assertEquals("en",response.get(0).getDetectedLanguage().getLanguage());
        assertEquals(1,response.get(0).getDetectedLanguage().getScore());
    }
    /*
    @Test    
    public void TranslateWithToken() throws Exception {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("This is a test."));

        List<TranslatedTextItem> response = getTranslationClientWithToken().translate(targetLanguages, content);
                
        assertNotNull(response.get(0).getTranslations().get(0).getText());
        assertEquals("cs",response.get(0).getDetectedLanguage().getLanguage());
    } */
}
