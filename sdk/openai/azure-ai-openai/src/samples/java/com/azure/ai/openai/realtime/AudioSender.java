package com.azure.ai.openai.realtime;

import com.azure.ai.openai.RealtimeAsyncClient;
import com.azure.ai.openai.models.realtime.RealtimeClientEventInputAudioBufferAppend;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import javax.sound.sampled.*;

public class AudioSender {

    private static final int SAMPLE_RATE = 24000;
    private static final int DURATION_MS = 100;
    private static final int BYTES_PER_SAMPLE = 2;
    private static final int SAMPLES_PER_CHUNK = SAMPLE_RATE * DURATION_MS / 1000;
    private static final int BYTES_PER_CHUNK = SAMPLES_PER_CHUNK * BYTES_PER_SAMPLE;

    public static void sendAudio(RealtimeAsyncClient client, String audioFilePath) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(audioFilePath));
        AudioFormat originalFormat = audioInputStream.getFormat();
        AudioFormat targetFormat = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

        if (!originalFormat.matches(targetFormat)) {
            audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
        }

        byte[] audioBytes = Files.readAllBytes(new File(audioFilePath).toPath());
        for (int i = 0; i < audioBytes.length; i += BYTES_PER_CHUNK) {
            int end = Math.min(audioBytes.length, i + BYTES_PER_CHUNK);
            byte[] chunk = new byte[end - i];
            System.arraycopy(audioBytes, i, chunk, 0, end - i);
            String base64Audio = Base64.getEncoder().encodeToString(chunk);
            client.sendMessage(new RealtimeClientEventInputAudioBufferAppend(base64Audio.getBytes(StandardCharsets.UTF_8)))
                    .block();
        }
    }

}