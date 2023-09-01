package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.AudioTranslationOptions;
import com.azure.core.util.BinaryData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MultipartDataHelper {
    private final String boundaryId = UUID.randomUUID().toString().substring(0, 16);

    private final String boundary = "--------------------------" + boundaryId;
    private final String endBoundary = boundary + "--";

    public String getBoundary() {
        return boundary;
    }

    public SerializationResult serializeAudioTranscriptionOption (
        AudioTranslationOptions audioTranscriptionOptions, String fileName) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // File
        String fileFieldPreamble = boundary
            + "Content-Disposition: form-data; name=\"file\"; filename=\""
            + fileName + "\" "
            + "Content-Type: application/octet-stream ";
        try {
            byteArrayOutputStream.write(fileFieldPreamble.getBytes(StandardCharsets.UTF_8));
            byteArrayOutputStream.write(audioTranscriptionOptions.getFile());
            byteArrayOutputStream.write(endBoundary.getBytes(StandardCharsets.UTF_8));

            int a =  byteArrayOutputStream.toByteArray().length;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] totalData = byteArrayOutputStream.toByteArray();
        return new SerializationResult(BinaryData.fromBytes(totalData), totalData.length);
    }

    public class SerializationResult {
        private final  int dataLength;
        private final BinaryData data;

        public SerializationResult(BinaryData data, int contentLength) {
            this.dataLength = contentLength;
            this.data = data;
        }

        public BinaryData getData() {
            return data;
        }

        public int getDataLength() {
            return dataLength;
        }
    }
}
