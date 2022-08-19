// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect;

import com.azure.json.JsonReader;
import com.azure.json.contract.JsonReaderContractTests;
import com.azure.json.reflect.jackson.JacksonJsonReader;

/**
 * Tests {@link GsonJsonReader} against the contract required by {@link JsonReader}.
 */
public class JacksonJsonReaderContractTests extends JsonReaderContractTests {
	@Override
	public JsonReader getJsonReader(String json) {
		return JacksonJsonReader.fromString(json);
	}
}
