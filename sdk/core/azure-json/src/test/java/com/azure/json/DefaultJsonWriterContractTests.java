// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.contract.JsonWriterContractTests;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Tests {@link DefaultJsonWriter} against the contract required by {@link JsonWriter}.
 */
public class DefaultJsonWriterContractTests extends JsonWriterContractTests {
    private ByteArrayOutputStream outputStream;
    private JsonWriter writer;

    @BeforeEach
    public void beforeEach() {
        this.outputStream = new ByteArrayOutputStream();
        this.writer = DefaultJsonWriter.toStream(outputStream);
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
