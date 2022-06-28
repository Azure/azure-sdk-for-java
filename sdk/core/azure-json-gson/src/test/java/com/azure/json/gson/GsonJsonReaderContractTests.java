// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.gson;

import com.azure.json.JsonReader;
import com.azure.json.contract.JsonReaderContractTests;

/**
 * Tests {@link GsonJsonReader} against the contract required by {@link JsonReader}.
 */
public class GsonJsonReaderContractTests extends JsonReaderContractTests {
    @Override
    public JsonReader getJsonReader(String json) {
        return GsonJsonReader.fromString(json);
    }
}
