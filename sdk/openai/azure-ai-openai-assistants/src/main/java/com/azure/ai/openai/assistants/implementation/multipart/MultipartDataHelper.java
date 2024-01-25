// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.implementation.multipart;

import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class is used to marshal the request object into a multipart HTTP request.
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
     * This method marshals the passed request object.
     *
     * @param fileRequest the request object to be marshalled.
     * @return a structure containing the marshalled data and its length.
     */
    public MultipartDataSerializationResult serializeRequest(Path file, Map<String, Object> otherFields) {
        List<MultipartField> fields = formatOpenAIFileRequest(otherFields);
        return serializeRequestFields(file, fields);
    }

    /**
     * This helper method marshals the passed request fields.
     *
     * @param file is the byte[] representation of the file in the request object.
     * @param fields a list of the members other than the file in the request object.
     * @return a structure containing the marshalled data and its length.
     */
    private MultipartDataSerializationResult serializeRequestFields(Path file, List<MultipartField> fields) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            // Adding other fields to the request
            for (MultipartField field : fields) {
                byteArrayOutputStream.write(serializeField(field));
            }
            // Adding the file segment first
            fileSegment(file, byteArrayOutputStream);
            byteArrayOutputStream.write((CRLF + endMarker).getBytes(encoderCharset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] totalData = byteArrayOutputStream.toByteArray();
        return new MultipartDataSerializationResult(BinaryData.fromBytes(totalData), totalData.length);
    }

    private void fileSegment(Path file, ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        // Multipart preamble
        String fileName = file.getFileName().toString();
        byte[] fileData = BinaryData.fromFile(file).toBytes();
        String contentType = Files.probeContentType(file);

        String fileFieldPreamble = CRLF
             + partSeparator
            + CRLF + "Content-Disposition: form-data; name=file; filename="
            + fileName
            + CRLF + "Content-Type: " + contentType + CRLF + CRLF;
        // Writing the file into the request as a byte stream

        if ("text/plain".equals(contentType)){
            byteArrayOutputStream.write(fileFieldPreamble.getBytes(encoderCharset));
            byteArrayOutputStream.write(Files.readString(file).getBytes(encoderCharset));
        } else {
            byteArrayOutputStream.write(fileFieldPreamble.getBytes(encoderCharset));
            byteArrayOutputStream.write(fileData);
        }
    }

    /**
     * This method formats the request object into a list of fields.
     *
     * @param fileFields the request object to be marshalled.
     * @return a list of fields.
     */
    private List<MultipartField> formatOpenAIFileRequest(Map<String, Object> fileFields) {
        List<MultipartField> fields = new ArrayList<>();
        if (fileFields.get("purpose") != null) {
            fields.add(new MultipartField("purpose",
                fileFields.get("purpose").toString()));
        }
//        if (fileFields.get("filename") != null) {
//            fields.add(new MultipartField("filename",
//                fileFields.get("filename").toString()));
//        }
        return fields;
    }

    /**
     * This method formats a field for a multipart HTTP request and returns its byte[] representation.
     *
     * @param field the field of the request to be marshalled.
     * @return byte[] representation of a field for a multipart HTTP request.
     */
    private byte[] serializeField(MultipartField field) {
        String serialized = //CRLF
            partSeparator
            + CRLF + "Content-Disposition: form-data; name="
            + field.getWireName() + CRLF + CRLF
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
