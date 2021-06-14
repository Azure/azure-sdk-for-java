// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;
// TODO (gapra): add parquet to inputs when parquet is released
/**
 * Defines the input and output serialization for a blob quick query request.
 * either {@link BlobQueryJsonSerialization}, {@link BlobQueryDelimitedSerialization}, or
 * {@link BlobQueryArrowSerialization}.
 * <p>
 * Note: {@link BlobQueryArrowSerialization} can only be used as an output.
 * </p>
 */
public interface BlobQuerySerialization {
}
