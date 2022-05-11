// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.implementation.AccessibleByteArrayOutputStream;

import java.nio.charset.StandardCharsets;

/**
 * Tests {@link DefaultJsonWriter} against the contract required by {@link JsonWriter}.
 */
public class DefaultJsonWriterContractTests extends JsonWriterContractTests {
    private AccessibleByteArrayOutputStream outputStream;

    @Override
    public JsonWriter getJsonWriter() {
        outputStream = new AccessibleByteArrayOutputStream();

        return DefaultJsonWriter.fromStream(outputStream);
    }

    @Override
    public String getJsonWriterContents() {
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
