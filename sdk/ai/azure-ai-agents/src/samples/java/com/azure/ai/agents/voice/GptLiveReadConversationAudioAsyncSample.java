// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsLiveConversationsAsyncClient;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.nio.file.Path;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Asynchronously downloads persisted GPT Live merged audio and optionally a speech item's WAV to new files.
 * The session must have used {@code store=true}. The agent and conversation must still exist.
 * Wait for recording finalization after the session ends. Like {@link GptLiveReadConversationAudioSample},
 * HTTP errors propagate and BYOS audio requires separate Azure Storage credentials.
 * File writes run on boundedElastic, not an HTTP I/O thread.
 * Blocking occurs only at the standalone main method boundary. Global Reactor schedulers are not shut down.
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
public class GptLiveReadConversationAudioAsyncSample {
    public static void main(String[] args) {
        Path directory = GptLiveReadConversationAudioSample.outputDirectory(args);
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_PROJECT_ENDPOINT");
        String agentName = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_GPT_LIVE_AGENT_NAME");
        String conversationId
            = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_GPT_LIVE_CONVERSATION_ID");
        String itemId = configuration.get("FOUNDRY_GPT_LIVE_ITEM_ID");
        BetaVoiceAgentsLiveConversationsAsyncClient client = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .addPolicy(GptLiveReadConversationSample.previewHeaders())
            .buildBetaVoiceAgentsLiveConversationsAsyncClient();
        downloadAudio(client, agentName, conversationId, itemId, directory).block();
    }

    static Mono<Void> downloadAudio(BetaVoiceAgentsLiveConversationsAsyncClient client, String agentName,
        String conversationId, String itemId, Path directory) {
        if (itemId != null && itemId.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("FOUNDRY_GPT_LIVE_ITEM_ID must not be blank when set."));
        }
        Mono<Void> merged = client.getAgentConversationAudio(agentName, conversationId).flatMap(recording -> {
            GptLiveReadConversationAudioSample.printRecording(recording);
            return GptLiveReadConversationAudioSample.isFoundryManaged(recording.getBlobUri())
                ? client.downloadAgentConversationAudio(agentName, conversationId)
                    .flatMap(audio -> saveAudio(audio, directory, "gpt-live-call-"))
                : Mono.empty();
        });
        return merged.then(Mono.defer(() -> itemId == null ? Mono.empty()
            : client.getAgentConversationAudioItem(agentName, conversationId, itemId).flatMap(item -> {
                System.out.printf("Item %s: format=%s, rate=%d%n", itemId, item.getFormat(), item.getSampleRate());
                return GptLiveReadConversationAudioSample.isFoundryManaged(item.getBlobUri())
                    ? client.downloadAgentConversationAudioItem(agentName, conversationId, itemId)
                        .flatMap(audio -> saveAudio(audio, directory, "gpt-live-item-"))
                    : Mono.empty();
            })));
    }

    private static Mono<Void> saveAudio(BinaryData audio, Path directory, String prefix) {
        return Mono.fromCallable(() -> GptLiveReadConversationAudioSample.saveAudio(audio, directory, prefix))
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }
}
