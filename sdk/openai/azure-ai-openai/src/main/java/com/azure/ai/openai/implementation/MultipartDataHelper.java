// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.AudioTranscriptionOptions;
import com.azure.ai.openai.models.AudioTranscriptionTimestampGranularity;
import com.azure.ai.openai.models.AudioTranslationOptions;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Helper class for marshaling {@link AudioTranscriptionOptions} and {@link AudioTranslationOptions} objects to be used
 * in multipart HTTP requests according to RFC7578.
 */
public class MultipartDataHelper {
    private static final ClientLogger LOGGER = new ClientLogger(MultipartDataHelper.class);

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
        this(UUID.randomUUID().toString().substring(0, 16));
    }

    /**
     * Constructor accepting a boundary generator. Used for testing.
     *
     * @param boundary The value to be used as "boundary".
     */
    public MultipartDataHelper(String boundary) {
        this.boundary = boundary;
        partSeparator = "--" + boundary;
        endMarker = partSeparator + "--";
    }

    /**
     * Gets the "boundary" value.
     *
     * @return the "boundary" value.
     */
    public String getBoundary() {
        return boundary;
    }

    /**
     * This method marshals the passed request into ready to be sent.
     *
     * @param requestOptions Object to be marshalled for the multipart HTTP request.
     * @param fileName The name of the file that is being sent as a part of this request.
     * @param <T> {@link AudioTranscriptionOptions} and {@link AudioTranslationOptions} are the only types supported.
     *           This represents the type information of the request object.
     * @return the marshalled data and its length.
     */
    public <T> MultipartDataSerializationResult serializeRequest(T requestOptions) {
        if (requestOptions instanceof AudioTranslationOptions) {
            AudioTranslationOptions audioTranslationOptions = (AudioTranslationOptions) requestOptions;
            byte[] file = audioTranslationOptions.getFile();
            List<MultipartField> fields = formatAudioTranslationOptions(audioTranslationOptions);
            return serializeRequestFields(file, fields, audioTranslationOptions.getFilename());
        } else if (requestOptions instanceof AudioTranscriptionOptions) {
            AudioTranscriptionOptions audioTranscriptionOptions = (AudioTranscriptionOptions) requestOptions;
            byte[] file = audioTranscriptionOptions.getFile();
            List<MultipartField> fields = formatAudioTranscriptionOptions(audioTranscriptionOptions);
            return serializeRequestFields(file, fields, audioTranscriptionOptions.getFilename());
        } else {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException(
                "Only AudioTranslationOptions and AudioTranscriptionOptions currently supported"));
        }
    }

    /**
     * This helper method marshals the passed request fields.
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
     * Adds member fields apart from the file to the multipart HTTP request.
     *
     * @param audioTranslationOptions The configuration information for an audio translation request.
     * @return a list of the fields in the request (except for "file").
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
     * Adds member fields apart from the file to the multipart HTTP request.
     *
     * @param audioTranscriptionOptions The configuration information for an audio transcription request.
     * @return a list of the fields in the request (except for "file").
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


        List<AudioTranscriptionTimestampGranularity> timestampGranularities =
                audioTranscriptionOptions.getTimestampGranularities();
        if (timestampGranularities != null) {
            for (AudioTranscriptionTimestampGranularity timestampGranularity : timestampGranularities) {
                fields.add(new MultipartField("timestamp_granularities[]", timestampGranularity.toString()));
            }
        }

        return fields;
    }

    /**
     * This method formats a field for a multipart HTTP request and returns its byte[] representation.
     *
     * @param field the field of the request to be marshalled.
     * @return byte[] representation of a field for a multipart HTTP request.
     */
    private byte[] serializeField(MultipartField field) {
        String serialized = CRLF + partSeparator
            + CRLF + "Content-Disposition: form-data; name=\""
            + field.getWireName() + "\"" + CRLF + CRLF
            + field.getValue();

        return serialized.getBytes(encoderCharset);
    }

    /**
     * Get the request options for multipart form data.
     *
     * @param requestOptions The request options.
     * @param result The multipart data serialization result.
     * @param multipartBoundary The multipart boundary.
     * @return The request options.
     */
    public RequestOptions getRequestOptionsForMultipartFormData(RequestOptions requestOptions,
        MultipartDataSerializationResult result, String multipartBoundary) {
        if (requestOptions == null) {
            requestOptions =
                new RequestOptions()
                    .setHeader(
                        HttpHeaderName.CONTENT_TYPE,
                        "multipart/form-data;" + " boundary=" + multipartBoundary)
                    .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(result.getDataLength()));
        }
        return requestOptions;
    }
}
