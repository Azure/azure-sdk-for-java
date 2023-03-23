// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.translation.text;

import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.TransliteratedText;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

public class TransliterationTests extends TextTranslationClientBase {

    @Test
    public void verifyTransliteration() {
        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("这里怎么一回事?"));

        List<TransliteratedText> response = getTranslationClient().transliterate("zh-Hans", "Hans", "Latn", content);
        assertTrue(response.get(0).getText().length() > 1);
    }

    @Test
    public void verifyTransliterationWithMultipleTextArray() {
        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("यहएककसौटीहैयहएककसौटीहै"));
        content.add(new InputTextItem("यहएककसौटीहै"));

        List<TransliteratedText> response = getTranslationClient().transliterate("hi", "Deva", "Latn", content);
        assertTrue(response.get(0).getText().length() > 1);
        assertTrue(response.get(1).getText().length() > 1);
    }

    @Test
    public void verifyTransliterationWithEditDistance() {
        ArrayList<InputTextItem> content = new ArrayList<>();
        content.add(new InputTextItem("gujarat"));
        content.add(new InputTextItem("hadman"));
        content.add(new InputTextItem("hukkabar"));

        List<TransliteratedText> response = getTranslationClient().transliterate("gu", "latn", "gujr", content);
        assertTrue(response.get(0).getText().length() > 1);
        assertTrue(response.get(1).getText().length() > 1);
        assertTrue(response.get(2).getText().length() > 1);

        String[] expectedText = { "ગુજરાત", "હદમાં", "હુક્કાબાર" };
        int editDistance = 0;
        for (int i = 0; i < expectedText.length; i++) {
            editDistance = editDistance + TestHelper.EditDistance(expectedText[i], response.get(i).getText());
        }
        assertTrue(editDistance < 6, "Total string distance: {editDistance}");
    }
}
