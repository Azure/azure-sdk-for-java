package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.AudioTranslationOptions;
import com.azure.core.util.BinaryData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MultipartDataHelper {
    private final String boundaryId = UUID.randomUUID().toString().substring(0, 16);

    private final String boundary = "--AZ-OAI-JAVA--" + boundaryId;
    private final String endBoundary = boundary + "--";

    private final String CRLF = "\r\n";
    private final List<MultipartField> fields = new ArrayList<>();

    public String getBoundary() {
        return boundary;
    }

    public SerializationResult serializeAudioTranscriptionOption (
        AudioTranslationOptions audioTranscriptionOptions, String fileName) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // File
        String fileFieldPreamble = boundary
            + CRLF + "Content-Disposition: form-data; name=\"file\"; filename=\""
            + fileName + "\""
            + CRLF + "Content-Type: application/octet-stream" + CRLF + CRLF;
        try {
            byteArrayOutputStream.write(fileFieldPreamble.getBytes(StandardCharsets.US_ASCII));
            byteArrayOutputStream.write(audioTranscriptionOptions.getFile());
            for (MultipartField field : fields) {
                byteArrayOutputStream.write(serializeField(field));
            }
            byteArrayOutputStream.write((CRLF + endBoundary).getBytes(StandardCharsets.US_ASCII));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] totalData = byteArrayOutputStream.toByteArray();
        // Uncomment to verify as string. Seems to check out with structure observed in the curl traces
        System.out.println(new String(totalData, StandardCharsets.US_ASCII));
        return new SerializationResult(BinaryData.fromBytes(totalData), totalData.length);
    }

    public void addFields(Consumer<List<MultipartField>> fieldAdder) {
        fieldAdder.accept(fields);
    }

    private byte[] serializeField(MultipartField field) {
        String toSerizalise = CRLF + boundary
            + CRLF + "Content-Disposition: form-data; name=\""
            + field.getWireName() + "\"" + CRLF + CRLF
            + field.getValue();

        return toSerizalise.getBytes(StandardCharsets.US_ASCII);
    }

    public class SerializationResult {
        private final long dataLength;
        private final BinaryData data;

        public SerializationResult(BinaryData data, long contentLength) {
            this.dataLength = contentLength;
            this.data = data;
        }

        public BinaryData getData() {
            return data;
        }

        public long getDataLength() {
            return dataLength;
        }
    }
}
