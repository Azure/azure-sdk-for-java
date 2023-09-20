// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.json.JsonWriter;
import com.azure.json.contract.JsonWriterContractTests;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Tests {@link JacksonJsonWriter} against the contract required by {@link JsonWriter}.
 */
public class JacksonJsonWriterContractTests extends JsonWriterContractTests {
    private static final JsonFactory FACTORY = JsonFactory.builder().build();

    private ByteArrayOutputStream outputStream;
    private JsonWriter writer;

    @BeforeEach
    public void beforeEach() throws IOException {
        this.outputStream = new ByteArrayOutputStream();
        this.writer = new JacksonJsonWriter(FACTORY.createGenerator(outputStream)
            .configure(JsonWriteFeature.WRITE_NAN_AS_STRINGS.mappedFeature(), true));
    }

    @AfterEach
    public void afterEach() throws IOException {
        if (writer != null) {
            try {
                writer.close();
            } catch (IllegalStateException ignored) {
                // Closing the JsonWriter may throw an IllegalStateException if the current writing state isn't valid
                // for closing, ignore it in test.
            }
        }
    }

    @Override
    public JsonWriter getJsonWriter() {
        return writer;
    }

    @Override
    public String getJsonWriterContents() {
        try {
            writer.flush();
            return outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
