// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StructuredInputSerializationTests {
    private static final String DECIMAL = "0.123456789012345678901234567890";
    private static final String INTEGER = "9223372036854775808123456789";
    private static final String NESTED = "{\"items\":[" + DECIMAL + "," + INTEGER + ",1e400,null,\"text\",false]}";
    private static final String INPUTS
        = "{\"decimal\":" + DECIMAL + ",\"integer\":" + INTEGER + ",\"exponent\":1e400,\"nested\":" + NESTED
            + ",\"text\":\"line\\n\\\"quoted\\\"\",\"flag\":true,\"nil\":null}";

    @ParameterizedTest
    @ValueSource(strings = { "recipient", "create", "job", "response" })
    public void structuredInputsRetainExactJsonValues(String model) throws IOException {
        JsonSerializable<?> value;
        Map<String, BinaryData> inputs;
        String json = "{\"destination\":{\"type\":\"phone_number\",\"value\":\"+15551234567\"},"
            + "\"connection_name\":\"connection\",\"structured_inputs\":" + INPUTS + "}";
        try (JsonReader reader = JsonProviders.createReader(json)) {
            switch (model) {
                case "recipient":
                    TelephonyRecipient recipient = TelephonyRecipient.fromJson(reader);
                    inputs = recipient.getStructuredInputs();
                    value = recipient;
                    break;

                case "create":
                    TelephonyCallJobCreate create = TelephonyCallJobCreate.fromJson(reader);
                    inputs = create.getStructuredInputs();
                    value = create;
                    break;

                case "job":
                    TelephonyCallJob job = TelephonyCallJob.fromJson(reader);
                    inputs = job.getStructuredInputs();
                    value = job;
                    break;

                case "response":
                    AzureCreateResponseOptions response = AzureCreateResponseOptions.fromJson(reader);
                    inputs = response.getStructuredInputs();
                    value = response;
                    break;

                default:
                    throw new IllegalArgumentException("Unexpected model " + model);
            }
        }
        assertEquals(new BigDecimal(DECIMAL), inputs.get("decimal").toObject(BigDecimal.class));
        assertEquals(new BigInteger(INTEGER), inputs.get("integer").toObject(BigInteger.class));
        assertEquals(new BigDecimal("1e400"), inputs.get("exponent").toObject(BigDecimal.class));
        assertEquals(NESTED, inputs.get("nested").toString());
        assertEquals("line\n\"quoted\"", inputs.get("text").toObject(String.class));
        assertEquals(Boolean.TRUE, inputs.get("flag").toObject(Boolean.class));
        assertTrue(inputs.containsKey("nil"));
        assertNull(inputs.get("nil"));
        assertTrue(serialize(value).contains("\"structured_inputs\":" + INPUTS));
    }

    @ParameterizedTest
    @ValueSource(strings = { "voice", "prompt" })
    public void agentDefinitionDefaultsAndSchemasRetainExactNumbers(String kind) throws IOException {
        String definition = "{\"kind\":\"" + kind + "\",\"structured_inputs\":{\"amount\":{\"default_value\":" + DECIMAL
            + ",\"schema\":{\"maximum\":" + INTEGER + ",\"examples\":[" + NESTED + "]}}}}";
        JsonSerializable<?> agent;
        StructuredInputDefinition input;
        try (JsonReader reader = JsonProviders.createReader(definition)) {
            if ("voice".equals(kind)) {
                VoiceAgentDefinition voice = VoiceAgentDefinition.fromJson(reader);
                input = voice.getStructuredInputs().get("amount");
                agent = voice;
            } else {
                PromptAgentDefinition prompt = PromptAgentDefinition.fromJson(reader);
                input = prompt.getStructuredInputs().get("amount");
                agent = prompt;
            }
        }
        assertEquals(new BigDecimal(DECIMAL), input.getDefaultValue().toObject(BigDecimal.class));
        assertEquals(new BigInteger(INTEGER), input.getSchema().get("maximum").toObject(BigInteger.class));
        assertEquals("[" + NESTED + "]", input.getSchema().get("examples").toString());
        String serialized = serialize(agent);
        assertTrue(serialized.contains("\"default_value\":" + DECIMAL));
        assertTrue(serialized.contains("\"maximum\":" + INTEGER));
        assertTrue(serialized.contains("\"examples\":[" + NESTED + "]"));
    }

    @Test
    public void absentNullAndEmptyInputsRemainDistinct() throws IOException {
        try (JsonReader reader = JsonProviders.createReader("{}")) {
            assertNull(AzureCreateResponseOptions.fromJson(reader).getStructuredInputs());
        }
        try (JsonReader reader = JsonProviders.createReader("{\"structured_inputs\":null}")) {
            assertNull(AzureCreateResponseOptions.fromJson(reader).getStructuredInputs());
        }
        try (JsonReader reader = JsonProviders.createReader("{\"structured_inputs\":{}}")) {
            AzureCreateResponseOptions options = AzureCreateResponseOptions.fromJson(reader);
            assertTrue(options.getStructuredInputs().isEmpty());
            assertTrue(serialize(options).contains("\"structured_inputs\":{}"));
        }
    }

    @Test
    public void callerProvidedStringInputsRemainJsonStrings() throws IOException {
        AzureCreateResponseOptions options = new AzureCreateResponseOptions()
            .setStructuredInputs(Collections.singletonMap("text", BinaryData.fromString("hello")));
        assertTrue(serialize(options).contains("\"structured_inputs\":{\"text\":\"hello\"}"));
        StructuredInputDefinition definition
            = new StructuredInputDefinition().setDefaultValue(BinaryData.fromString("hello"));
        assertTrue(serialize(definition).contains("\"default_value\":\"hello\""));
    }

    private static String serialize(JsonSerializable<?> value) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(output)) {
            value.toJson(writer);
        }
        return output.toString("UTF-8");
    }
}
