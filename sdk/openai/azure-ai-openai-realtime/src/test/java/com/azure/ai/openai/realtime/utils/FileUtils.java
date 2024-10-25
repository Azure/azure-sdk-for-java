package com.azure.ai.openai.realtime.utils;

import com.azure.ai.openai.realtime.RealtimeAsyncClient;
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
    private static final int BYTES_PER_CHUNK = 4 * SAMPLES_PER_CHUNK * BYTES_PER_SAMPLE;

    public static Mono<Void> sendAudioFile(RealtimeAsyncClient client, Path audioFile) {
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

    public static Path openResourceFile(String fileName) {
        return Paths.get("src", "test", "resources", fileName);
    }
}
