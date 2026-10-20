// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.LiveInitialSessionAudioParam;
import com.azure.ai.agents.models.LiveResponsesDelegationParam;
import com.azure.ai.agents.models.LiveResponsesDelegationSettingsInputParam;
import com.azure.ai.agents.models.LiveSessionAudioFormatPCMParam;
import com.azure.ai.agents.models.LiveSessionAudioFormatPCMParamRate;
import com.azure.ai.agents.models.VoiceAgentLiveDefinition;
import com.azure.ai.agents.models.VoiceAgentSubProtocol;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

final class GptLiveSampleUtils {
    static final Duration TIMEOUT = Duration.ofSeconds(60);

    private GptLiveSampleUtils() {
    }

    static Map<?, ?> object(BinaryData data) {
        return data.toObject(Map.class);
    }

    static Map<?, ?> object(Object value) {
        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Expected a native Live JSON object: " + value);
        }
        return (Map<?, ?>) value;
    }

    static String type(Map<?, ?> event) {
        return string(event, "type");
    }

    static String string(Map<?, ?> event, String field) {
        Object value = event.get(field);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Missing or invalid Live event field: " + field);
        }
        return (String) value;
    }

    static BinaryData event(String type) {
        return event(type, null, null);
    }

    static BinaryData event(String type, String field, Object value) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", type);
        if (field != null) {
            event.put(field, value);
        }
        return BinaryData.fromObject(event);
    }

    static BinaryData userMessage(String text) {
        Map<String, Object> part = new LinkedHashMap<>();
        part.put("type", "input_text");
        part.put("text", text);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", "message");
        item.put("role", "user");
        item.put("content", java.util.Collections.singletonList(part));
        return event("response.item.create", "item", item);
    }

    static BinaryData audio(byte[] pcm) {
        return event("session.input_audio.append", "audio", Base64.getEncoder().encodeToString(pcm));
    }

    static BinaryData commentary(String content) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
            JsonWriter writer = JsonProviders.createWriter(output)) {
            writer.writeStartObject()
                .writeStringField("type", "session.commentary.append")
                .writeStringField("content", content)
                .writeNullField("delegation_id")
                .writeEndObject()
                .flush();
            return BinaryData.fromBytes(output.toByteArray());
        } catch (IOException error) {
            throw new IllegalStateException("Unable to serialize GPT Live commentary.", error);
        }
    }

    static Map<?, ?> backendEvent(Map<?, ?> event) {
        return "response.event".equals(type(event)) ? object(event.get("event")) : event;
    }

    static void checkError(Map<?, ?> event) {
        if ("error".equals(type(event)) || "response.failed".equals(type(event))
            || "response.incomplete".equals(type(event))) {
            Object error = event.get("error");
            if (error instanceof Map<?, ?> && ("content_filter".equals(((Map<?, ?>) error).get("code"))
                || "content_filter".equals(((Map<?, ?>) error).get("type")))) {
                throw new IllegalStateException("GPT Live service error: generation stopped by service moderation "
                    + "(content_filter). The call has ended; review the conversation and service diagnostics. Event: "
                    + BinaryData.fromObject(event));
            }
            throw new IllegalStateException("GPT Live service error: " + BinaryData.fromObject(event));
        }
    }

    static void printEvent(Map<?, ?> event) {
        checkError(event);
        if ("session.started".equals(type(event))) {
            Object conversationId = event.get("conversation_id");
            System.out.println(conversationId == null ? "Live session started."
                : "Live session started; conversation_id=" + conversationId);
        } else if ("session.input_transcript.delta".equals(type(event))) {
            System.out.print(string(event, "delta"));
        } else if ("session.output_transcript.delta".equals(type(event))) {
            System.out.print(string(event, "delta"));
        } else if ("response.event".equals(type(event))) {
            Map<?, ?> backend = backendEvent(event);
            checkError(backend);
            if ("response.output_text.delta".equals(type(backend))) {
                System.out.print(string(backend, "delta"));
            }
        }
    }

    static final class AudioTranscriptPrinter {
        static final int MAX_BUFFERED_TRANSCRIPT_CHARACTERS = 65536;
        private String speaker;
        private boolean readingInput;
        private boolean lineOpen;
        private final boolean inputEchoed;
        private final boolean completeUtterances;
        private final StringBuilder pending = new StringBuilder();
        private final StringBuilder utterance = new StringBuilder();

        AudioTranscriptPrinter() {
            this(false);
        }

        AudioTranscriptPrinter(boolean inputEchoed) {
            this(inputEchoed, false);
        }

        AudioTranscriptPrinter(boolean inputEchoed, boolean completeUtterances) {
            this.inputEchoed = inputEchoed;
            this.completeUtterances = completeUtterances;
        }

        synchronized void beginInput() {
            if (readingInput) {
                throw new IllegalStateException("A console read is already active.");
            }
            if (lineOpen) {
                System.out.println();
            }
            speaker = null;
            lineOpen = false;
            readingInput = true;
            System.out.print("You: ");
            System.out.flush();
        }

        synchronized void endInput() {
            if (!readingInput) {
                return;
            }
            readingInput = false;
            if (!inputEchoed) {
                System.out.println();
            }
            if (pending.length() > 0) {
                System.out.print(pending);
                pending.setLength(0);
            }
            speaker = null;
        }

        synchronized void beginTurn() {
            speaker = null;
        }

        synchronized void endUtterance() {
            if (utterance.length() == 0) {
                return;
            }
            String spokenText = utterance.toString().trim();
            if (spokenText.isEmpty()) {
                utterance.setLength(0);
                return;
            }
            String text = (lineOpen ? System.lineSeparator() : "") + "Assistant: " + spokenText
                + System.lineSeparator();
            if (readingInput && text.length() > MAX_BUFFERED_TRANSCRIPT_CHARACTERS - pending.length()) {
                throw new IllegalStateException("Buffered transcript exceeded 65536 characters while reading input.");
            }
            utterance.setLength(0);
            speaker = null;
            write(text);
        }

        synchronized void print(Map<?, ?> event) {
            checkError(event);
            String eventType = type(event);
            if ("response.event".equals(eventType)) {
                checkError(backendEvent(event));
            } else if ("session.input_transcript.delta".equals(eventType)
                || "session.output_transcript.delta".equals(eventType)) {
                if (completeUtterances && "session.output_transcript.delta".equals(eventType)) {
                    String delta = string(event, "delta");
                    if (delta.length() > MAX_BUFFERED_TRANSCRIPT_CHARACTERS - pending.length() - utterance.length()) {
                        throw new IllegalStateException("Buffered transcript exceeded 65536 characters.");
                    }
                    utterance.append(delta);
                    return;
                }
                String nextSpeaker = "session.input_transcript.delta".equals(eventType) ? "You" : "Assistant";
                if (!nextSpeaker.equals(speaker)) {
                    write((lineOpen ? System.lineSeparator() : "") + nextSpeaker + ": ");
                    speaker = nextSpeaker;
                }
                write(string(event, "delta"));
            } else if ("session.started".equals(eventType)) {
                printEvent(event);
            } else if ("session.closed".equals(eventType)) {
                endUtterance();
                if (lineOpen) {
                    write(System.lineSeparator());
                }
                speaker = null;
            }
        }

        private void write(String text) {
            if (readingInput) {
                if (text.length() > MAX_BUFFERED_TRANSCRIPT_CHARACTERS - pending.length() - utterance.length()) {
                    throw new IllegalStateException("Buffered transcript exceeded 65536 characters while reading input.");
                }
                pending.append(text);
            } else {
                System.out.print(text);
            }
            if (!text.isEmpty()) {
                lineOpen = !text.endsWith("\n");
            }
        }
    }

    static AgentResource createAgent(AgentsClientBuilder builder, String model, String backendModel,
        String scenario, boolean tools, boolean keepAgent) {
        LiveResponsesDelegationSettingsInputParam responses = new LiveResponsesDelegationSettingsInputParam(backendModel)
                .setInstructions(tools
                    ? "Use get_weather for weather requests. After receiving its result, answer briefly."
                    : "Answer the user briefly and helpfully.");
        if (tools) {
            responses.setTools(java.util.Collections.singletonList(GptLiveFunctionToolSample.weatherTool()));
        }
        VoiceAgentLiveDefinition definition = new VoiceAgentLiveDefinition(VoiceAgentSubProtocol.LIVE, model)
                .setInstructions("You are a helpful voice assistant. Delegate requests to your Responses backend.")
                .setAudio(new LiveInitialSessionAudioParam().setFormat(new LiveSessionAudioFormatPCMParam(
                    LiveSessionAudioFormatPCMParamRate.TWO_FOUR_ZERO_ZERO_ZERO)))
                .setDelegation(BinaryData.fromObject(new LiveResponsesDelegationParam(responses)));
        String agentName = createAgentName(scenario);
        AgentsClient agents = builder.buildAgentsClient();
        agents.createAgentVersion(agentName, new CreateAgentVersionInput(definition));
        System.out.println("Created GPT Live agent: " + agentName);
        return new AgentResource(builder, agents, agentName, keepAgent);
    }

    static String createAgentName(String scenario) {
        return "gpt-live-" + scenario + "-" + UUID.randomUUID();
    }

    static final class AgentResource implements AutoCloseable {
        final AgentsClientBuilder builder;
        final String name;
        private final AgentsClient agents;
        private final boolean keep;

        AgentResource(AgentsClientBuilder builder, AgentsClient agents, String name, boolean keep) {
            this.builder = builder;
            this.agents = agents;
            this.name = name;
            this.keep = keep;
        }

        @Override
        public void close() {
            if (keep) {
                System.out.println("Kept GPT Live agent: " + name);
            } else {
                agents.deleteAgent(name);
                System.out.println("Deleted GPT Live agent: " + name);
            }
        }
    }
}
