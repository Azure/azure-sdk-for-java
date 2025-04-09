// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.realtime.utils;

import com.azure.core.annotation.Fluent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility representation gathering the data needed to send files to the Realtime service.
 */
@Fluent
public class AudioFile {

    /**
     * The path to the audio file.
     */
    private final Path filePath;

    /**
     * The sample rate of the audio file. Defaults to 24 kHz.
     */
    private int sampleRate = 24000;

    /**
     * The number of bytes per sample. Defaults to 2.
     */
    private int bytesPerSample = 2;

    /**
     * The duration of each chunk in milliseconds in which the file will be separated for sending. Defaults to 100 ms.
     */
    private int chunkDurationMillis = 100;

    /**
     * Creates an instance of AudioFile.
     *
     * @param filePath path to file
     */
    public AudioFile(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Gets the sample rate of the audio file.
     * @return the sample rate
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Sets the sample rate of the audio file.
     * @param sampleRate the sample rate
     * @return the AudioFile object itself
     */
    public AudioFile setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    /**
     * Gets the number of bytes per sample.
     * @return the number of bytes per sample
     */
    public int getBytesPerSample() {
        return bytesPerSample;
    }

    /**
     * Sets the number of bytes per sample.
     * @param bytesPerSample the number of bytes per sample
     * @return the AudioFile object itself
     */
    public AudioFile setBytesPerSample(int bytesPerSample) {
        this.bytesPerSample = bytesPerSample;
        return this;
    }

    /**
     * Gets the duration of each chunk in milliseconds in which the file will be separated for sending.
     * @return the duration of each chunk in milliseconds
     */
    public int getChunkDurationMillis() {
        return chunkDurationMillis;
    }

    /**
     * Sets the duration of each chunk in milliseconds in which the file will be separated for sending.
     * @param chunkDurationMillis the duration of each chunk in milliseconds
     * @return the AudioFile object itself
     */
    public AudioFile setChunkDurationMillis(int chunkDurationMillis) {
        this.chunkDurationMillis = chunkDurationMillis;
        return this;
    }

    /**
     * Gets the path to the audio file.
     * @return the path to the audio file
     */
    public Path getFilePath() {
        return filePath;
    }

    /**
     * Gets the audio file bytes.
     * @return the audio file bytes
     * @throws IOException if the file cannot be read
     */
    public byte[] getAudioFilesBytes() throws IOException {
        return Files.readAllBytes(filePath);
    }
}
