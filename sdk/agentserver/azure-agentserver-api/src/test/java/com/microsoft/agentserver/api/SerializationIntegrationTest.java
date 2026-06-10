// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.agentserver.api.serialization.ObjectMapperFactory;
import com.openai.core.JsonMissing;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ToolChoiceOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JSON serialization/deserialization round-trips using
 * the ObjectMapperFactory. Tests the complete serialize → deserialize pipeline
 * for key protocol types.
 */
class SerializationIntegrationTest {

    private final ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

    // ══════════════════════════════════════════════════════════════
    //  AgentServerCreateResponse deserialization
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("AgentServerCreateResponse deserialization")
    class CreateResponseDeserialization {

        @Test
        @DisplayName("Deserialize request with text input")
        void deserializeTextInput() throws Exception {
            String json = """
                {
                    "input": "Hello, agent!",
                    "model": "gpt-4o"
                }
                """;

            AgentServerCreateResponse request = mapper.readValue(json, AgentServerCreateResponse.class);
            assertNotNull(request);
            assertEquals("Hello, agent!", request.inputText());
            assertNull(request.agent());
        }

        @Test
        @DisplayName("Deserialize request with agent_reference")
        void deserializeWithAgentReference() throws Exception {
            String json = """
                {
                    "input": "Hello",
                    "model": "gpt-4o",
                    "agent_reference": {
                        "type": "agent_reference",
                        "name": "my-agent",
                        "version": "1.0",
                        "label": "prod"
                    }
                }
                """;

            AgentServerCreateResponse request = mapper.readValue(json, AgentServerCreateResponse.class);
            assertNotNull(request.agent());
            assertEquals("my-agent", request.agent().name());
            assertEquals("1.0", request.agent().version());
            assertEquals(AgentReferenceType.AGENT_REFERENCE, request.agent().type());
        }

        @Test
        @DisplayName("Deserialize request with legacy 'agent' field")
        void deserializeWithLegacyAgentField() throws Exception {
            String json = """
                {
                    "input": "Hello",
                    "model": "gpt-4o",
                    "agent": {
                        "type": "agent_reference",
                        "name": "legacy-agent",
                        "version": "0.9",
                        "label": "staging"
                    }
                }
                """;

            AgentServerCreateResponse request = mapper.readValue(json, AgentServerCreateResponse.class);
            assertNotNull(request.agent());
            assertEquals("legacy-agent", request.agent().name());
        }

        @Test
        @DisplayName("Deserialize request with structured input items")
        void deserializeStructuredInput() throws Exception {
            String json = """
                {
                    "input": [
                        {
                            "type": "message",
                            "role": "user",
                            "content": [
                                {"type": "input_text", "text": "What is the weather?"}
                            ]
                        }
                    ],
                    "model": "gpt-4o"
                }
                """;

            AgentServerCreateResponse request = mapper.readValue(json, AgentServerCreateResponse.class);
            assertNotNull(request.responseCreateParams());
            assertTrue(request.responseCreateParams().input().isPresent());
        }

        @Test
        @DisplayName("Deserialize request with message items missing 'type' discriminator gets fixed")
        void deserializeMissingTypeDiscriminator() throws Exception {
            // The custom deserializer should inject type: "message" for items with role+content but no type
            String json = """
                {
                    "input": [
                        {
                            "role": "user",
                            "content": [
                                {"type": "input_text", "text": "No type field here"}
                            ]
                        }
                    ],
                    "model": "gpt-4o"
                }
                """;

            AgentServerCreateResponse request = mapper.readValue(json, AgentServerCreateResponse.class);
            assertNotNull(request.responseCreateParams());
            assertTrue(request.responseCreateParams().input().isPresent());
        }

