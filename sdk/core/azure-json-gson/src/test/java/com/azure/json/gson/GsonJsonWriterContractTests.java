// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.gson;

import com.azure.json.JsonWriter;
import com.azure.json.contract.JsonWriterContractTests;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Tests {@link GsonJsonWriter} against the contract required by {@link JsonWriter}.
 */
public class GsonJsonWriterContractTests extends JsonWriterContractTests {
    private ByteArrayOutputStream outputStream;
    private JsonWriter writer;

    @BeforeEach
    public void beforeEach() {
        this.outputStream = new ByteArrayOutputStream();
        this.writer = GsonJsonWriter.toStream(outputStream);
    }

    @Override
    public JsonWriter getJsonWriter() {
        return writer;
    }

    @Override
    public String getJsonWriterContents() {
        try {
            return outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
