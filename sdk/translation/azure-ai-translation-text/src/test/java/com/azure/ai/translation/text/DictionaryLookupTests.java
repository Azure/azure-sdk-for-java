// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.DictionaryLookupItem;
import com.azure.ai.translation.text.models.InputTextItem;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class DictionaryLookupTests extends TextTranslationClientBase{
    @Test    
    public void SingleInputItem() throws Exception {

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("fly"));

        List<DictionaryLookupItem> response = getTranslationClient().lookupDictionaryEntries("en", "es", content, null);
        
        assertEquals("fly", response.get(0).getNormalizedSource());
        assertEquals("fly", response.get(0).getDisplaySource());        
    }
    
    @Test    
    public void MultipleInputItems() throws Exception {

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("fly"));
        content.add(new InputTextItem("fox"));

        List<DictionaryLookupItem> response = getTranslationClient().lookupDictionaryEntries("en", "es", content, null);
        assertTrue(response.size() == 2);      
    }
}
