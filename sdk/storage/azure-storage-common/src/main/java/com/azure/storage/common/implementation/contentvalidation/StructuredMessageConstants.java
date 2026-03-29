// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

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
     * The maximum single part upload size to use CRC64 header.
     */
    public static final int MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER = 4 * 1024 * 1024; // 4 MiB

    /**
     * The structured body type header value indicating version 1 with CRC64 properties.
     */
    public static final String STRUCTURED_BODY_TYPE_VALUE = "XSM/1.0; properties=crc64";

    public static final String CONTENT_VALIDATION_BEHAVIOR_KEY = "contentValidationBehavior";

    public static final String USE_CRC64_CHECKSUM_HEADER_CONTEXT = "crc64ChecksumHeaderContext";

    public static final String USE_STRUCTURED_MESSAGE_CONTEXT = "structuredMessageChecksumAlgorithm";
}
