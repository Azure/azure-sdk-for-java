// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobRequestConditions;

import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.ProgressReceiver;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.quickquery.implementation.util.NetworkInputStream;
import com.azure.storage.quickquery.models.BlobQuickQueryError;
import com.azure.storage.quickquery.models.BlobQuickQueryErrorReceiver;
import com.azure.storage.quickquery.models.BlobQuickQueryResponse;
import com.azure.storage.quickquery.models.BlobQuickQuerySerialization;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Exceptions;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.Flow;

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
     * Opens a blob input stream to query the blob.
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.quickquery.BlobQuickQueryClient.openInputStream#String}
     *
     * @param expression The query expression.
     */
    public final BlobQuickQueryInputStream openInputStream(String expression) {
        return openInputStream(expression, null, null, null, null, null);
    }

    /**
     * Opens a blob input stream to query the blob.
     *
     * @param expression The query expression.
     * @param input {@link BlobQuickQuerySerialization Serialization input}.
     * @param output {@link BlobQuickQuerySerialization Serialization output}.
     * @param requestConditions {@link BlobRequestConditions}
     *
     */
    public final BlobQuickQueryInputStream openInputStream(String expression, BlobQuickQuerySerialization input,
        BlobQuickQuerySerialization output, BlobRequestConditions requestConditions,
        BlobQuickQueryErrorReceiver nonFatalErrorReceiver, ProgressReceiver progressReceiver) {

        Flux<ByteBuffer> data = client.queryWithResponse(expression, input, output, requestConditions)
            .flatMapMany(ResponseBase::getValue)
            .onBackpressureBuffer()
            .subscribeOn(Schedulers.boundedElastic());

        NetworkInputStream networkInputStream = new NetworkInputStream(data, logger);

        return new BlobQuickQueryInputStream(networkInputStream, nonFatalErrorReceiver, progressReceiver, logger);
    }

    /**
     * Queries an entire blob into an output stream. NOTE: Returns raw avro.
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
     * Queries an entire blob into an output stream. NOTE: Returns raw avro.
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
