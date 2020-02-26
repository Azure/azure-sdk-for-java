// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobRequestConditions;

import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.quickquery.models.BlobQuickQueryResponse;
import com.azure.storage.quickquery.models.BlobQuickQuerySerialization;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.Duration;

import static com.azure.storage.common.implementation.StorageImplUtils.blockWithOptionalTimeout;

/**
 * This class provides a client that contains all operations that apply to Azure Storage Blob quick query.
 *
 * <p>This client offers the ability to query contents of a structured blob. </p>
 *
 * @see BlobQuickQueryClientBuilder
 */
@ServiceClient(builder = BlobQuickQueryClientBuilder.class)
public class BlobQuickQueryClient {
    private final ClientLogger logger = new ClientLogger(BlobQuickQueryClient.class);

    private BlobQuickQueryAsyncClient client;

    /**
     * Package-private constructor for use by {@link BlobQuickQueryClientBuilder}.
     * @param client the async blob quick query client
     */
    BlobQuickQueryClient(BlobQuickQueryAsyncClient client) {
        this.client = client;
    }

    /**
     * Queries an entire blob into an output stream.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.quickquery.BlobQuickQueryClient.query#OutputStream-String}
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param expression The query expression.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null.
     */
    public void query(OutputStream stream, String expression) {
        queryWithResponse(stream, expression, null, null, null, null, Context.NONE);
    }

    /**
     * Queries an entire blob into an output stream.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.quickquery.BlobQuickQueryClient.queryWithResponse#OutputStream-String-BlobQuickQuerySerialization-BlobQuickQuerySerialization-BlobRequestConditions-Duration-Context}
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param expression The query expression.
     * @param input {@link BlobQuickQuerySerialization Serialization input}.
     * @param output {@link BlobQuickQuerySerialization Serialization output}.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null.
     */
    public BlobQuickQueryResponse queryWithResponse(OutputStream stream, String expression,
        BlobQuickQuerySerialization input, BlobQuickQuerySerialization output, BlobRequestConditions requestConditions,
        Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("stream", stream);
        Mono<BlobQuickQueryResponse> download = client
            .queryWithResponse(expression, input, output, requestConditions, context)
            .flatMap(response -> response.getValue().reduce(stream, (outputStream, buffer) -> {
                try {
                    outputStream.write(FluxUtil.byteBufferToArray(buffer));
                    return outputStream;
                } catch (IOException ex) {
                    throw logger.logExceptionAsError(Exceptions.propagate(new UncheckedIOException(ex)));
                }
            }).thenReturn(new BlobQuickQueryResponse(response)));

        return blockWithOptionalTimeout(download, timeout);
    }

}
