// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.json.implementation;

import com.client.json.JsonOptions;
import com.client.json.JsonReader;
import com.client.json.contract.JsonReaderContractTests;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;

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
}
