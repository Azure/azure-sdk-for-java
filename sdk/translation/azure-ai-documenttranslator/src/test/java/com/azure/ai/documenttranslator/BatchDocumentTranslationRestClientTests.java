// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documenttranslator;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.models.JsonArray;
import com.azure.json.models.JsonElement;
import com.azure.json.models.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BatchDocumentTranslationRestClientTests extends BatchDocumentTranslationClientTestBase {
    @Test
    public void testGetSupportedDocumentFormats() throws IOException {
        BatchDocumentTranslationClient client = getClient();
        String response = client.getSupportedDocumentFormatsWithResponse(null).getValue().toString();

        assertNotNull(response);

        try (JsonReader jsonReader = JsonProviders.createReader(response)) {
            JsonObject jsonObject = JsonObject.fromJson(jsonReader);

            JsonElement jsonElement = jsonObject.getProperty("value");
            assertNotNull(jsonElement);
            assertTrue(jsonElement.isArray(), "Expected value to be an array");
            assertTrue(((JsonArray) jsonElement).size() > 0, "Expected value to have at least one element");
        }
    }

    @Test
    public void testGetSupportedGlossaryFormats() throws IOException {
        BatchDocumentTranslationClient client = getClient();
        String response = client.getSupportedGlossaryFormatsWithResponse(null).getValue().toString();

        assertNotNull(response);

        try (JsonReader jsonReader = JsonProviders.createReader(response)) {
            JsonObject jsonObject = JsonObject.fromJson(jsonReader);

            JsonElement jsonElement = jsonObject.getProperty("value");
            assertNotNull(jsonElement);
            assertTrue(jsonElement.isArray(), "Expected value to be an array");
            assertTrue(((JsonArray) jsonElement).size() > 0, "Expected value to have at least one element");
        }
    }
}
