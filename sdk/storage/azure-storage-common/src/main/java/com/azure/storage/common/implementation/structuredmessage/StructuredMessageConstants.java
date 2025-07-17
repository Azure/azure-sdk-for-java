// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

/**
 * Constants used in the structured message encoding and decoding process.
 */
public final class StructuredMessageConstants {
    /**
     * The default version of the structured message.
     */
    public static final int DEFAULT_MESSAGE_VERSION = 1;

    /**
     * The length of the header for version 1 of the structured message.
     */
    public static final int V1_HEADER_LENGTH = 13;

    /**
     * The length of the segment header for version 1 of the structured message.
     */
    public static final int V1_SEGMENT_HEADER_LENGTH = 10;

    /**
     * The length of the CRC64 checksum.
     */
    public static final int CRC64_LENGTH = 8;

    /**
     * The default length of segments for version 1.
     */
    public static final int V1_DEFAULT_SEGMENT_CONTENT_LENGTH = 4 * 1024 * 1024; // 4 MiB

    /**
     * The maximum amount of data to encode at once.
     */
    public static final int STATIC_MAXIMUM_ENCODED_DATA_LENGTH = 4 * 1024 * 1024; // 4 MiB

    /**
     * The header name for the CRC64 checksum.
     */
    public static final String STRUCTURED_BODY_TYPE_VALUE = "XSM/1.0; properties=crc64";
}
