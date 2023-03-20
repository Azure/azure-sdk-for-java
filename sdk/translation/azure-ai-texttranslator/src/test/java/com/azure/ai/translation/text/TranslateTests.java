// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import java.util.ArrayList;
import java.util.List;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.ai.translation.text.models.ProfanityAction;
import com.azure.ai.translation.text.models.ProfanityMarker;
import com.azure.ai.translation.text.models.TextType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TranslateTests extends TextTranslationClientBase{    
    
    @Test    
    public void TranslateBasic() throws Exception {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("Hola mundo"));

        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content);
        
        assertEquals(1, response.get(0).getTranslations().size());
        assertEquals("cs", response.get(0).getTranslations().get(0).getTo());
        assertNotNull(response.get(0).getTranslations().get(0).getText());
    }

    @Test
    public void TranslateWithAutoDetect() {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("This is a test."));

        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content);
        assertEquals("en", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals(1, response.get(0).getTranslations().size());
        assertEquals("cs", response.get(0).getTranslations().get(0).getTo());
        assertNotNull(response.get(0).getTranslations().get(0).getText());
    } 
    
    @Test
    public void TranslateWithNoTranslateTag() {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("en");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("<span class=notranslate>今天是怎么回事是</span>非常可怕的"));

        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content, null, "zh-chs", TextType.HTML, null, null, null, null, null, null, null, null, null);
		
        assertEquals(1, response.get(0).getTranslations().size());
        assertTrue(response.get(0).getTranslations().get(0).getText().contains("今天是怎么回事是"));
    }
	
    @Test
    public void TranslateWithDictionaryTag()
    {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("es");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("The word < mstrans:dictionary translation =\"wordomatic\">wordomatic</mstrans:dictionary> is a dictionary entry." ));

        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content, null, "en", null, null, null, null, null, null, null, null, null, null);

        assertEquals(1, response.get(0).getTranslations().size());
        assertEquals("es", response.get(0).getTranslations().get(0).getTo());
        assertTrue(response.get(0).getTranslations().get(0).getText().contains("wordomatic"));
    }

    @Test
    public void TranslateWithTransliteration()
    {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("zh-Hans");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("hudha akhtabar." ));

        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content, null, "ar", null, null, null, null, null, null, null, "Latn", "Latn", null);
        
        assertNotNull(response.get(0).getSourceText().getText());
        assertEquals("zh-Hans", response.get(0).getTranslations().get(0).getTo());
        assertNotNull(response.get(0).getTranslations().get(0).getText());
    }

    @Test
    public void TranslateFromLatinToLatinScript()
    {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("ta");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("ap kaise ho" ));
        
        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content, null, "hi", null, null, null, null, null, null, null, "Latn", "Latn", null);

        assertNotNull(response.get(0).getTranslations().get(0).getTransliteration().getScript());
        assertEquals("eppadi irukkiraai?", response.get(0).getTranslations().get(0).getTransliteration().getText());
    }

    @Test
    public void TranslateWithMultipleInputTexts()
    {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("This is a test." ));
        content.add(new InputTextItem("Esto es una prueba." ));
        content.add(new InputTextItem("Dies ist ein Test." ));

        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content);

        assertEquals(3, response.size());
        assertEquals("en", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals("es", response.get(1).getDetectedLanguage().getLanguage());
        assertEquals("de", response.get(2).getDetectedLanguage().getLanguage());

        assertEquals(1, response.get(0).getDetectedLanguage().getScore());
        assertEquals(1, response.get(1).getDetectedLanguage().getScore());
        assertEquals(1, response.get(2).getDetectedLanguage().getScore());

        assertNotNull( response.get(0).getTranslations().get(0).getText());
        assertNotNull( response.get(1).getTranslations().get(0).getText());
        assertNotNull( response.get(2).getTranslations().get(0).getText());
    }

    @Test
    public void TranslateMultipleTargetLanguages()
    {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");
        targetLanguages.add("es");
        targetLanguages.add("de");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("This is a test." ));

        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content);

        assertEquals(1, response.size());
        assertEquals(3, response.get(0).getTranslations().size());
        assertEquals("en", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals(1, response.get(0).getDetectedLanguage().getScore());
        assertNotNull(response.get(0).getTranslations().get(0).getText());
        assertNotNull(response.get(0).getTranslations().get(1).getText());
        assertNotNull(response.get(0).getTranslations().get(2).getText());
    }

    @Test
    public void TranslateDifferentTextTypes()
    {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");
        
        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("<html><body>This <b>is</b> a test.</body></html>"));

        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content, null, null, TextType.HTML, null, null, null, null, null, null, null, null, null);

        assertEquals(1, response.size());
        assertEquals(1, response.get(0).getTranslations().size());
        assertEquals("en", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals(1, response.get(0).getDetectedLanguage().getScore());
    }

    @Test
    public void TranslateWithProfanity()
    {
        ProfanityAction profanityAction = ProfanityAction.MARKED;
        ProfanityMarker profanityMarker = ProfanityMarker.ASTERISK;

        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("zh-cn");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("shit this is fucking crazy"));

        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content, null, null, null, null, profanityAction, profanityMarker, null, null, null, null, null, null);

        assertEquals(1, response.size());
        assertEquals(1, response.get(0).getTranslations().size());
        assertEquals("en", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals(1, response.get(0).getDetectedLanguage().getScore());
        assertTrue(response.get(0).getTranslations().get(0).getText().contains("***"));
    }
    
    @Test
    public void TranslateWithAlignment()
    {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("It is a beautiful morning"));
        
        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content, null, null, null, null, null, null,true, null, null, null, null, null);

        assertEquals(1, response.size());
        assertEquals(1, response.get(0).getTranslations().size());
        assertEquals("en", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals(1, response.get(0).getDetectedLanguage().getScore());
        assertNotNull(response.get(0).getTranslations().get(0).getAlignment().getProj()); 
    }

    @Test
    public void TranslateWithIncludeSentenceLength()
    {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("fr");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("La réponse se trouve dans la traduction automatique. La meilleure technologie de traduction automatique ne peut pas toujours fournir des traductions adaptées à un site ou des utilisateurs comme un être humain. Il suffit de copier et coller un extrait de code n'importe où."));
        
        List<TranslatedTextItem> response = getTranslationClient().translate(targetLanguages, content, null, null, null, null, null, null,null, true, null, null, null, null);

        assertEquals(1, response.size());        
        assertEquals("fr", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals(1, response.get(0).getDetectedLanguage().getScore());
        assertEquals(1, response.get(0).getTranslations().size());
        assertEquals(3, response.get(0).getTranslations().get(0).getSentLen().getSrcSentLen().size());
        assertEquals(3, response.get(0).getTranslations().get(0).getSentLen().getTransSentLen().size());
    }

    @Test
    public void TranslateWithCustomEndpoint()
    {
        ArrayList<String> targetLanguages = new ArrayList<>();
        targetLanguages.add("cs");

        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("It is a beautiful morning"));
        
        List<TranslatedTextItem> response = getTranslationClientWithCustomEndpoint().translate(targetLanguages, content);

        assertEquals(1, response.size());        
        assertEquals("en", response.get(0).getDetectedLanguage().getLanguage());
        assertEquals(1, response.get(0).getDetectedLanguage().getScore());
        assertEquals(1, response.get(0).getTranslations().size());
        assertNotNull(response.get(0).getTranslations().get(0).getText()); 	
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