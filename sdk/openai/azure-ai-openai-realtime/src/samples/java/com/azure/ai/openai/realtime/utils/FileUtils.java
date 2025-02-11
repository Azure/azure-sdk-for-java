// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.utils;

import com.azure.ai.openai.realtime.RealtimeAsyncClient;
import com.azure.ai.openai.realtime.RealtimeClient;
import com.azure.ai.openai.realtime.models.InputAudioBufferAppendEvent;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Utility class to send files in consecutive chunks to the Realtime service.
 */
public class FileUtils {

    /**
     * Sends the audio file to the Realtime service in consecutive chunks. For async usage.
     *
     * @param client The Realtime async client.
     * @param audioFile A representation of the audio file to send
     * @return A Mono that signals success when it returns {@link Void}, or an error otherwise.
     */
    public static Mono<Void> sendAudioFileAsync(RealtimeAsyncClient client, AudioFile audioFile) {
        byte[] audioBytes;
        try {
            audioBytes = audioFile.getAudioFilesBytes();
        } catch (IOException e) {
            return Mono.error(e);
        }

        int samplesPerChunk = audioFile.getSampleRate() * audioFile.getChunkDurationMillis() / 1000;
        int bytesPerChunk = samplesPerChunk * audioFile.getBytesPerSample();
        for (int i = 0; i < audioBytes.length; i += bytesPerChunk) {
            int end = Math.min(audioBytes.length, i + bytesPerChunk);
            byte[] chunk = new byte[end - i];
            System.arraycopy(audioBytes, i, chunk, 0, end - i);
            client.sendMessage(new InputAudioBufferAppendEvent(chunk)).block();
        }

        return Mono.empty();
    }

    /**
     * Sends the audio file to the Realtime service in consecutive chunks. For sync usage.
     *
     * @param client The Realtime client.
     * @param audioFile A representation of the audio file to send
     */
    public static void sendAudioFile(RealtimeClient client, AudioFile audioFile) {
        byte[] audioBytes;
        try {
            audioBytes = audioFile.getAudioFilesBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int samplesPerChunk = audioFile.getSampleRate() * audioFile.getChunkDurationMillis() / 1000;
        int bytesPerChunk = samplesPerChunk * audioFile.getBytesPerSample();

        for (int i = 0; i < audioBytes.length; i += bytesPerChunk) {
            int end = Math.min(audioBytes.length, i + bytesPerChunk);
            byte[] chunk = new byte[end - i];
            System.arraycopy(audioBytes, i, chunk, 0, end - i);
            client.sendMessage(new InputAudioBufferAppendEvent(chunk));
        }
    }

    /**
     * Returns the {@link Path} to a file in the test resources folder.
     *
     * @param fileName The name of the file to open.
     * @return The path to the file.
     */
    public static Path openResourceFile(String fileName) {
        return Paths.get("src", "samples", "resources", fileName);
    }

    /**
     * Writes a byte array into a new file (if it doesn't exist) in append mode.
     *
     * @param destinationFile The path to the file to write to.
     * @param data The data to write.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public static void writeToFile(Path destinationFile, byte[] data) throws IOException {
        Files.write(destinationFile, data, StandardOpenOption.WRITE, StandardOpenOption.APPEND,
            StandardOpenOption.CREATE, StandardOpenOption.SYNC);
    }
}
