// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.implementation;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.contract.JsonReaderContractTests;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;

/**
 * Tests {@link DefaultJsonReader} against the contract required by {@link JsonReader}.
 */
public class DefaultJsonReaderContractTests extends JsonReaderContractTests {
    private JsonReader reader;

    @Override
    public JsonReader getJsonReader(String json) throws IOException {
        return getJsonReader(json, new JsonOptions());
    }

    @Override
    protected JsonReader getJsonReader(String json, JsonOptions jsonOptions) throws IOException {
        this.reader = DefaultJsonReader.fromString(json, jsonOptions ==  null ? new JsonOptions() : jsonOptions);
        return reader;
    }

    @AfterEach
    public void afterEach() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
