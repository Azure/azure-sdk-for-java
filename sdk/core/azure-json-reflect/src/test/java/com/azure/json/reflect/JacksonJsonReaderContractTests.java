// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect;

import com.azure.json.JsonReader;
import com.azure.json.contract.JsonReaderContractTests;

import java.io.IOException;

/**
 * Tests {@link JacksonJsonReader} against the contract required by {@link JsonReader}.
 */
public class JacksonJsonReaderContractTests extends JsonReaderContractTests {
    @Override
    public JsonReader getJsonReader(String json) throws IOException {
        return JsonProviderFactory.getJacksonJsonProvider().createReader(json);
    }
}
