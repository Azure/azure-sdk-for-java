// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.ContentType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Helper class to build a multipart HTTP request.
 */
public final class MultipartFormDataBuilder {
    /**
     * Line separator for the multipart HTTP request.
     */
    private static final String CR_LF = "\r\n";

    /**
     * Value to be used as part of the divider for the multipart requests.
     */
    private final String boundary;

    /**
     * The actual part separator in the request. This is obtained by prepending "--" to the "boundary".
     */
    private final String boundaryDelimiter;

    /**
     * The marker for the ending of a multipart request.
     * This is obtained by post-pending "--" to the "boundaryDelimiter".
     */
    private final String boundaryCloseDelimiter;

    /**
     * Charset used for encoding the multipart HTTP request.
     */
    private final Charset encoderCharset = StandardCharsets.UTF_8;

    private InputStream requestDataStream = new ByteArrayInputStream(new byte[0]);

    private long contentLength;

    /**
     * Default constructor used in the code. The boundary is a random value.
     *
     */
    public MultipartFormDataBuilder() {
        this(UUID.randomUUID().toString().substring(0, 16));
    }

    /**
     * Create an instance of MultipartFormData.
     *
     * @param boundary the boundary value
     */
    public MultipartFormDataBuilder(String boundary) {
        this.boundary = boundary;
        boundaryDelimiter = "--" + boundary;
        boundaryCloseDelimiter = this.boundaryDelimiter + "--";
    }

    /**
     * Formats a text/plain field for a multipart HTTP request.
     *
     * @param fieldName the field name
     * @param value the value of the text/plain field
     * @return the MultipartFormDataBuilder instance
     */
    public MultipartFormDataBuilder appendText(String fieldName, String value) {
        if (value != null) {
            String serialized = boundaryDelimiter + CR_LF + "Content-Disposition: form-data; name=\""
                + escapeName(fieldName) + "\"" + CR_LF + CR_LF + value + CR_LF;

            byte[] data = serialized.getBytes(encoderCharset);
            appendBytes(data);
        }
        return this;
    }

    /**
     * Formats a application/json field for a multipart HTTP request.
     *
     * @param fieldName the field name
     * @param jsonObject the object of the application/json field
     * @return the MultipartFormDataBuilder instance
     */
    public MultipartFormDataBuilder appendJson(String fieldName, Object jsonObject) {
        if (jsonObject != null) {
            String serialized = boundaryDelimiter + CR_LF + "Content-Disposition: form-data; name=\""
                + escapeName(fieldName) + "\"" + CR_LF + "Content-Type: application/json" + CR_LF + CR_LF
                + BinaryData.fromObject(jsonObject) + CR_LF;

            byte[] data = serialized.getBytes(encoderCharset);
            appendBytes(data);
        }
        return this;
    }

    /**
     * Formats a file field for a multipart HTTP request.
     *
     * @param fieldName the field name
     * @param file the BinaryData of the file
     * @param contentType the content-type of the file
     * @param filename Optional. The filename
     * @return the MultipartFormDataBuilder instance
     */
    public MultipartFormDataBuilder appendFile(String fieldName, BinaryData file, String contentType, String filename) {
        if (file != null) {
            if (CoreUtils.isNullOrEmpty(contentType)) {
                contentType = ContentType.APPLICATION_OCTET_STREAM;
            }
            writeFileField(fieldName, file, contentType, filename);
        }
        return this;
    }

    /**
     * Formats a file field (potentially multiple files) for a multipart HTTP request.
     *
     * @param fieldName the field name
     * @param files the List of BinaryData of the files
     * @param contentTypes the List of content-type of the files
     * @param filenames the List of filenames
     * @return the MultipartFormDataBuilder instance
     */
    public MultipartFormDataBuilder appendFiles(String fieldName, List<BinaryData> files, List<String> contentTypes,
        List<String> filenames) {
        if (!CoreUtils.isNullOrEmpty(files)) {
            for (int i = 0; i < files.size(); ++i) {
                BinaryData file = files.get(i);
                String contentType = contentTypes.get(i);
                String filename = filenames.get(i);
                // Each part shares the same field name
                appendFile(fieldName, file, contentType, filename);
            }
        }
        return this;
    }

    /**
     * Ends the serialization of the multipart HTTP request.
     *
     * @return the MultipartFormDataBuilder instance
     */
    public MultipartFormData build() {
        byte[] data = boundaryCloseDelimiter.getBytes(encoderCharset);
        appendBytes(data);

        return new MultipartFormData("multipart/form-data; boundary=" + this.boundary, requestDataStream,
            contentLength);
    }

    private void writeFileField(String fieldName, BinaryData file, String contentType, String filename) {
        String contentDispositionFilename = "";
        if (!CoreUtils.isNullOrEmpty(filename)) {
            contentDispositionFilename = "; filename=\"" + escapeName(filename) + "\"";
        }

        // Multipart preamble
        String fileFieldPreamble
            = boundaryDelimiter + CR_LF + "Content-Disposition: form-data; name=\"" + escapeName(fieldName) + "\""
                + contentDispositionFilename + CR_LF + "Content-Type: " + contentType + CR_LF + CR_LF;
        byte[] data = fileFieldPreamble.getBytes(encoderCharset);
        appendBytes(data);

        // Writing the file into the request as a byte stream
        requestDataStream = new SequenceInputStream(requestDataStream, file.toStream());
        contentLength += file.getLength();

        // CRLF
        data = CR_LF.getBytes(encoderCharset);
        appendBytes(data);
    }

    private void appendBytes(byte[] bytes) {
        requestDataStream = new SequenceInputStream(requestDataStream, new ByteArrayInputStream(bytes));
        contentLength += bytes.length;
    }

    private static String escapeName(String name) {
        return name.replace("\n", "%0A").replace("\r", "%0D").replace("\"", "%22");
    }
}
