// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines the input and output serialization for a blob quick query request.
 * either {@link BlobQueryJsonSerialization}, {@link BlobQueryDelimitedSerialization},
 * {@link BlobQueryArrowSerialization}, or {@link BlobQueryParquetSerialization}.
 * <p>
 * Note: {@link BlobQueryParquetSerialization} can only be used as an input and
 * {@link BlobQueryArrowSerialization} can only be used as an output.
 * </p>
 */
public interface BlobQuerySerialization {
}
