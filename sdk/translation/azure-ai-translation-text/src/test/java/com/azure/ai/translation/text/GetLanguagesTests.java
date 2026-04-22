// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.GetSupportedLanguagesResult;
import com.azure.ai.translation.text.models.LanguageScope;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetLanguagesTests extends TextTranslationClientBase {

    @Test
    public void getSupportedLanguagesAllScopes() {
        GetSupportedLanguagesResult response = getTranslationClient().getSupportedLanguages();
        assertFalse(response.getTranslation().isEmpty());
        assertFalse(response.getModels().isEmpty());
        assertFalse(response.getTransliteration().isEmpty());
    }

    @Test
    public void getSupportedLanguagesTranslationScope() {
        ArrayList<LanguageScope> scopes = new ArrayList<>();
        scopes.add(LanguageScope.TRANSLATION);
        GetSupportedLanguagesResult response = getTranslationClient().getSupportedLanguages(scopes, null, null);
        assertFalse(response.getTranslation().isEmpty());
        assertTrue(response.getTranslation().containsKey("af"));
        assertNotNull(response.getTranslation().get("af").getDirectionality());
        assertNotNull(response.getTranslation().get("af").getName());
        assertNotNull(response.getTranslation().get("af").getNativeName());
    }

    @Test
    public void getSupportedLanguagesTransliterationScope() {
        ArrayList<LanguageScope> scopes = new ArrayList<>();
        scopes.add(LanguageScope.TRANSLITERATION);
        GetSupportedLanguagesResult response = getTranslationClient().getSupportedLanguages(scopes, null, null);
        assertFalse(response.getTransliteration().isEmpty());
        assertTrue(response.getTransliteration().containsKey("be"));

        assertNotNull(response.getTransliteration().get("be").getName());
        assertNotNull(response.getTransliteration().get("be").getNativeName());
        assertNotNull(response.getTransliteration().get("be").getScripts());

        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getCode());
        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getDirectionality());
        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getName());
        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getNativeName());
        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getToScripts());

        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getToScripts().get(0).getCode());
        assertNotNull(
            response.getTransliteration().get("be").getScripts().get(0).getToScripts().get(0).getDirectionality());
        assertNotNull(response.getTransliteration().get("be").getScripts().get(0).getToScripts().get(0).getName());
        assertNotNull(
            response.getTransliteration().get("be").getScripts().get(0).getToScripts().get(0).getNativeName());
    }

    @Test
    public void getSupportedLanguagesTransliterationScopeMultipleScripts() {
        ArrayList<LanguageScope> scopes = new ArrayList<>();
        scopes.add(LanguageScope.TRANSLITERATION);
        GetSupportedLanguagesResult response = getTranslationClient().getSupportedLanguages(scopes, null, null);
        assertFalse(response.getTransliteration().isEmpty());
        assertTrue(response.getTransliteration().containsKey("zh-Hant"));

        assertNotNull(response.getTransliteration().get("zh-Hant").getName());
        assertNotNull(response.getTransliteration().get("zh-Hant").getNativeName());
        assertNotNull(response.getTransliteration().get("zh-Hant").getScripts());

        assertTrue(response.getTransliteration().get("zh-Hant").getScripts().get(0).getToScripts().size() > 1);
        assertTrue(response.getTransliteration().get("zh-Hant").getScripts().get(1).getToScripts().size() > 1);
    }

    @Test
    public void getSupportedLanguagesWithCulture() {
        GetSupportedLanguagesResult response = getTranslationClient().getSupportedLanguages(null, "es", null);
        assertFalse(response.getTransliteration().isEmpty());
        assertFalse(response.getTranslation().isEmpty());
        assertFalse(response.getModels().isEmpty());

        assertNotNull(response.getTranslation().get("en").getDirectionality());
        assertNotNull(response.getTranslation().get("en").getName());
        assertNotNull(response.getTranslation().get("en").getNativeName());
    }
}
