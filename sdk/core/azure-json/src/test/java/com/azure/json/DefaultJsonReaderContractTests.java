// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

/**
 * Tests {@link DefaultJsonReader} against the contract required by {@link JsonReader}.
 */
public class DefaultJsonReaderContractTests extends JsonReaderContractTests {
    @Override
    public JsonReader getJsonReader(byte[] json) {
        return DefaultJsonReader.fromBytes(json);
    }
}
