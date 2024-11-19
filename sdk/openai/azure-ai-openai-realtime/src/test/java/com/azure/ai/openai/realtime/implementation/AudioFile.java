package com.azure.ai.openai.realtime.implementation;

import com.azure.core.annotation.Fluent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Fluent
public class AudioFile {

    private final Path filePath;

    private int sampleRate = 24000;
    private int bytesPerSample = 2;
    private int chunkDurationMillis = 100;

    public AudioFile(Path filePath) {
        this.filePath = filePath;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public AudioFile setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public int getBytesPerSample() {
        return bytesPerSample;
    }

    public AudioFile setBytesPerSample(int bytesPerSample) {
        this.bytesPerSample = bytesPerSample;
        return this;
    }

    public int getChunkDurationMillis() {
        return chunkDurationMillis;
    }

    public AudioFile setChunkDurationMillis(int chunkDurationMillis) {
        this.chunkDurationMillis = chunkDurationMillis;
        return this;
    }

    public Path getFilePath() {
        return filePath;
    }

    public byte[] getAudioFilesBytes() throws IOException {
        return Files.readAllBytes(filePath);
    }
}
