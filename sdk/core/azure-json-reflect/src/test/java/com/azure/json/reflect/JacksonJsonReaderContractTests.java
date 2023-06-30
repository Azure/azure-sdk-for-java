// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.contract.JsonReaderContractTests;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;

/**
 * Tests {@link JacksonJsonReader} against the contract required by {@link JsonReader}.
 */
public class JacksonJsonReaderContractTests extends JsonReaderContractTests {
    private JsonReader reader;

    @Override
    public JsonReader getJsonReader(String json) throws IOException {
        return getJsonReader(json, new JsonOptions());
    }

    @Override
    protected JsonReader getJsonReader(String json, JsonOptions jsonOptions) throws IOException {
        this.reader = JsonProviderFactory.getJacksonJsonProvider().createReader(json,
            jsonOptions == null ? new JsonOptions() : jsonOptions);
        return reader;
    }

    @AfterEach
    public void afterEach() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
