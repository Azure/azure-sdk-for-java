// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Extended options that may be passed when uploading a file in parallel.
 */
@Fluent
public class FileParallelUploadOptions {
    private final Flux<ByteBuffer> dataFlux;
    private final InputStream dataStream;
    private final Long length;
    private ParallelTransferOptions parallelTransferOptions;
    private PathHttpHeaders headers;
    private Map<String, String> metadata;
    private String permissions;
    private String umask;
    private DataLakeRequestConditions requestConditions;

    /**
     * Constructs a new {@code FileParallelUploadOptions}.
     *
     * @param dataFlux The data to write to the file. Unlike other upload methods, this method does not require that
     * the {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not
     * expected to produce the same values across subscriptions.
     */
    public FileParallelUploadOptions(Flux<ByteBuffer> dataFlux) {
        StorageImplUtils.assertNotNull("dataFlux", dataFlux);
        this.dataFlux = dataFlux;
        this.dataStream = null;
        this.length = null;
    }

    /**
     * Constructs a new {@code FileParallelUploadOptions}.
     *
     * Use {@link #FileParallelUploadOptions(InputStream)} instead to supply an InputStream without knowing the exact
     * length beforehand.
     *
     * @param dataStream The data to write to the blob. The data must be markable. This is in order to support retries.
     * If the data is not markable, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add
     * mark support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @deprecated length is no longer necessary; use {@link #FileParallelUploadOptions(InputStream)} instead.
     */
    @Deprecated
    public FileParallelUploadOptions(InputStream dataStream, long length) {
        this(dataStream, Long.valueOf(length));
    }

    /**
     * Constructs a new {@code FileParallelUploadOptions}.
     *
     * @param dataStream The data to write to the blob. The data must be markable. This is in order to support retries.
     * If the data is not markable, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add
     * mark support.
     */
    public FileParallelUploadOptions(InputStream dataStream) {
        this(dataStream, null);
    }

    private FileParallelUploadOptions(InputStream dataStream, Long length) {
        StorageImplUtils.assertNotNull("dataStream", dataStream);
        if (length != null) {
            StorageImplUtils.assertInBounds("length", length, 0, Long.MAX_VALUE);
        }
        this.dataStream = dataStream;
        this.length = length;
        this.dataFlux = null;
    }

    /**
     * Gets the data source.
     *
     * @return The data to write to the file.
     */
    public Flux<ByteBuffer> getDataFlux() {
        return this.dataFlux;
    }

    /**
     * Gets the data source.
     *
     * @return The data to write to the file.
     */
    public InputStream getDataStream() {
        return this.dataStream;
    }

    /**
     * Gets the length of the data.
     *
     * @return The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @deprecated use {@link #getOptionalLength()} to have safe access to a length that will not always exist.
     */
    @Deprecated
    public long getLength() {
        return length;
    }

    /**
     * Gets the length of the data.
     *
     * @return The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     */
    public Long getOptionalLength() {
        return length;
    }

    /**
     * Gets the {@link ParallelTransferOptions}.
     *
     * @return {@link ParallelTransferOptions}
     */
    public ParallelTransferOptions getParallelTransferOptions() {
        return parallelTransferOptions;
    }

    /**
     * Sets the {@link ParallelTransferOptions}.
     *
     * @param parallelTransferOptions {@link ParallelTransferOptions}
     * @return The updated options.
     */
    public FileParallelUploadOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions) {
        this.parallelTransferOptions = parallelTransferOptions;
        return this;
    }

    /**
     * Gets the {@link PathHttpHeaders}.
     *
     * @return {@link PathHttpHeaders}
     */
    public PathHttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Sets the {@link PathHttpHeaders}.
     *
     * @param headers {@link PathHttpHeaders}
     * @return The updated options
     */
    public FileParallelUploadOptions setHeaders(PathHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Gets the metadata.
     *
     * @return The metadata to associate with the file.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata.
     *
     * @param metadata The metadata to associate with the blob.
     * @return The updated options.
     */
    public FileParallelUploadOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the permissions.
     *
     * @return the POSIX access permissions for the resource owner, the resource owning group, and others.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Sets the permissions.
     *
     * @param permissions the POSIX access permissions for the resource owner, the resource owning group, and others.
     * @return The updated options
     */
    public FileParallelUploadOptions setPermissions(String permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * Gets the umask.
     *
     * @return the umask.
     */
    public String getUmask() {
        return umask;
    }

    /**
     * Sets the umask.
     *
     * @param umask Restricts permissions of the resource to be created.
     * @return The updated options
     */
    public FileParallelUploadOptions setUmask(String umask) {
        this.umask = umask;
        return this;
    }

    /**
     * Gets the {@link DataLakeRequestConditions}.
     *
     * @return {@link DataLakeRequestConditions}
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link DataLakeRequestConditions}.
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return The updated options.
     */
    public FileParallelUploadOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

}
