// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documenttranslator;

import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BatchDocumentTranslationRestClientTests extends BatchDocumentTranslationClientTestBase {
    @Test
    public void testGetSupportedDocumentFormats() {
        BatchDocumentTranslationClient client = getClient();
        String response = client.getSupportedDocumentFormatsWithResponse(null)
            .getValue()
            .toString();

        assertNotNull(response);

        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonObject result = jsonReader.readObject();

        assertTrue(result.containsKey("value"));
        assertTrue(result.getJsonArray("value").size() > 0);
    }

    @Test
    public void testGetSupportedGlossaryFormats() {
        BatchDocumentTranslationClient client = getClient();
        String response = client.getSupportedGlossaryFormatsWithResponse(null)
            .getValue()
            .toString();

        assertNotNull(response);

        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonObject result = jsonReader.readObject();

        assertTrue(result.containsKey("value"));
        assertTrue(result.getJsonArray("value").size() > 0);
    }
}
