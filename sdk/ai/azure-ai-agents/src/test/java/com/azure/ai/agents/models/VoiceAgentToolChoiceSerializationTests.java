// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.openai.models.responses.ToolChoiceFunction;
import com.openai.models.responses.ToolChoiceMcp;
import com.openai.models.responses.ToolChoiceOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VoiceAgentToolChoiceSerializationTests {
    @Test
    public void voiceAgentDefinitionRoundTrips() throws IOException {
        assertModelRoundTrips("{\"kind\":\"voice\"}", VoiceAgentDefinition::fromJson,
            VoiceAgentDefinition::getToolChoice, VoiceAgentDefinition::setToolChoice);
    }

    @Test
    public void inlineVoiceSubagentRoundTrips() throws IOException {
        assertModelRoundTrips("{\"id\":\"specialist\",\"description\":\"Specialist\"}", InlineVoiceSubagent::fromJson,
            InlineVoiceSubagent::getToolChoice, InlineVoiceSubagent::setToolChoice);
    }

    @Test
    public void greetingConfigurationRoundTrips() throws IOException {
        assertModelRoundTrips("{\"type\":\"llm_generated\",\"prompt\":\"Say hello\"}",
            VoiceAgentLlmGeneratedGreetingConfiguration::fromJson,
            VoiceAgentLlmGeneratedGreetingConfiguration::getToolChoice,
            VoiceAgentLlmGeneratedGreetingConfiguration::setToolChoice);
    }

    @Test
    public void responseCreateOptionsRoundTrip() throws IOException {
        assertModelRoundTrips("{\"instructions\":\"Say hello\"}", VoiceAgentResponseCreateOptions::fromJson,
            VoiceAgentResponseCreateOptions::getToolChoice, VoiceAgentResponseCreateOptions::setToolChoice);
    }

    @Test
    public void sessionUpdateConfigurationRoundTrips() throws IOException {
        assertModelRoundTrips("{\"type\":\"realtime\"}", VoiceAgentSessionUpdateConfiguration::fromJson,
            VoiceAgentSessionUpdateConfiguration::getToolChoice, VoiceAgentSessionUpdateConfiguration::setToolChoice);
    }

    @Test
    public void sessionResponseConfigurationRoundTrips() throws IOException {
        assertModelRoundTrips("{\"id\":\"s\",\"model\":\"m\"}", VoiceAgentSessionResponseConfiguration::fromJson,
            VoiceAgentSessionResponseConfiguration::getToolChoice, null);
    }

    @Test
    public void variantsSerializeAsUnwrappedValuesAndExposeOnlySelectedValue() throws IOException {
        for (ToolChoiceOptions mode : Arrays.asList(ToolChoiceOptions.NONE, ToolChoiceOptions.AUTO,
            ToolChoiceOptions.REQUIRED)) {
            VoiceAgentToolChoice choice = VoiceAgentToolChoice.ofMode(mode);
            assertEquals("\"" + mode.asString() + "\"", UnionTypeSerializationTestUtils.serialize(choice));
            VoiceAgentToolChoice result = roundTrip(choice);
            assertEquals(mode, result.getMode());
            assertNull(result.getFunction());
            assertNull(result.getMcp());
        }

        VoiceAgentToolChoice function
            = VoiceAgentToolChoice.ofFunction(ToolChoiceFunction.builder().name("lookup").build());
        assertEquals(UnionTypeSerializationTestUtils.deserialize("{\"type\":\"function\",\"name\":\"lookup\"}",
            JsonReader::readUntyped), properties(function));
        VoiceAgentToolChoice functionResult = roundTrip(function);
        assertEquals("lookup", functionResult.getFunction().name());
        assertNull(functionResult.getMode());
        assertNull(functionResult.getMcp());

        VoiceAgentToolChoice mcp
            = VoiceAgentToolChoice.ofMcp(ToolChoiceMcp.builder().serverLabel("server").name("search").build());
        VoiceAgentToolChoice mcpResult = roundTrip(mcp);
        assertEquals("server", mcpResult.getMcp().serverLabel());
        assertNull(mcpResult.getMode());
        assertNull(mcpResult.getFunction());
    }

    @Test
    public void nullChoiceIsDistinctFromFactoriesWithNullArguments() throws IOException {
        assertNull(UnionTypeSerializationTestUtils.deserialize("null", VoiceAgentToolChoice::fromJson));
        assertThrows(NullPointerException.class, () -> VoiceAgentToolChoice.ofMode(null));
        assertThrows(NullPointerException.class, () -> VoiceAgentToolChoice.ofFunction(null));
        assertThrows(NullPointerException.class, () -> VoiceAgentToolChoice.ofMcp(null));
    }

    @Test
    public void unknownValuesArePreservedWithoutMisclassifyingObjectVariants() throws IOException {
        VoiceAgentToolChoice futureMode = VoiceAgentToolChoice.ofMode(ToolChoiceOptions.of("future_mode"));
        assertEquals("future_mode", roundTrip(futureMode).getMode().asString());
        String json = "{\"type\":\"future_tool\",\"setting\":true}";
        VoiceAgentToolChoice futureObject
            = UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentToolChoice::fromJson);
        assertEquals(json, UnionTypeSerializationTestUtils.serialize(futureObject));
        assertNull(futureObject.getMode());
        assertNull(futureObject.getFunction());
        assertNull(futureObject.getMcp());
    }

    @Test
    public void rejectsJsonValuesOutsideTheUnionShape() {
        for (String json : Arrays.asList("42", "true", "[]")) {
            assertThrows(IllegalStateException.class,
                () -> UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentToolChoice::fromJson));
        }
    }

    private static <T extends JsonSerializable<?>> void assertModelRoundTrips(String baseJson,
        UnionTypeSerializationTestUtils.Deserializer<T> deserializer, Function<T, VoiceAgentToolChoice> getter,
        BiConsumer<T, VoiceAgentToolChoice> setter) throws IOException {
        assertNull(getter.apply(UnionTypeSerializationTestUtils.deserialize(baseJson, deserializer)));
        String prefix = baseJson.substring(0, baseJson.length() - 1) + ",\"tool_choice\":";
        assertNull(getter.apply(UnionTypeSerializationTestUtils.deserialize(prefix + "null}", deserializer)));
        for (VoiceAgentToolChoice choice : Arrays.asList(VoiceAgentToolChoice.ofMode(ToolChoiceOptions.NONE),
            VoiceAgentToolChoice.ofMode(ToolChoiceOptions.AUTO),
            VoiceAgentToolChoice.ofMode(ToolChoiceOptions.REQUIRED),
            VoiceAgentToolChoice.ofFunction(ToolChoiceFunction.builder().name("lookup").build()),
            VoiceAgentToolChoice.ofMcp(ToolChoiceMcp.builder().serverLabel("server").name("search").build()))) {
            String choiceJson = UnionTypeSerializationTestUtils.serialize(choice);
            T model = UnionTypeSerializationTestUtils.deserialize(prefix + choiceJson + "}", deserializer);
            assertChoiceEquals(choice, getter.apply(model));
            if (setter != null) {
                setter.accept(model, null);
                assertNull(getter.apply(model));
                assertFalse(properties(model).containsKey("tool_choice"));
                setter.accept(model, choice);
            }
            assertEquals(UnionTypeSerializationTestUtils.deserialize(choiceJson, JsonReader::readUntyped),
                properties(model).get("tool_choice"));
            T result = UnionTypeSerializationTestUtils.deserialize(UnionTypeSerializationTestUtils.serialize(model),
                deserializer);
            assertChoiceEquals(choice, getter.apply(result));
        }
    }

    private static Map<String, Object> properties(JsonSerializable<?> model) throws IOException {
        return UnionTypeSerializationTestUtils.deserialize(UnionTypeSerializationTestUtils.serialize(model),
            reader -> reader.readMap(JsonReader::readUntyped));
    }

    private static void assertChoiceEquals(VoiceAgentToolChoice expected, VoiceAgentToolChoice actual) {
        assertNotNull(actual);
        assertEquals(expected.getMode(), actual.getMode());
        assertEquals(expected.getFunction(), actual.getFunction());
        assertEquals(expected.getMcp(), actual.getMcp());
    }

    private static VoiceAgentToolChoice roundTrip(VoiceAgentToolChoice choice) throws IOException {
        return UnionTypeSerializationTestUtils.deserialize(UnionTypeSerializationTestUtils.serialize(choice),
            VoiceAgentToolChoice::fromJson);
    }
}
