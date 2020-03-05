// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.models.DelimitedTextConfiguration;
import com.azure.storage.blob.implementation.models.JsonTextConfiguration;
import com.azure.storage.blob.implementation.models.QueryRequest;
import com.azure.storage.blob.implementation.models.QuickQueryFormat;
import com.azure.storage.blob.implementation.models.QuickQueryFormatType;
import com.azure.storage.blob.implementation.models.QuickQuerySerialization;
import com.azure.storage.blob.implementation.models.QuickQueryType;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.ProgressReporter;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.UploadBufferPool;
import com.azure.storage.common.implementation.UploadUtils;
import com.azure.storage.quickquery.models.BlobQuickQueryAsyncResponse;
import com.azure.storage.quickquery.models.BlobQuickQueryDelimitedSerialization;
import com.azure.storage.quickquery.models.BlobQuickQueryJsonSerialization;
import com.azure.storage.quickquery.models.BlobQuickQuerySerialization;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.azure.core.util.FluxUtil.byteBufferToArray;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client that contains all operations that apply to Azure Storage Blob quick query.
 *
 * <p>This client offers the ability to query contents of a structured blob. </p>
 *
 * @see BlobQuickQueryClientBuilder
 */
@ServiceClient(builder = BlobQuickQueryClientBuilder.class, isAsync = true)
class BlobQuickQueryAsyncClient {

    private final ClientLogger logger = new ClientLogger(BlobQuickQueryAsyncClient.class);

    private final AzureBlobStorageImpl client;
    private final CpkInfo customerProvidedKey;

    /**
     * Package-private constructor for use by {@link BlobQuickQueryClientBuilder}
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param customerProvidedKey Customer provided key used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     */
    BlobQuickQueryAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion,
        CpkInfo customerProvidedKey) {
        this.client = new AzureBlobStorageBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion.getVersion())
            .build();
        this.customerProvidedKey = customerProvidedKey;
    }

    /**
     * Queries the entire blob. NOTE: Returns raw avro.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.quickquery.BlobQuickQueryAsyncClient.query#String}
     *
     * @param expression The query expression.
     * @return A reactive response containing the queried data.
     */
    Flux<ByteBuffer> query(String expression) {
        return queryWithResponse(expression, null, null, null)
            .flatMapMany(BlobQuickQueryAsyncResponse::getValue);
    }

    /**
     * Queries the entire blob. NOTE: Returns raw avro.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.quickquery.BlobQuickQueryAsyncClient.queryWithResponse#String-BlobQuickQuerySerialization-BlobQuickQuerySerialization-BlobRequestConditions}
     *
     * @param expression The query expression.
     * @param input {@link BlobQuickQuerySerialization Serialization input}
     * @param output {@link BlobQuickQuerySerialization Serialization output}
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the queried data.
     */
    Mono<BlobQuickQueryAsyncResponse> queryWithResponse(String expression, BlobQuickQuerySerialization input,
        BlobQuickQuerySerialization output, BlobRequestConditions requestConditions) {
        try {
            return withContext(context ->
                queryWithResponse(expression, input, output, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<BlobQuickQueryAsyncResponse> queryWithResponse(String expression, BlobQuickQuerySerialization input,
        BlobQuickQuerySerialization output, BlobRequestConditions requestConditions, Context context) {

        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        QuickQuerySerialization in = transformSerialization(input, logger);
        QuickQuerySerialization out = transformSerialization(output, logger);

        QueryRequest qr = new QueryRequest()
            .setExpression(expression)
            .setInputSerialization(in)
            .setOutputSerialization(out);

        return client.blobs().quickQueryWithRestResponseAsync(null, null, qr, null, null,
            requestConditions.getLeaseId(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), null, customerProvidedKey, context)
            .map(response -> new BlobQuickQueryAsyncResponse(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), response.getValue(), response.getDeserializedHeaders()));
    }

    /**
     * Transforms a generic BlobQuickQuerySerialization into a QuickQuerySerialization.
     * @param userSerialization {@link BlobQuickQuerySerialization}
     * @param logger {@link ClientLogger}
     * @return {@link QuickQuerySerialization}
     */
    private static QuickQuerySerialization transformSerialization(BlobQuickQuerySerialization userSerialization,
        ClientLogger logger) {
        if (userSerialization == null) {
            return null;
        }

        QuickQueryFormat generatedFormat = new QuickQueryFormat();
        if (userSerialization instanceof BlobQuickQueryDelimitedSerialization) {

            generatedFormat.setType(QuickQueryFormatType.DELIMITED);
            generatedFormat.setDelimitedTextConfiguration(transformDelimited(
                (BlobQuickQueryDelimitedSerialization) userSerialization));

        } else if (userSerialization instanceof BlobQuickQueryJsonSerialization) {

            generatedFormat.setType(QuickQueryFormatType.JSON);
            generatedFormat.setJsonTextConfiguration(transformJson(
                (BlobQuickQueryJsonSerialization) userSerialization));

        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format("'input' must be one of %s or %s", BlobQuickQueryJsonSerialization.class.getSimpleName(),
                    BlobQuickQueryDelimitedSerialization.class.getSimpleName())));
        }
        return new QuickQuerySerialization().setFormat(generatedFormat);
    }

    /**
     * Transforms a BlobQuickQueryDelimitedSerialization into a DelimitedTextConfiguration.
     *
     * @param delimitedSerialization {@link BlobQuickQueryDelimitedSerialization}
     * @return {@link DelimitedTextConfiguration}
     */
    private static DelimitedTextConfiguration transformDelimited(
        BlobQuickQueryDelimitedSerialization delimitedSerialization) {
        if (delimitedSerialization == null) {
            return null;
        }
        return new DelimitedTextConfiguration()
            .setColumnSeparator(charToString(delimitedSerialization.getColumnSeparator()))
            .setEscapeChar(charToString(delimitedSerialization.getEscapeChar()))
            .setFieldQuote(charToString(delimitedSerialization.getFieldQuote()))
            .setHeadersPresent(delimitedSerialization.isHeadersPresent())
            .setRecordSeparator(charToString(delimitedSerialization.getRecordSeparator()));
    }

    /**
     * Transforms a BlobQuickQueryJsonSerialization into a JsonTextConfiguration.
     *
     * @param jsonSerialization {@link BlobQuickQueryJsonSerialization}
     * @return {@link JsonTextConfiguration}
     */
    private static JsonTextConfiguration transformJson(BlobQuickQueryJsonSerialization jsonSerialization) {
        if (jsonSerialization == null) {
            return null;
        }
        return new JsonTextConfiguration()
            .setRecordSeparator(charToString(jsonSerialization.getRecordSeparator()));
    }

    private static String charToString(char c) {
        return c == '\0' ? "" : Character.toString(c);
    }

}
