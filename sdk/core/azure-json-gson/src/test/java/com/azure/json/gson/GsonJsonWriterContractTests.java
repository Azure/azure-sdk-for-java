// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.gson;

import com.azure.json.JsonWriter;
import com.azure.json.JsonWriterContractTests;
import com.azure.json.implementation.AccessibleByteArrayOutputStream;

import java.nio.charset.StandardCharsets;

/**
 * Tests {@link GsonJsonWriter} against the contract required by {@link JsonWriter}.
 */
public class GsonJsonWriterContractTests extends JsonWriterContractTests {
    private AccessibleByteArrayOutputStream outputStream;

    @Override
    public JsonWriter getJsonWriter() {
        outputStream = new AccessibleByteArrayOutputStream();

        return GsonJsonWriter.fromStream(outputStream);
    }

    @Override
    public String getJsonWriterContents() {
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
