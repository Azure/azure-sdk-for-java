// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranscriptionOptions;
import com.azure.ai.openai.models.AudioTranslationFormat;
import com.azure.ai.openai.models.AudioTranslationOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.MultipartFormDataBuilder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link MultipartFormDataBuilder}
 */
public class MultipartDataHelperTest {

    private static final String TEST_BOUNDARY = "test-boundary";

    @Test
    public void serializeAudioTranslationOptionsAllFields() {
        MultipartFormDataBuilder helper = new MultipartFormDataBuilder(TEST_BOUNDARY);

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
                .appendFile("file", BinaryData.fromBytes(file), null, fileName)
                .appendText("response_format", translationOptions.getResponseFormat().toString())
                .appendText("model", translationOptions.getModel())
                .appendText("prompt", translationOptions.getPrompt())
                .appendText("temperature", String.valueOf(translationOptions.getTemperature()))
                .build();

        BinaryData actual = helper.build().getRequestBody();
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
        MultipartFormDataBuilder helper = new MultipartFormDataBuilder(TEST_BOUNDARY);
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
                .appendFile("file", BinaryData.fromBytes(file), null, fileName)
                .appendText("response_format", transcriptionOptions.getResponseFormat().toString())
                .appendText("model", transcriptionOptions.getModel())
                .appendText("prompt", transcriptionOptions.getPrompt())
                .appendText("temperature", String.valueOf(transcriptionOptions.getTemperature()))
                .appendText("language", transcriptionOptions.getLanguage())
                .build();

        BinaryData actual = helper.build().getRequestBody();
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
        MultipartFormDataBuilder helper = new MultipartFormDataBuilder(TEST_BOUNDARY);
        byte[] file = new byte[] {};
        String fileName = "file_name.wav";

        helper.appendFile("file", BinaryData.fromBytes(file), null, fileName)
                .build();
        BinaryData actual = helper.build().getRequestBody();

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
