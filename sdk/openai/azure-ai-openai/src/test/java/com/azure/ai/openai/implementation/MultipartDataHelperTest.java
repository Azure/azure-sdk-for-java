package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranslationOptions;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.azure.core.util.BinaryData;
import com.nimbusds.jose.util.Base64;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link MultipartDataHelper}
 */
public class MultipartDataHelperTest {

    @Test
    public void serializeAudioTranslationOptionsAllFields() {
        MultipartDataHelper helper = new MultipartDataHelper(new TestBoundaryGenerator());
        byte[] file = new byte[]{};
        String fileName = "file_name.wav";
        AudioTranslationOptions translationOptions = new AudioTranslationOptions(file);
        translationOptions.setModel("model_name")
                .setPrompt("prompt text")
                .setResponseFormat(AudioTranscriptionFormat.TEXT)
                .setTemperature(0.1);
        MultipartDataSerializationResult actual = helper.serializeRequest(translationOptions, fileName);

        String expected = multipartFileSegment(fileName, file)
                + fieldFormData("response_format", "text")
                + fieldFormData("model", "model_name")
                + fieldFormData("prompt", "prompt text")
                + fieldFormData("temperature", "0.1")
                + closingMarker();

        assertEquals(expected, actual.getData().toString());
        assertEquals(expected.getBytes(StandardCharsets.US_ASCII).length, actual.getDataLength());
    }

    @Test
    public void serializeAudioTranscriptionOptionsAllFields() {}

    @Test
    public void serializeAudioTranslationOptionsMinimumFields() {}

    @Test
    public void serializeAudioTranscriptionOptionsMinimumFields() {}

    @Test
    public void serializeUnsupportedType() {
        assertThrows(IllegalArgumentException.class, () -> {
            MultipartDataHelper helper = new MultipartDataHelper(new TestBoundaryGenerator());
            EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(new ArrayList<>());
            helper.serializeRequest(embeddingsOptions, "path/to/file");
        });
    }

    private static String fieldFormData(String fieldName, String fieldValue) {
        return "\r\n--test-boundary"
                + "\r\nContent-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n"
                + fieldValue;
    }

    private static String multipartFileSegment(String fileName, byte[] file) {
        return "--test-boundary\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n"
                + Base64.encode(file);
    }

    private static String closingMarker() {
        return "\r\n--test-boundary--";
    }
}
