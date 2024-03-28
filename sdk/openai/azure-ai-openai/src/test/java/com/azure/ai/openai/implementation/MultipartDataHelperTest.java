// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranscriptionOptions;
import com.azure.ai.openai.models.AudioTranslationFormat;
import com.azure.ai.openai.models.AudioTranslationOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link MultipartFormDataHelper}
 */
public class MultipartDataHelperTest {

    private static final String TEST_BOUNDARY = "test-boundary";

    @Test
    public void serializeAudioTranslationOptionsAllFields() {
        MultipartFormDataHelper helper = new MultipartFormDataHelper(new RequestOptions(), TEST_BOUNDARY);

        byte[] file = new byte[] {73, 32, 115, 104, 111, 117, 108, 100, 32, 104, 97, 118, 101, 32, 116, 104, 111, 117,
            103, 104, 116, 32, 111, 102, 32, 97, 32, 103, 111, 111, 100, 32, 101, 97, 115, 116, 101, 114, 32, 101,
            103, 103};
        String fileName = "file_name.wav";
        AudioTranslationOptions translationOptions = new AudioTranslationOptions(file)
                .setModel("model_name")
                .setPrompt("prompt text")
                .setFilename(fileName)
                .setResponseFormat(AudioTranslationFormat.TEXT)
                .setTemperature(0.1);

        helper
                .serializeFileField("file", BinaryData.fromBytes(file), null, fileName)
                .serializeTextField("response_format", translationOptions.getResponseFormat().toString())
                .serializeTextField("model", translationOptions.getModel())
                .serializeTextField("prompt", translationOptions.getPrompt())
                .serializeTextField("temperature", String.valueOf(translationOptions.getTemperature()))
                .end();

        BinaryData actual = helper.getRequestBody();
        String expected = multipartFileSegment(fileName, file)
                + fieldFormData("response_format", "text")
                + fieldFormData("model", "model_name")
                + fieldFormData("prompt", "prompt text")
                + fieldFormData("temperature", "0.1")
                + closingMarker();
        assertEquals(expected, actual.toString());
        assertEquals(expected.getBytes(StandardCharsets.US_ASCII).length, actual.getLength());
    }

    @Test
    public void serializeAudioTranscriptionOptionsAllFields() {
        MultipartFormDataHelper helper = new MultipartFormDataHelper(new RequestOptions(), TEST_BOUNDARY);
        byte[] file = new byte[] {73, 32, 115, 104, 111, 117, 108, 100, 32, 104, 97, 118, 101, 32, 116, 104, 111, 117,
            103, 104, 116, 32, 111, 102, 32, 97, 32, 103, 111, 111, 100, 32, 101, 97, 115, 116, 101, 114, 32, 101,
            103, 103};
        String fileName = "file_name.wav";
        AudioTranscriptionOptions transcriptionOptions = new AudioTranscriptionOptions(file)
                .setModel("model_name")
                .setPrompt("prompt text")
                .setFilename(fileName)
                .setResponseFormat(AudioTranscriptionFormat.TEXT)
                .setLanguage("en")
                .setTemperature(0.1);

        helper
                .serializeFileField("file", BinaryData.fromBytes(file), null, fileName)
                .serializeTextField("response_format", transcriptionOptions.getResponseFormat().toString())
                .serializeTextField("model", transcriptionOptions.getModel())
                .serializeTextField("prompt", transcriptionOptions.getPrompt())
                .serializeTextField("temperature", String.valueOf(transcriptionOptions.getTemperature()))
                .serializeTextField("language", transcriptionOptions.getLanguage())
                .end();

        BinaryData actual = helper.getRequestBody();
        String expected = multipartFileSegment(fileName, file)
                + fieldFormData("response_format", "text")
                + fieldFormData("model", "model_name")
                + fieldFormData("prompt", "prompt text")
                + fieldFormData("temperature", "0.1")
                + fieldFormData("language", "en")
                + closingMarker();

        assertEquals(expected, actual.toString());
        assertEquals(expected.getBytes(StandardCharsets.US_ASCII).length, actual.getLength());
    }

    @Test
    public void emptyPartsInBetweenFirstAndLastBoundaries() {
        MultipartFormDataHelper helper = new MultipartFormDataHelper(new RequestOptions(), TEST_BOUNDARY);
        byte[] file = new byte[] {};
        String fileName = "file_name.wav";

        helper.serializeFileField("file", BinaryData.fromBytes(file), null, fileName)
                .end();
        BinaryData actual = helper.getRequestBody();

        String expected = multipartFileSegment(fileName, file)
                + closingMarker();

        assertEquals(expected, actual.toString());
        assertEquals(expected.getBytes(StandardCharsets.US_ASCII).length, actual.getLength());
    }


    private static String fieldFormData(String fieldName, String fieldValue) {
        return "\r\n--test-boundary"
                + "\r\nContent-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n"
                + fieldValue;
    }

    private static String multipartFileSegment(String fileName, byte[] fileBytes) {
        return "--test-boundary\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n"
                + new String(fileBytes, StandardCharsets.US_ASCII);
    }

    private static String closingMarker() {
        return "\r\n--test-boundary--";
    }
}
