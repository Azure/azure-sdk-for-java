// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.DictionaryExampleItem;
import com.azure.ai.translation.text.models.DictionaryExampleTextItem;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class DictionaryExamplesTest extends TextTranslationClientBase{
    @Test    
    public void SingleInputItem() throws Exception {

        ArrayList<DictionaryExampleTextItem> content = new ArrayList<>();
        content.add(new DictionaryExampleTextItem("fly","volar"));

        List<DictionaryExampleItem> response = getTranslationClient().lookupDictionaryExamples("en", "es", content, null);
        
        assertEquals("fly", response.get(0).getNormalizedSource());
        assertEquals("volar", response.get(0).getNormalizedTarget());        
    }
    
    @Test    
    public void MultipleInputItems() throws Exception {

        ArrayList<DictionaryExampleTextItem> content = new ArrayList<>();
        content.add(new DictionaryExampleTextItem("fly","volar"));
        content.add(new DictionaryExampleTextItem("beef","came"));

        List<DictionaryExampleItem> response = getTranslationClient().lookupDictionaryExamples("en", "es", content, null);
        assertTrue(response.size() == 2);      
    }
}
