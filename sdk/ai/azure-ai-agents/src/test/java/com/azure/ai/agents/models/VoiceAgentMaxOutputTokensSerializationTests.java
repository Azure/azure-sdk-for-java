// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentMaxOutputTokensSerializationTests {
    @Test
    public void countsSerializeAsJsonIntegers() throws IOException {
        for (int count : new int[] { 1, 8192, Integer.MAX_VALUE }) {
            VoiceAgentMaxOutputTokens limit = VoiceAgentMaxOutputTokens.of(count);
            String json = UnionTypeSerializationTestUtils.serialize(limit);
            assertEquals(Integer.toString(count), json);
            VoiceAgentMaxOutputTokens result
                = UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentMaxOutputTokens::fromJson);
            assertEquals(count, result.asInteger());
            assertFalse(result.isMaximum());
        }
    }

    @Test
    public void maximumSerializesAsInfString() throws IOException {
        String json = UnionTypeSerializationTestUtils.serialize(VoiceAgentMaxOutputTokens.MAXIMUM);
        assertEquals("\"inf\"", json);
        VoiceAgentMaxOutputTokens result
            = UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentMaxOutputTokens::fromJson);
        assertTrue(result.isMaximum());
        assertNull(result.asInteger());
    }

    @Test
    public void unknownStringsArePreservedWithoutBecomingCountsOrMaximum() throws IOException {
        for (String json : Arrays.asList("\"future_limit\"", "\"1024\"")) {
            VoiceAgentMaxOutputTokens limit
                = UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentMaxOutputTokens::fromJson);
            assertNull(limit.asInteger());
            assertFalse(limit.isMaximum());
            assertEquals(json, UnionTypeSerializationTestUtils.serialize(limit));
        }
    }

    @Test
    public void nullIsUnsetAndOtherJsonShapesAreRejected() throws IOException {
        assertNull(UnionTypeSerializationTestUtils.deserialize("null", VoiceAgentMaxOutputTokens::fromJson));
        for (String json : Arrays.asList("true", "[]", "{}", "1.5", "2147483648", "-2147483649")) {
            assertThrows(IllegalStateException.class,
                () -> UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentMaxOutputTokens::fromJson));
        }
    }

    @Test
    public void voiceAgentDefinitionRoundTrips() throws IOException {
        assertModelRoundTrips("{\"kind\":\"voice\"}", "max_output_tokens", VoiceAgentDefinition::fromJson,
            VoiceAgentDefinition::getMaxOutputTokens, VoiceAgentDefinition::setMaxOutputTokens);
    }

    @Test
    public void inlineVoiceSubagentRoundTrips() throws IOException {
        assertModelRoundTrips("{\"id\":\"specialist\",\"description\":\"Specialist\"}", "max_response_output_tokens",
            InlineVoiceSubagent::fromJson, InlineVoiceSubagent::getMaxResponseOutputTokens,
            InlineVoiceSubagent::setMaxResponseOutputTokens);
    }

    @Test
    public void realtimeSessionConfigurationRoundTrips() throws IOException {
        assertModelRoundTrips("{\"type\":\"realtime\"}", "max_output_tokens", RealtimeSessionConfiguration::fromJson,
            RealtimeSessionConfiguration::getMaxOutputTokens, RealtimeSessionConfiguration::setMaxOutputTokens);
    }

    @Test
    public void responseCreateOptionsRoundTrip() throws IOException {
        assertModelRoundTrips("{\"instructions\":\"Say hello\"}", "max_output_tokens",
            VoiceAgentResponseCreateOptions::fromJson, VoiceAgentResponseCreateOptions::getMaxOutputTokens,
            VoiceAgentResponseCreateOptions::setMaxOutputTokens);
    }

    @Test
    public void sessionUpdateConfigurationRoundTrips() throws IOException {
        assertModelRoundTrips("{\"type\":\"realtime\"}", "max_output_tokens",
            VoiceAgentSessionUpdateConfiguration::fromJson, VoiceAgentSessionUpdateConfiguration::getMaxOutputTokens,
            VoiceAgentSessionUpdateConfiguration::setMaxOutputTokens);
    }

    @Test
    public void sessionResponseConfigurationRoundTrips() throws IOException {
        assertModelRoundTrips("{\"id\":\"s\",\"model\":\"m\"}", "max_output_tokens",
            VoiceAgentSessionResponseConfiguration::fromJson,
            VoiceAgentSessionResponseConfiguration::getMaxOutputTokens, null);
    }

    @Test
    public void realtimeResponseRoundTrips() throws IOException {
        assertModelRoundTrips("{\"id\":\"r\"}", "max_output_tokens", VoiceAgentRealtimeResponse::fromJson,
            VoiceAgentRealtimeResponse::getMaxOutputTokens, null);
    }

    @Test
    public void persistedResponseRoundTrips() throws IOException {
        assertModelRoundTrips("{\"id\":\"r\",\"conversation_id\":\"c\"}", "max_output_tokens", VoiceResponse::fromJson,
            VoiceResponse::getMaxOutputTokens, null);
    }

    private static <T extends JsonSerializable<?>> void assertModelRoundTrips(String baseJson, String property,
        UnionTypeSerializationTestUtils.Deserializer<T> deserializer, Function<T, VoiceAgentMaxOutputTokens> getter,
        BiConsumer<T, VoiceAgentMaxOutputTokens> setter) throws IOException {
        T unset = UnionTypeSerializationTestUtils.deserialize(baseJson, deserializer);
        assertNull(getter.apply(unset));
        assertFalse(properties(unset).containsKey(property));
        String prefix = baseJson.substring(0, baseJson.length() - 1) + ",\"" + property + "\":";
        assertNull(getter.apply(UnionTypeSerializationTestUtils.deserialize(prefix + "null}", deserializer)));

        for (String limitJson : Arrays.asList("1024", "\"inf\"", "\"future_limit\"", "\"1024\"")) {
            VoiceAgentMaxOutputTokens limit
                = UnionTypeSerializationTestUtils.deserialize(limitJson, VoiceAgentMaxOutputTokens::fromJson);
            T model = UnionTypeSerializationTestUtils.deserialize(prefix + limitJson + "}", deserializer);
            assertLimitEquals(limit, getter.apply(model));
            if (setter != null) {
                setter.accept(model, null);
                assertNull(getter.apply(model));
                assertFalse(properties(model).containsKey(property));
                setter.accept(model, limit);
                assertLimitEquals(limit, getter.apply(model));
            }
            assertEquals(UnionTypeSerializationTestUtils.deserialize(limitJson, JsonReader::readUntyped),
                properties(model).get(property));
            T result = UnionTypeSerializationTestUtils.deserialize(UnionTypeSerializationTestUtils.serialize(model),
                deserializer);
            assertLimitEquals(limit, getter.apply(result));
        }
    }

    private static void assertLimitEquals(VoiceAgentMaxOutputTokens expected, VoiceAgentMaxOutputTokens actual)
        throws IOException {
        assertNotNull(actual);
        assertEquals(expected.asInteger(), actual.asInteger());
        assertEquals(expected.isMaximum(), actual.isMaximum());
        assertEquals(UnionTypeSerializationTestUtils.serialize(expected),
            UnionTypeSerializationTestUtils.serialize(actual));
    }

    private static Map<String, Object> properties(JsonSerializable<?> model) throws IOException {
        return UnionTypeSerializationTestUtils.deserialize(UnionTypeSerializationTestUtils.serialize(model),
            reader -> reader.readMap(JsonReader::readUntyped));
    }
}
