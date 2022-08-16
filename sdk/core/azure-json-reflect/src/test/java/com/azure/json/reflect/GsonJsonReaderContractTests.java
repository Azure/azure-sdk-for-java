// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect;

import com.azure.json.JsonReader;
import com.azure.json.contract.JsonReaderContractTests;
import com.azure.json.reflect.gson.GsonJsonReader;

import java.io.StringReader;

/**
 * Tests {@link GsonJsonReader} against the contract required by {@link JsonReader}.
 */
public class GsonJsonReaderContractTests extends JsonReaderContractTests {
    @Override
    public JsonReader getJsonReader(String json) {
        return new GsonJsonReader(new StringReader(json));
    }
}
