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

    public static String getMultipartBoundary(String boundarySuffix) {
        return "----WebkitFormBoundary" + boundarySuffix;
    }

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
