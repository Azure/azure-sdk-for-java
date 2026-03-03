// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

/**
 * Algorithm for content validation on upload and download operations. When enabled, the SDK computes checksums and
 * validates content integrity using the selected algorithm. Content validation is off by default.
 * <p>
 * Supported in Azure Storage Blob, Data Lake, and File Share packages for methods that use APIs supporting
 * transactional CRC64, transactional MD5, or structured message format.
 */
public enum StorageChecksumAlgorithm {

    /**
     * No content validation. This is the default; no checksum is computed or validated.
     */
    NONE,

    /**
     * Allow the SDK to choose the validation algorithm. Currently resolves to CRC64 where supported. Different
     * library versions may make different choices.
     */
    AUTO,

    /**
     * Standard MD5 hash. The SDK can compute and validate MD5 for the transfer where the API supports it
     * (e.g. Content-MD5 header).
     */
    MD5,

    /**
     * Azure Storage custom 64-bit CRC. The SDK computes and validates CRC64 checksums for the transfer.
     */
    CRC64
}
