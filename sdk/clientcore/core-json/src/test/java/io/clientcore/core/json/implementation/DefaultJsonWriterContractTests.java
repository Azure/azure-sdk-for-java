// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.json.implementation;

import io.clientcore.core.json.JsonOptions;
import io.clientcore.core.json.JsonWriter;
import io.clientcore.core.json.contract.JsonWriterContractTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Tests {@link DefaultJsonWriter} against the contract required by {@link JsonWriter}.
 */
public class DefaultJsonWriterContractTests extends JsonWriterContractTests {
    private ByteArrayOutputStream outputStream;
    private JsonWriter writer;

    @BeforeEach
    public void beforeEach() throws IOException {
        this.outputStream = new ByteArrayOutputStream();
        this.writer = DefaultJsonWriter.toStream(outputStream, new JsonOptions());
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
