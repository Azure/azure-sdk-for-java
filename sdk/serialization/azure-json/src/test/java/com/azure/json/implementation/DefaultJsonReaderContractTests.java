// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.implementation;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.contract.JsonReaderContractTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests {@link DefaultJsonReader} against the contract required by {@link JsonReader}.
 */
public class DefaultJsonReaderContractTests extends JsonReaderContractTests {
    private JsonReader reader;

    @Override
    public JsonReader getJsonReader(String json) throws IOException {
        this.reader = DefaultJsonReader.fromString(json, new JsonOptions());
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
        String json = "{    // single line comment\n" + "    \"single-line\": \"comment\",\n" + "    /*\n"
            + "    multi-line comment\n" + "    */\n" + "    \"multi-line\": \"comment\"}";
        try (JsonReader jsonReader = getJsonReader(json)) {
            jsonReader.nextToken();
            String outputJson = jsonReader.readChildren();
            assertNotNull(outputJson);
        }
    }
}