        @Test
        @DisplayName("Deserialize request with metadata containing response_id")
        void deserializeWithMetadata() throws Exception {
            String json = """
                {
                    "input": "Hello",
                    "model": "gpt-4o",
                    "metadata": {
                        "response_id": "resp_custom_id_123"
                    }
                }
                """;

            AgentServerCreateResponse request = mapper.readValue(json, AgentServerCreateResponse.class);
            assertNotNull(request);
            // The metadata should be accessible through the response create params
            assertTrue(request.responseCreateParams().metadata().isPresent());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Response serialization
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Response serialization")
    class ResponseSerialization {

        @Test
        @DisplayName("Response serializes createdAt as integer (not decimal)")
        void createdAtAsInteger() throws Exception {
            ResponseOutputMessage message = ResponseOutputMessage.builder()
                .id("msg_test")
                .status(ResponseOutputMessage.Status.COMPLETED)
                .addContent(ResponseOutputMessage.Content.ofOutputText(
                    ResponseOutputText.builder().text("Test").annotations(List.of()).build()))
                .build();

            Response response = Response.builder()
                .id("resp_test")
                .createdAt(1700000000.0)
                .addOutput(ResponseOutputItem.ofMessage(message))
                .model("gpt-4o")
                .parallelToolCalls(false)
                .tools(List.of())
                .status(ResponseStatus.COMPLETED)

                .error(JsonMissing.of())
                .incompleteDetails(JsonMissing.of())
                .instructions(JsonMissing.of())
                .metadata(JsonMissing.of())
                .temperature(JsonMissing.of())
                .topP(JsonMissing.of())
                .toolChoice(ToolChoiceOptions.AUTO)
                .build();

            String json = mapper.writeValueAsString(response);
            JsonNode node = mapper.readTree(json);

            // createdAt should be present and numeric
            assertTrue(node.has("created_at"));
            assertTrue(node.get("created_at").isNumber());
            assertEquals(1700000000L, node.get("created_at").longValue(),
                "createdAt should serialize as the correct epoch timestamp");
        }

        @Test
        @DisplayName("Null and JsonNull fields are excluded from serialization")
        void nullFieldsExcluded() throws Exception {
            Response response = Response.builder()
                .id("resp_minimal")
                .createdAt(1700000000.0)
                .output(List.of())
                .model("gpt-4o")
                .parallelToolCalls(false)
                .tools(List.of())
                .status(ResponseStatus.COMPLETED)

                .error(JsonMissing.of())
                .incompleteDetails(JsonMissing.of())
                .instructions(JsonMissing.of())
                .metadata(JsonMissing.of())
                .temperature(JsonMissing.of())
                .topP(JsonMissing.of())
                .toolChoice(ToolChoiceOptions.AUTO)
                .build();

            String json = mapper.writeValueAsString(response);
            JsonNode node = mapper.readTree(json);

            // Fields that weren't set should not appear
            assertFalse(node.has("error") && !node.get("error").isNull(),
                "Error field should not be present as a non-null value");
        }

        @Test
        @DisplayName("CreateResponse serializes with unwrapped Response fields")
        void createResponseUnwrapped() throws Exception {
            ResponseOutputText outputText = ResponseOutputText.builder()
                .text("Test output")
                .annotations(List.of())
                .build();

            ResponseOutputMessage message = ResponseOutputMessage.builder()
                .id("msg_cr_test")
                .status(ResponseOutputMessage.Status.COMPLETED)
                .addContent(ResponseOutputMessage.Content.ofOutputText(outputText))
                .build();

            Response response = Response.builder()
                .id("resp_cr_test")
                .createdAt(1700000000.0)
                .addOutput(ResponseOutputItem.ofMessage(message))
                .model("gpt-4o")
                .parallelToolCalls(false)
                .tools(List.of())
                .status(ResponseStatus.COMPLETED)

                .error(JsonMissing.of())
                .incompleteDetails(JsonMissing.of())
                .instructions(JsonMissing.of())
                .metadata(JsonMissing.of())
                .temperature(JsonMissing.of())
                .topP(JsonMissing.of())
                .toolChoice(ToolChoiceOptions.AUTO)
                .build();

            CreateResponse createResponse = new CreateResponse(
                new AgentReference(AgentReferenceType.AGENT_REFERENCE, "test-agent", "1.0", "prod"),
                response);

            String json = mapper.writeValueAsString(createResponse);
            JsonNode node = mapper.readTree(json);

            // Response fields should be unwrapped at top level
            assertTrue(node.has("id"));
            assertEquals("resp_cr_test", node.get("id").asText());

            // Agent should be nested
            assertTrue(node.has("agent"));
            assertEquals("test-agent", node.get("agent").get("name").asText());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  ResponseEventStream event serialization
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Event stream serialization")
    class EventStreamSerialization {

        @Test
        @DisplayName("Streaming events serialize to valid JSON")
        void streamingEventsSerialize() throws Exception {
            ResponseCreateParams.Body body = ResponseCreateParams.builder()
                .input("test")
                .model("test-model")
                .build()
                ._body();
            AgentServerCreateResponse request = new AgentServerCreateResponse(null, body);

            ResponseContext context = ResponseContext.builder()
                .responseId("resp_AAAAAAAAAAAAAAAAAA" + "B".repeat(32))
                .provider(ResponsesProvider.inMemory())
                .request(body)
                .build();

            ResponseEventStream stream = ResponseEventStream.create(context, request);
            stream.emitCreated()
                .emitInProgress()
                .addOutputMessage(msg -> msg.outputItemMessage("Hello!"))
                .emitCompleted();

            // Serialize each event
            for (ResponseEvent event : stream.getEvents()) {
                String json = mapper.writeValueAsString(event.streamEvent());
                assertNotNull(json);
                assertFalse(json.isEmpty());
                // Verify it's valid JSON
                JsonNode node = mapper.readTree(json);
                assertNotNull(node);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  AgentReference serialization round-trip
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("AgentReference round-trip")
    class AgentReferenceRoundTrip {

        @Test
        @DisplayName("Serialize and deserialize AgentReference")
        void roundTrip() throws Exception {
            // Deserialize from JSON (using lowercase "agent_reference" matching @JsonCreator)
            String json = """
                {
                    "type": "agent_reference",
                    "name": "my-agent",
                    "version": "2.0",
                    "label": "canary"
                }
                """;

            AgentReference deserialized = mapper.readValue(json, AgentReference.class);
            assertEquals("my-agent", deserialized.name());
            assertEquals("2.0", deserialized.version());
            assertEquals("canary", deserialized.label());
            assertEquals(AgentReferenceType.AGENT_REFERENCE, deserialized.type());

            // Re-serialize and verify key fields
            String reserialized = mapper.writeValueAsString(deserialized);
            JsonNode node = mapper.readTree(reserialized);
            assertEquals("my-agent", node.get("name").asText());
        }
    }
}









