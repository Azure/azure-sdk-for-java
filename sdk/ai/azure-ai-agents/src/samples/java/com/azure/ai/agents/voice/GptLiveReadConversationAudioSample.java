// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsLiveConversationsClient;
import com.azure.ai.agents.models.VoiceAudioItem;
import com.azure.ai.agents.models.VoiceRecording;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Downloads persisted GPT Live merged audio and optionally a speech item's WAV to new files.
 * The session must have used {@code store=true}. The agent and conversation must still exist.
 * Wait for recording finalization after the session ends. HTTP errors propagate: inspect Retry-After on
 * recording_not_ready (409), and distinguish missing audio (404) from recording_unavailable (409).
 * BYOS audio requires separate Azure Storage credentials; this sample does not download it.
 * No agent or persisted data is modified or deleted.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - Required. The Azure AI Project endpoint. No default.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_AGENT_NAME} - Required. The existing GPT Live agent's name. No default.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_CONVERSATION_ID} - Required. The persisted Foundry conversation ID from
 *   {@code session.started.conversation_id}, not the native session ID. No default.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_ITEM_ID} - Optional. A speech item's ID, not a delegation item.
 *   Defaults to unset, downloading only the merged conversation audio. Explicitly blank values are invalid.</li>
 * </ul>
 *
 * <p>To download an individual speech item's audio:</p>
 * <ol>
 *   <li>Run {@link GptLiveReadConversationSample} with the same project endpoint, agent name, and conversation ID.</li>
 *   <li>Find the desired transcript in its output and copy the ID from the preceding
 *   {@code Item <item-id>: type=input} or {@code Item <item-id>: type=output} line. Do not select a delegation item.</li>
 *   <li>Set {@code FOUNDRY_GPT_LIVE_ITEM_ID} to that ID, then run this sample with an existing output directory
 *   as its sole command-line argument. It downloads both the merged call audio and the selected item's WAV.</li>
 * </ol>
 * <p>For merged call audio only, skip the item lookup and leave {@code FOUNDRY_GPT_LIVE_ITEM_ID} unset.</p>
 *
 * <p>Authenticates with {@code DefaultAzureCredential}. Pass one existing output directory as the sole
 * command-line argument; there is no default directory. Treat downloaded audio as sensitive data.
 * Model deployment settings are not needed because this sample reads an existing conversation.</p>
 */
public class GptLiveReadConversationAudioSample {
    public static void main(String[] args) throws IOException {
        Path directory = outputDirectory(args);
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_PROJECT_ENDPOINT");
        String agentName = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_GPT_LIVE_AGENT_NAME");
        String conversationId
            = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_GPT_LIVE_CONVERSATION_ID");
        String itemId = configuration.get("FOUNDRY_GPT_LIVE_ITEM_ID");
        BetaVoiceAgentsLiveConversationsClient client = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .addPolicy(GptLiveReadConversationSample.previewHeaders())
            .buildBetaVoiceAgentsLiveConversationsClient();
        downloadAudio(client, agentName, conversationId, itemId, directory);
    }

    static void downloadAudio(BetaVoiceAgentsLiveConversationsClient client, String agentName,
        String conversationId, String itemId, Path directory) throws IOException {
        if (itemId != null && itemId.trim().isEmpty()) {
            throw new IllegalArgumentException("FOUNDRY_GPT_LIVE_ITEM_ID must not be blank when set.");
        }
        VoiceRecording recording = client.getAgentConversationAudio(agentName, conversationId);
        printRecording(recording);
        if (isFoundryManaged(recording.getBlobUri())) {
            saveAudio(client.downloadAgentConversationAudio(agentName, conversationId), directory, "gpt-live-call-");
        }
        if (itemId != null) {
            VoiceAudioItem item = client.getAgentConversationAudioItem(agentName, conversationId, itemId);
            System.out.printf("Item %s: format=%s, rate=%d%n", itemId, item.getFormat(), item.getSampleRate());
            if (isFoundryManaged(item.getBlobUri())) {
                saveAudio(client.downloadAgentConversationAudioItem(agentName, conversationId, itemId),
                    directory, "gpt-live-item-");
            }
        }
    }

    static void printRecording(VoiceRecording recording) {
        System.out.printf("Recording: format=%s, rate=%d, channels=%d, durationMs=%s%n",
            recording.getFormat(), recording.getSampleRate(), recording.getChannels(), recording.getDurationMs());
    }

    static boolean isFoundryManaged(String blobUri) {
        if (blobUri == null || blobUri.isEmpty()) {
            return true;
        }
        System.out.println("BYOS audio: use metadata.getBlobUri() with your own Azure Storage credentials.");
        return false;
    }

    static Path saveAudio(BinaryData audio, Path directory, String prefix) throws IOException {
        Path output = Files.createTempFile(directory, prefix, ".wav");
        try (OutputStream stream = Files.newOutputStream(output)) {
            audio.writeTo(stream);
        } catch (IOException | RuntimeException error) {
            try {
                Files.deleteIfExists(output);
            } catch (IOException cleanupError) {
                error.addSuppressed(cleanupError);
            }
            throw error;
        }
        System.out.println("Saved WAV: " + output.toAbsolutePath());
        return output;
    }

    static Path outputDirectory(String[] args) {
        if (args.length != 1 || !Files.isDirectory(Paths.get(args[0]))) {
            throw new IllegalArgumentException("Pass one existing output directory as the command-line argument.");
        }
        return Paths.get(args[0]);
    }
}
