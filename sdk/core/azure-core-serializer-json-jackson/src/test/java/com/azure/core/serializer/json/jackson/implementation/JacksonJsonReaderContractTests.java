// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.contract.JsonReaderContractTests;
import com.azure.json.implementation.DefaultJsonReader;
import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.aggregator.ArgumentAccessException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link JacksonJsonReader} against the contract required by {@link JsonReader}.
 */
public class JacksonJsonReaderContractTests extends JsonReaderContractTests {
    private JsonReader reader;
    String jsonWithComments = "{    // single line comment\n" + "    \"single-line\": \"comment\",\n" + "    /*\n"
        + "    multi-line comment\n" + "    */\n" + "    \"multi-line\": \"comment\"}";

    @Override
    public JsonReader getJsonReader(String json) throws IOException {
        this.reader = AzureJsonUtils.createReader(json, null);
        return reader;
    }

    @AfterEach
    public void afterEach() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }


    @Test
    public void readJsonc() throws IOException {

        try (JsonReader jsonReader
                 = AzureJsonUtils.createReader(jsonWithComments, new JsonOptions().setJsoncSupported(true))) {
            jsonReader.nextToken();
            String outputJson = jsonReader.readChildren();
            assertNotNull(outputJson);
        }
    }

    @Test
    public void readJsoncFails() throws IOException {
        assertThrows(JsonParseException.class, () -> {
            try (JsonReader jsonReader = getJsonReader(jsonWithComments)) {
                jsonReader.nextToken();
                String outputJson = jsonReader.readChildren();
                assertNotNull(outputJson);
            }
        });
    }
}
