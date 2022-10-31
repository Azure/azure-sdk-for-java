// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.developer.loadtesting.implementation.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.azure.core.util.BinaryData;

/**
 * This class provides helper methods for constructing multipart/form-data body
 */
public final class MultipartHelper {
    private static final char CR = (char) 0x0D;
    private static final char LF = (char) 0x0A;
    private static final String NEWLINE = "" + CR + LF;

    /**
     * Create Multipart boundary using suffix.
     *
     * @param boundarySuffix Suffix to be added to multipart boundary, must be a valid URL character ^[a-z0-9_-]*$.
     * @return the Multipart boundary {@link String}.
     */
    public static String getMultipartBoundary(String boundarySuffix) {
        return "----WebkitFormBoundary" + boundarySuffix;
    }

    /**
     * Delete an App Component.
     *
     * @param fileName Name of the file, must be a valid URL character ^[a-z0-9_-]*$.
     * @param file The file as BinaryData to be used as multipart body of request.
     * @param boundary The Multipart boundary used in the request body.
     * @throws IOException thrown if error occurs while writing to byte stream.
     * @return the Multipart request body as {@link BinaryData} encoded as UTF-8.
     */
    public static BinaryData createMultipartBodyFromFile(String fileName, BinaryData file, String boundary) throws IOException {
        ByteArrayOutputStream bodyByteStream = new ByteArrayOutputStream();
        bodyByteStream.write(("--" + boundary + NEWLINE).getBytes("UTF-8"));
        bodyByteStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + NEWLINE).getBytes("UTF-8"));
        bodyByteStream.write(("Content-Type: application/octet-stream" + NEWLINE + NEWLINE).getBytes("UTF-8"));
        bodyByteStream.write(file.toBytes());
        bodyByteStream.write((NEWLINE + NEWLINE + "--" + boundary + "--").getBytes("UTF-8"));

        return BinaryData.fromBytes(bodyByteStream.toByteArray());
    }
}
