// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.quickquery;

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
import com.azure.storage.blob.implementation.models.QuickQuerySerialization;
import com.azure.storage.blob.implementation.models.QuickQueryType;
import com.azure.storage.blob.quickquery.models.BlobQuickQueryAsyncResponse;
import com.azure.storage.blob.quickquery.models.BlobQuickQueryDelimitedSerialization;
import com.azure.storage.blob.quickquery.models.BlobQuickQueryJsonSerialization;
import com.azure.storage.blob.quickquery.models.BlobQuickQuerySerialization;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

public class BlobQuickQueryAsyncClient {

    private final ClientLogger logger = new ClientLogger(BlobQuickQueryAsyncClient.class);

    private final AzureBlobStorageImpl client;

    BlobQuickQueryAsyncClient(String url, HttpPipeline pipeline, BlobServiceVersion serviceVersion) {
        this.client = new AzureBlobStorageBuilder()
            .pipeline(pipeline)
            .url(url)
            .version("2019-12-12")
            .build();
    }

    public Flux<ByteBuffer> query(String expression) {
        return queryWithResponse(expression, null, null)
            .flatMapMany(BlobQuickQueryAsyncResponse::getValue);
    }

    public Mono<BlobQuickQueryAsyncResponse> queryWithResponse(String expression, BlobQuickQuerySerialization input,
        BlobQuickQuerySerialization output) {
        try {
            return withContext(context ->
                queryWithResponse(expression, input, output, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<BlobQuickQueryAsyncResponse> queryWithResponse(String expression, BlobQuickQuerySerialization input,
        BlobQuickQuerySerialization output, Context context) {

        QuickQuerySerialization in = transformSerialization(input, logger);
        QuickQuerySerialization out = transformSerialization(output, logger);

        QueryRequest qr = new QueryRequest()
            .setExpression(expression)
            .setInputSerialization(in)
            .setOutputSerialization(out);


        return client.blobs().quickQueryWithRestResponseAsync(null, null, qr, null, null, null, null, null, null, null,
            null, null, null, null, null, context)
            .map(response -> new BlobQuickQueryAsyncResponse(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), response.getValue(), response.getDeserializedHeaders()));
    }

    private static QuickQuerySerialization transformSerialization(BlobQuickQuerySerialization userSerialization,
        ClientLogger logger) {
        if (userSerialization == null) {
            return null;
        }

        QuickQueryFormat generatedFormat = new QuickQueryFormat();
        if (userSerialization instanceof BlobQuickQueryDelimitedSerialization) {

            generatedFormat.setQuickQueryType(QuickQueryType.DELIMITED);
            generatedFormat.setDelimitedTextConfiguration(transformDelimited(
                (BlobQuickQueryDelimitedSerialization) userSerialization));

        } else if (userSerialization instanceof BlobQuickQueryJsonSerialization) {

            generatedFormat.setQuickQueryType(QuickQueryType.JSON);
            generatedFormat.setJsonTextConfiguration(transformJson(
                (BlobQuickQueryJsonSerialization) userSerialization));

        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format("'input' must be one of %s or %s", BlobQuickQueryJsonSerialization.class.getSimpleName(),
                    BlobQuickQueryDelimitedSerialization.class.getSimpleName())));
        }
        return new QuickQuerySerialization().setFormat(generatedFormat);
    }

    private static DelimitedTextConfiguration transformDelimited(BlobQuickQueryDelimitedSerialization delimitedSerialization) {
        if (delimitedSerialization == null) {
            return null;
        }
        return new DelimitedTextConfiguration()
            .setColumnSeparator(charToString(delimitedSerialization.getColumnSeparator()))
            .setEscapeChar(charToString(delimitedSerialization.getEscapeChar()))
            .setFieldQuote(charToString(delimitedSerialization.getFieldQuote()))
            .setHasHeaders(Boolean.toString(delimitedSerialization.isHeadersPresent()))
            .setRecordSeparator(charToString(delimitedSerialization.getRecordSeparator()));
    }

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
