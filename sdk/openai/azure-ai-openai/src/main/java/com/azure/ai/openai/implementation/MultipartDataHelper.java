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

/**
 * Helper class for marshaling {@link AudioTranscriptionOptions} and {@link AudioTranslationOptions} objects to be used
 * in multipart HTTP requests according to RFC7578.
 */
public class MultipartDataHelper {

    /**
     * Value to be used as part of the divider for the multipart requests.
     */
    private final String boundary;

    /**
     * The actual part separator in the request. This is obtained by prepending "--" to the "boundary".
     */
    private final String partSeparator;

    /**
     * The marker for the ending of a multipart request. This is obtained by post-pending "--" to the "partSeparator".
     */
    private final String endMarker;

    /**
     * Charset used for encoding the multipart HTTP request.
     */
    private final Charset encoderCharset = StandardCharsets.UTF_8;

    /**
     * Line separator for the multipart HTTP request.
     */
    private static final String CRLF = "\r\n";

    /**
     * Default constructor used in the code. The boundary is a random value.
     */
    public MultipartDataHelper() {
        // We can't use randomly generated UUIDs for now. Generating a test session record won't match
        // the newly generated UUID for the test run instance
        // this(UUID.randomUUID().toString().substring(0, 16));
        this("29580623-3d02-4a");
    }

    /**
     * Constructor accepting a boundary generator. Used for testing.
     * @param boundary The value to be used as "boundary".
     */
    public MultipartDataHelper(String boundary) {
        this.boundary = boundary;
        partSeparator = "--" + boundary;
        endMarker = partSeparator + "--";
    }

    /**
     *
     * @return the "boundary" value.
     */
    public String getBoundary() {
        return boundary;
    }

    /**
     * This methods marshals the passed request into ready to be sent
     * @param requestOptions object to be marshalled for the multipart HTTP request
     * @param fileName the name of the file that is being sent as a part of this request
     * @return the marshalled data and its length
     * @param <T> {@link AudioTranscriptionOptions} and {@link AudioTranslationOptions} are the only types supported.
     *           This represents the type information of the request object.
     */
    public <T> MultipartDataSerializationResult serializeRequest(T requestOptions, String fileName) {
        if (requestOptions instanceof AudioTranslationOptions) {
            AudioTranslationOptions audioTranslationOptions = (AudioTranslationOptions) requestOptions;
            byte[] file = audioTranslationOptions.getFile();
            List<MultipartField> fields = formatAudioTranslationOptions(audioTranslationOptions);
            return serializeRequestFields(file, fields, fileName);
        } else if (requestOptions instanceof AudioTranscriptionOptions) {
            AudioTranscriptionOptions audioTranscriptionOptions = (AudioTranscriptionOptions) requestOptions;
            byte[] file = audioTranscriptionOptions.getFile();
            List<MultipartField> fields = formatAudioTranscriptionOptions(audioTranscriptionOptions);
            return serializeRequestFields(file, fields, fileName);
        } else {
            throw new IllegalArgumentException("Only AudioTranslationOptions and AudioTranscriptionOptions currently supported");
        }
    }

    /**
     *
     * @param file is the byte[] representation of the file in the request object.
     * @param fields a list of the members other than the file in the request object.
     * @param fileName the name of the file passed in the "file" field of the request object.
     * @return a structure containing the marshalled data and its length.
     */
    private MultipartDataSerializationResult serializeRequestFields(byte[] file, List<MultipartField> fields, String fileName) {
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
        return new MultipartDataSerializationResult(BinaryData.fromBytes(totalData), totalData.length);
    }

    /**
     * Adds member fields apart from the file to the multipart HTTP request
     * @param audioTranslationOptions request object
     * @return a list of the fields in the request (except for "file")
     */
    private List<MultipartField> formatAudioTranslationOptions(AudioTranslationOptions audioTranslationOptions) {
        List<MultipartField> fields = new ArrayList<>();
        if (audioTranslationOptions.getResponseFormat() != null) {
            fields.add(new MultipartField(
                "response_format",
                audioTranslationOptions.getResponseFormat().toString()));
        }
        if (audioTranslationOptions.getModel() != null) {
            fields.add(new MultipartField("model",
                    audioTranslationOptions.getModel()
            ));
        }
        if (audioTranslationOptions.getPrompt() != null) {
            fields.add(new MultipartField("prompt",
                audioTranslationOptions.getPrompt()));
        }
        if (audioTranslationOptions.getTemperature() != null) {
            fields.add(new MultipartField("temperature",
                String.valueOf(audioTranslationOptions.getTemperature())));
        }
        return fields;
    }

    /**
     * Adds member fields apart from the file to the multipart HTTP request
     * @param audioTranscriptionOptions request object
     * @return a list of the fields in the request (except for "file")
     */
    private List<MultipartField> formatAudioTranscriptionOptions(AudioTranscriptionOptions audioTranscriptionOptions) {
        List<MultipartField> fields = new ArrayList<>();
        if (audioTranscriptionOptions.getResponseFormat() != null) {
            fields.add(new MultipartField("response_format",
                audioTranscriptionOptions.getResponseFormat().toString()));
        }
        if (audioTranscriptionOptions.getModel() != null) {
            fields.add(new MultipartField("model",
                    audioTranscriptionOptions.getModel()
            ));
        }
        if (audioTranscriptionOptions.getPrompt() != null) {
            fields.add(new MultipartField("prompt",
                audioTranscriptionOptions.getPrompt()));
        }
        if (audioTranscriptionOptions.getTemperature() != null) {
            fields.add(new MultipartField("temperature",
                String.valueOf(audioTranscriptionOptions.getTemperature())));
        }
        if (audioTranscriptionOptions.getLanguage() != null) {
            fields.add(new MultipartField("language",
                audioTranscriptionOptions.getLanguage()));
        }
        return fields;
    }

    /**
     * This method formats a field for a multipart HTTP request and returns its byte[] representation
     * @param field the field of the request to be marshalled
     * @return byte[] representation of a field for a multipart HTTP request
     */
    private byte[] serializeField(MultipartField field) {
        String serialized = CRLF + partSeparator
            + CRLF + "Content-Disposition: form-data; name=\""
            + field.getWireName() + "\"" + CRLF + CRLF
            + field.getValue();

        return serialized.getBytes(encoderCharset);
    }
}
