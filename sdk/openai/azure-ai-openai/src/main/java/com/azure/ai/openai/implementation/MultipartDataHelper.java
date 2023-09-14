// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.AudioTranscriptionOptions;
import com.azure.ai.openai.models.AudioTranslationOptions;
import com.azure.core.util.BinaryData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MultipartDataHelper {

    private final String boundary;

    private final String partSeparator;

    private final String endMarker;

    private final Charset encoderCharset = StandardCharsets.US_ASCII;

    private final String CRLF = "\r\n";

    public MultipartDataHelper() {
        this(() -> UUID.randomUUID().toString().substring(0, 16));
    }

    public MultipartDataHelper(MultipartBoundaryGenerator boundaryGenerator) {
        this.boundary = boundaryGenerator.generateBoundary();
        partSeparator = "--" + boundary;
        endMarker = partSeparator + "--";
    }

    public String getBoundary() {
        return boundary;
    }

    public <T> MultipartDataSerializationResult serializeRequest(T requestOptions, String fileName) {
        if (requestOptions instanceof AudioTranslationOptions) {
            AudioTranslationOptions audioTranslationOptions = (AudioTranslationOptions) requestOptions;
            byte[] file = audioTranslationOptions.getFile();
            List<MultipartField> fields = formatAudioTranslationOptions(audioTranslationOptions);
            return serializeRequestFields(file, fields, fileName);
        }
        else if (requestOptions instanceof AudioTranscriptionOptions) {
            AudioTranscriptionOptions audioTranscriptionOptions = (AudioTranscriptionOptions) requestOptions;
            byte[] file = audioTranscriptionOptions.getFile();
            List<MultipartField> fields = formatAudioTranscriptionOptions(audioTranscriptionOptions);
            return serializeRequestFields(file, fields, fileName);
        } else {
            throw new IllegalArgumentException("Only AudioTranslationOptions and AudioTranscriptionOptions currently supported");
        }
    }

    private MultipartDataSerializationResult serializeRequestFields (
        byte[] file, List<MultipartField> fields, String fileName) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Multipart preamble
        String fileFieldPreamble = partSeparator
            + CRLF + "Content-Disposition: form-data; name=\"file\"; filename=\""
            + fileName + "\""
            + CRLF + "Content-Type: application/octet-stream" + CRLF + CRLF;
        try {
            // Writing the file into the request as a byte stream
            byteArrayOutputStream.write(fileFieldPreamble.getBytes(encoderCharset));
            byteArrayOutputStream.write(file);

            // Adding other fields to the request
            for (MultipartField field : fields) {
                byteArrayOutputStream.write(serializeField(field));
            }
            byteArrayOutputStream.write((CRLF + endMarker).getBytes(encoderCharset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] totalData = byteArrayOutputStream.toByteArray();
        // Uncomment to verify as string. Seems to check out with structure observed in the curl traces
//        System.out.println(new String(totalData, StandardCharsets.US_ASCII));
        return new MultipartDataSerializationResult(BinaryData.fromBytes(totalData), totalData.length);
    }

    private List<MultipartField> formatAudioTranslationOptions(AudioTranslationOptions audioTranslationOptions) {
        List<MultipartField> fields = new ArrayList<>();
        if (audioTranslationOptions.getResponseFormat() != null) {
            fields.add(new MultipartField(
                "response_format",
                audioTranslationOptions.getResponseFormat().toString()));
        }
        if (audioTranslationOptions.getModel() != null) {
            fields.add(new MultipartField(
                    "model",
                    audioTranslationOptions.getModel()
            ));
        }
        if (audioTranslationOptions.getPrompt() != null) {
            fields.add( new MultipartField(
                "prompt",
                audioTranslationOptions.getPrompt()));
        }
        if (audioTranslationOptions.getTemperature() != null) {
            fields.add(new MultipartField(
                "temperature",
                String.valueOf(audioTranslationOptions.getTemperature())));
        }
        return fields;
    }

    private List<MultipartField> formatAudioTranscriptionOptions(AudioTranscriptionOptions audioTranscriptionOptions) {
        List<MultipartField> fields = new ArrayList<>();
        if (audioTranscriptionOptions.getResponseFormat() != null) {
            fields.add(new MultipartField(
                "response_format",
                audioTranscriptionOptions.getResponseFormat().toString()));
        }
        if (audioTranscriptionOptions.getModel() != null) {
            fields.add(new MultipartField(
                    "model",
                    audioTranscriptionOptions.getModel()
            ));
        }
        if (audioTranscriptionOptions.getPrompt() != null) {
            fields.add( new MultipartField(
                "prompt",
                audioTranscriptionOptions.getPrompt()));
        }
        if (audioTranscriptionOptions.getTemperature() != null) {
            fields.add(new MultipartField(
                "temperature",
                String.valueOf(audioTranscriptionOptions.getTemperature())));
        }
        if (audioTranscriptionOptions.getLanguage() != null) {
            fields.add(new MultipartField(
                "language",
                audioTranscriptionOptions.getLanguage()));
        }
        return fields;
    }

    private byte[] serializeField(MultipartField field) {
        String serialized = CRLF + partSeparator
            + CRLF + "Content-Disposition: form-data; name=\""
            + field.getWireName() + "\"" + CRLF + CRLF
            + field.getValue();

        return serialized.getBytes(encoderCharset);
    }
}
