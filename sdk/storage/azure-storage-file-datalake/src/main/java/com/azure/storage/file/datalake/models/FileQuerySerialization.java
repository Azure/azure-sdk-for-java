// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

// TODO (gapra): add parquet to inputs when parquet is released
/**
 * Defines the input and output serialization for a file quick query request.
 * either {@link FileQueryJsonSerialization}, {@link FileQueryDelimitedSerialization},
 * {@link FileQueryArrowSerialization}, or {@link FileQueryParquetSerialization}.
 * <p>
 * Note: {@link FileQueryParquetSerialization} can only be used as an input and
 *  {@link FileQueryArrowSerialization} can only be used as an output.
 * </p>
 */
public interface FileQuerySerialization {
}
