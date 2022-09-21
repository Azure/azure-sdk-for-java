package com.azure.json.reflect;

import com.azure.json.JsonWriter;
import com.azure.json.contract.JsonWriterContractTests;
import com.azure.json.reflect.jackson.JacksonJsonWriter;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Tests {@link JacksonJsonWriter} against the contract required by {@link JsonWriter}.
 */
public class JacksonJsonWriterContractTests extends JsonWriterContractTests {
    private ByteArrayOutputStream outputStream;
    private JsonWriter writer;

    @BeforeEach
    public void beforeEach() {
        this.outputStream = new ByteArrayOutputStream();
        this.writer = JacksonJsonWriter.toStream(outputStream);
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
