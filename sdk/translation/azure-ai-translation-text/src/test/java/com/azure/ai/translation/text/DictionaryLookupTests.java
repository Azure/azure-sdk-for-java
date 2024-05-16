// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.DictionaryLookupItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DictionaryLookupTests extends TextTranslationClientBase {
    @Test
    public void singleInputItem() {
        List<DictionaryLookupItem> response = getTranslationClient().lookupDictionaryEntries("en", "es", "fly");

        assertEquals("fly", response.get(0).getNormalizedSource());
        assertEquals("fly", response.get(0).getDisplaySource());
    }

    @Test
    public void multipleInputItems() {
        ArrayList<String> content = new ArrayList<>();
        content.add("fly");
        content.add("fox");

        List<DictionaryLookupItem> response = getTranslationClient().lookupDictionaryEntries("en", "es", content);
        assertEquals(2, response.size());
    }
}
