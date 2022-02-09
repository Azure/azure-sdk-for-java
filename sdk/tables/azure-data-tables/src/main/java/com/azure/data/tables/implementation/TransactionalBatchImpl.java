// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.tables.implementation.models.TransactionalBatchRequestBody;
import com.azure.data.tables.implementation.models.TransactionalBatchResponse;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import reactor.core.publisher.Mono;

/**
 * An instance of this class provides access to transactional batch operations.
 */
public final class TransactionalBatchImpl {
    /**
     * The proxy service used to perform REST calls.
     */
    private final TransactionalBatchService service;

    /**
     * The service client containing this operation class.
     */
    private final AzureTableImpl client;

    /**
     * Initializes an instance of {@link TransactionalBatchImpl}.
     *
     * @param client The instance of the service client containing this operation class.
     * @param transactionalBatchSerializer Serializer adapter used to handle requests and responses in this
     * implementation client.
     */
    public TransactionalBatchImpl(AzureTableImpl client, SerializerAdapter transactionalBatchSerializer) {
        this.service = RestProxy.create(TransactionalBatchService.class, client.getHttpPipeline(), transactionalBatchSerializer);
        this.client = client;
    }

    /**
     * The interface defining all the services for {@link TransactionalBatchImpl} to be used by the proxy service to
     * perform REST calls.
     */
    @Host("{url}")
    @ServiceInterface(name = "AzureTableServices")
    public interface TransactionalBatchService {
        @Post("/$batch")
        @ExpectedResponses({202})
        @UnexpectedResponseExceptionType(TableServiceErrorException.class)
        Mono<TransactionalBatchResponse> submitTransactionalBatch(
            @HostParam("url") String url,
            @HeaderParam("Content-Type") String multipartContentType,
            @HeaderParam("x-ms-version") String version,
            @HeaderParam("x-ms-client-request-id") String requestId,
            @HeaderParam("DataServiceVersion") String dataServiceVersion,
            @BodyParam("multipart/mixed") TransactionalBatchRequestBody body,
            Context context);
    }

    /**
     * The submit transactional batch operation allows multiple API calls to be embedded into a single HTTP request.
     *
     * @param body Initial data.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the
     * analytics logs when storage analytics logging is enabled.
     * @param context The context to associate with this operation.
     *
     * @return A reactive result containing a {@link TransactionalBatchResponse}.
     *
     * @throws IllegalArgumentException If parameters fail validation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TransactionalBatchResponse> submitTransactionalBatchWithRestResponseAsync(TransactionalBatchRequestBody body, String requestId, Context context) {
        final String dataServiceVersion = "3.0";

        return service.submitTransactionalBatch(this.client.getUrl(), body.getContentType(), this.client.getVersion(), requestId,
            dataServiceVersion, body, context);
    }
}
