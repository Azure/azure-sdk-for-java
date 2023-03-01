// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.texttranslator;

import com.azure.core.test.annotation.DoNotRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetLanguagesTests {

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void getLanguagesTest() throws Exception {
        var client = new TranslatorClientBuilder()
                .endpoint("https://api.cognitive.microsofttranslator.com")
                .buildClient();

        var languages = client.getLanguages();
        assertEquals(111, languages.getTranslation().size());
    }
}
