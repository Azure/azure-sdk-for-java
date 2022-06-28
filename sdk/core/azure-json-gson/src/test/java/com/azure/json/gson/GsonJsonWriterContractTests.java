// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.gson;

import com.azure.json.JsonWriter;
import com.azure.json.contract.JsonWriterContractTests;
import com.azure.json.implementation.AccessibleByteArrayOutputStream;
import org.junit.jupiter.api.BeforeEach;

import java.nio.charset.StandardCharsets;

/**
 * Tests {@link GsonJsonWriter} against the contract required by {@link JsonWriter}.
 */
public class GsonJsonWriterContractTests extends JsonWriterContractTests {
    private AccessibleByteArrayOutputStream outputStream;
    private JsonWriter writer;

    @BeforeEach
    public void beforeEach() {
        this.outputStream = new AccessibleByteArrayOutputStream();
        this.writer = GsonJsonWriter.fromStream(outputStream);
    }

    @Override
    public JsonWriter getJsonWriter() {
        return writer;
    }

    @Override
    public String getJsonWriterContents() {
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
