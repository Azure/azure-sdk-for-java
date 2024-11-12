// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation;

import com.azure.ai.openai.realtime.RealtimeAsyncClient;
import com.azure.ai.openai.realtime.RealtimeClient;
import com.azure.ai.openai.realtime.models.RealtimeClientEventInputAudioBufferAppend;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    private static final int SAMPLE_RATE = 24000;
    private static final int DURATION_MS = 100;
    private static final int BYTES_PER_SAMPLE = 2;
    private static final int SAMPLES_PER_CHUNK = SAMPLE_RATE * DURATION_MS / 1000;

    // Increasing the buffer size per chunk will trigger the server to send more `response.audio.append` events
    private static final int BYTES_PER_CHUNK = 4 * SAMPLES_PER_CHUNK * BYTES_PER_SAMPLE;

    public static Mono<Void> sendAudioFileAsync(RealtimeAsyncClient client, Path audioFile) {
        byte[] audioBytes;
        try {
            audioBytes = Files.readAllBytes(audioFile);
        } catch (IOException e) {
            return Mono.error(e);
        }

        for (int i = 0; i < audioBytes.length; i += BYTES_PER_CHUNK) {
            int end = Math.min(audioBytes.length, i + BYTES_PER_CHUNK);
            byte[] chunk = new byte[end - i];
            System.arraycopy(audioBytes, i, chunk, 0, end - i);
            client.sendMessage(new RealtimeClientEventInputAudioBufferAppend(chunk))
                    .block();
        }

        return Mono.empty();
    }

    public static void sendAudioFile(RealtimeClient client, Path audioFile) {
        byte[] audioBytes;
        try {
            audioBytes = Files.readAllBytes(audioFile);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }

        for (int i = 0; i < audioBytes.length; i += BYTES_PER_CHUNK) {
            int end = Math.min(audioBytes.length, i + BYTES_PER_CHUNK);
            byte[] chunk = new byte[end - i];
            System.arraycopy(audioBytes, i, chunk, 0, end - i);
            client.sendMessage(new RealtimeClientEventInputAudioBufferAppend(chunk));
        }
    }

    public static Path openResourceFile(String fileName) {
        return Paths.get("src", "test", "resources", fileName);
    }
}
