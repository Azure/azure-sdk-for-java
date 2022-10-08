// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect.jackson;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.contract.JsonReaderContractTests;
import com.azure.json.reflect.jackson.JacksonJsonReader;

import java.io.IOException;

/**
 * Tests {@link JacksonJsonReader} against the contract required by {@link JsonReader}.
 */
public class JacksonJsonReaderContractTests extends JsonReaderContractTests {
	@Override
	public JsonReader getJsonReader(String json) {
        try {
            return JacksonJsonReader.fromString(json, new JsonOptions());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
