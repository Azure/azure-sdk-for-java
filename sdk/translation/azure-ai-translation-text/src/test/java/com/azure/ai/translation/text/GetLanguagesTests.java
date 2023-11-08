// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.GetLanguagesResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetLanguagesTests extends TextTranslationClientBase {

    @Test
    public void getLanguagesAllScopes() {
        GetLanguagesResult response = getTranslationClient().getLanguages(null, null, null, null);
        assertFalse(response.getTranslation().isEmpty());
        assertFalse(response.getDictionary().isEmpty());
        assertFalse(response.getTransliteration().isEmpty());
    }

    @Test
    public void getLanguagesTranslationScope() {
        GetLanguagesResult response = getTranslationClient().getLanguages(null, "translation", null, null);
        assertFalse(response.getTranslation().isEmpty());
        assertTrue(response.getTranslation().containsKey("af"));
        assertNotNull(response.getTranslation().get("af").getDir());
        assertNotNull(response.getTranslation().get("af").getName());
        assertNotNull(response.getTranslation().get("af").getNativeName());
    }

    @Test
    public void getLanguagesTransliterationScope() {
        GetLanguagesResult response = getTranslationClient().getLanguages(null, "transliteration", null, null);
        assertFalse(response.getTransliteration().isEmpty());
        assertTrue(response.getTransliteration().containsKey("be"));

        assertNotNull(response.getTransliteration().get("be").getName());
        assertNotNull(response.getTransliteration().get("be").getNativeName());
        assertNotNull(response.getTransliteration().get("be").getScripts());

        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getCode());
        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getDir());
        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getName());
        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getNativeName());
        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getToScripts());

        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getToScripts().get(0).getCode());
        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getToScripts().get(0).getDir());
        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getToScripts().get(0).getName());
        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getToScripts().get(0).getNativeName());
    }

    @Test
    public void getLanguagesTransliterationScopeMultipleScripts() {

        GetLanguagesResult response = getTranslationClient().getLanguages(null, "transliteration", null, null);
        assertFalse(response.getTransliteration().isEmpty());
        assertTrue(response.getTransliteration().containsKey("zh-Hant"));

        assertNotNull(response.getTransliteration().get("zh-Hant").getName());
        assertNotNull(response.getTransliteration().get("zh-Hant").getNativeName());
        assertNotNull(response.getTransliteration().get("zh-Hant").getScripts());

        assertTrue(response.getTransliteration().get("zh-Hant").getScripts().get(0).getToScripts().size() > 1);
        assertTrue(response.getTransliteration().get("zh-Hant").getScripts().get(1).getToScripts().size() > 1);
    }

    @Test
    public void getLanguagesDictionaryScope() {
        GetLanguagesResult response = getTranslationClient().getLanguages(null, "dictionary", null, null);
        assertFalse(response.getDictionary().isEmpty());
        assertTrue(response.getDictionary().containsKey("de"));

        assertNotNull(response.getDictionary().get("de").getName());
        assertNotNull(response.getDictionary().get("de").getNativeName());
        assertFalse(response.getDictionary().get("de").getDir().isEmpty());

        assertNotNull(response.getDictionary().get("de").getTranslations().get(0).getCode());
        assertNotNull(response.getDictionary().get("de").getTranslations().get(0).getDir());
        assertNotNull(response.getDictionary().get("de").getTranslations().get(0).getName());
        assertNotNull(response.getDictionary().get("de").getTranslations().get(0).getNativeName());
    }

    @Test
    public void getLanguagesDictionaryScopeMultipleTranslations() {
        GetLanguagesResult response = getTranslationClient().getLanguages(null, "dictionary", null, null);
        assertFalse(response.getDictionary().isEmpty());
        assertTrue(response.getDictionary().containsKey("en"));

        assertNotNull(response.getDictionary().get("en").getName());
        assertNotNull(response.getDictionary().get("en").getNativeName());
        assertNotNull(response.getDictionary().get("en").getDir());

        assertTrue(response.getDictionary().get("en").getTranslations().size() > 1);
    }

    @Test
    public void getLanguagesWithCulture() {
        GetLanguagesResult response = getTranslationClient().getLanguages(null, null, "es", null);
        assertFalse(response.getTransliteration().isEmpty());
        assertFalse(response.getTranslation().isEmpty());
        assertFalse(response.getDictionary().isEmpty());

        assertNotNull(response.getTranslation().get("en").getDir());
        assertNotNull(response.getTranslation().get("en").getName());
        assertNotNull(response.getTranslation().get("en").getNativeName());
    }
}
