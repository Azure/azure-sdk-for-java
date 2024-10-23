package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.models.RealtimeClientEventInputAudioBufferAppend;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AudioSender {

    private static final int SAMPLE_RATE = 24000;
    private static final int DURATION_MS = 100;
    private static final int BYTES_PER_SAMPLE = 2;
    private static final int SAMPLES_PER_CHUNK = SAMPLE_RATE * DURATION_MS / 1000;
    private static final int BYTES_PER_CHUNK = SAMPLES_PER_CHUNK * BYTES_PER_SAMPLE;

    public static Mono<Void> sendAudio(RealtimeAsyncClient client, Path audioFile) {
//        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(audioFilePath));
//        AudioFormat originalFormat = audioInputStream.getFormat();
//        AudioFormat targetFormat = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

//        if (!originalFormat.matches(targetFormat)) {
//            audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
//        }

        byte[] audioBytes = null;
        try {
            audioBytes = Files.readAllBytes(audioFile);
        } catch (IOException e) {
            return Mono.error(e);
        }

        // UklGRn ... dXV1Q==

        for (int i = 0; i < audioBytes.length; i += BYTES_PER_CHUNK) {
            int end = Math.min(audioBytes.length, i + BYTES_PER_CHUNK);
            byte[] chunk = new byte[end - i];
            System.arraycopy(audioBytes, i, chunk, 0, end - i);
//            byte[] base64Audio = Base64.getEncoder().encode(chunk);
            client.sendMessage(new RealtimeClientEventInputAudioBufferAppend(chunk))
                    .block();
        }

        return Mono.empty();
    }

}