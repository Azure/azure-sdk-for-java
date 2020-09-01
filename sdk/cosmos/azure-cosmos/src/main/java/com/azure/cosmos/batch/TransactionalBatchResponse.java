// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.batch.implementation.ItemBatchOperation;
import com.azure.cosmos.batch.implementation.ServerBatchRequest;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static com.azure.cosmos.implementation.HttpConstants.HttpHeaders.*;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.*;

/**
 * Response of a {@link com.azure.cosmos.batch.TransactionalBatch} request.
 */
public class TransactionalBatchResponse implements AutoCloseable, List<TransactionalBatchOperationResult<?>> {

    private final static Logger logger = LoggerFactory.getLogger(TransactionalBatchResponse.class);
    private boolean isDisposed;
    private static final String EMPTY_UUID = "00000000-0000-0000-0000-000000000000";
    private static char HYBRID_V1 = 129;

    private final Map<String, String> responseHeaders;
    private final HttpResponseStatus responseStatus;
    private String errorMessage;
    private List<TransactionalBatchOperationResult<?>> results;
    private int subStatusCode;
    private volatile List<ItemBatchOperation<?>> operations;
    private CosmosDiagnostics cosmosDiagnostics;

    /**
     * Initializes a new instance of the {@link TransactionalBatchResponse} class.
     *
     * @param responseStatus the {@link HttpResponseStatus response status}.
     * @param subStatusCode the response sub-status code.
     * @param errorMessage an error message or {@code null}.
     * @param responseHeaders the response http headers
     * @param cosmosDiagnostics the diagnostic
     * @param operations a {@link List list} of {@link ItemBatchOperation batch operations}.
     */
    protected TransactionalBatchResponse(
        final HttpResponseStatus responseStatus,
        final int subStatusCode,
        final String errorMessage,
        Map<String, String> responseHeaders,
        CosmosDiagnostics cosmosDiagnostics,
        List<ItemBatchOperation<?>> operations) {

        checkNotNull(responseStatus, "expected non-null responseStatus");
        checkNotNull(responseHeaders, "expected non-null responseHeaders");
        checkNotNull(operations, "expected non-null operations");

        this.responseStatus = responseStatus;
        this.subStatusCode = subStatusCode;
        this.errorMessage = errorMessage;
        this.responseHeaders = responseHeaders;
        this.cosmosDiagnostics = cosmosDiagnostics;
        this.operations = UnmodifiableList.unmodifiableList(operations);
    }

    /** Creates a transactional batch response} from a exception
     *
     * @param throwable the {@link Throwable error}.
     * @param request the {@link ServerBatchRequest batch request} that produced {@code message}.
     *
     * @return a Mono that provides the {@link TransactionalBatchResponse transactional batch response} created
     * from {@link TransactionalBatchResponse message} when the asynchronous operation completes.
     */
    public static Mono<TransactionalBatchResponse> fromResponseMessageAsync(
        final Throwable throwable,
        final ServerBatchRequest request) {

        if (throwable instanceof CosmosException) {
            CosmosException cosmosException = (CosmosException) throwable;
            TransactionalBatchResponse response =  new TransactionalBatchResponse(
                HttpResponseStatus.valueOf(cosmosException.getStatusCode()),
                cosmosException.getSubStatusCode(),
                cosmosException.getMessage(),
                cosmosException.getResponseHeaders(),
                cosmosException.getDiagnostics(),
                request.getOperations());

            response.createAndPopulateResults(request.getOperations(), 0);
            return Mono.just(response);
        } else {
            return Mono.error(throwable);
        }
    }

    /** Creates a transactional batch response} from a response message
     *
     * @param documentServiceResponse the {@link RxDocumentServiceResponse response message}.
     * @param request the {@link ServerBatchRequest batch request} that produced {@code message}.
     * @param shouldPromoteOperationStatus indicates whether the operation status should be promoted.
     *
     * @return a future that provides the {@link TransactionalBatchResponse transactional batch response} created
     * from {@link TransactionalBatchResponse message} when the asynchronous operation completes.
     */
    public static Mono<TransactionalBatchResponse> fromResponseMessageAsync(
        final RxDocumentServiceResponse documentServiceResponse,
        final ServerBatchRequest request,
        final boolean shouldPromoteOperationStatus) {

        TransactionalBatchResponse response = null;
        String responseContent = documentServiceResponse.getResponseBodyAsString();

        if (StringUtils.isNotEmpty(responseContent)) {
            response = TransactionalBatchResponse.populateFromContentAsync(documentServiceResponse, request, shouldPromoteOperationStatus);

            if (response == null) {
                // Convert any payload read failures as InternalServerError
                response = new TransactionalBatchResponse(
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    "ServerResponseDeserializationFailure",
                    documentServiceResponse.getResponseHeaders(),
                    documentServiceResponse.getCosmosDiagnostics(),
                    request.getOperations());
            }
        }

        HttpResponseStatus responseStatusCode = HttpResponseStatus.valueOf(documentServiceResponse.getStatusCode());
        int responseSubStatusCode = Integer.parseInt(
            documentServiceResponse.getResponseHeaders().getOrDefault(SUB_STATUS, String.valueOf(HttpConstants.SubStatusCodes.UNKNOWN)));

        if (response == null) {
            response = new TransactionalBatchResponse(
                responseStatusCode,
                responseSubStatusCode,
                null, // Figure out error message
                documentServiceResponse.getResponseHeaders(),
                documentServiceResponse.getCosmosDiagnostics(),
                request.getOperations());
        }

        if (response.results == null || response.results.size() != request.getOperations().size()) {
            if (responseStatusCode.code() >= 200 && responseStatusCode.code() <= 299)  {
                // Server should be guaranteeing number of results equal to operations when
                // batch request is successful - so fail as InternalServerError if this is not the case.
                response = new TransactionalBatchResponse(
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    "Invalid server response",
                    documentServiceResponse.getResponseHeaders(),
                    documentServiceResponse.getCosmosDiagnostics(),
                    request.getOperations());
            }

            // When the overall response status code is TooManyRequests, propagate the RetryAfter into the individual operations.
            int retryAfterMilliseconds = 0;

            if (responseStatusCode == HttpResponseStatus.TOO_MANY_REQUESTS) {
                String retryResponseValue = documentServiceResponse.getResponseHeaders().getOrDefault(RETRY_AFTER_IN_MILLISECONDS, null);
                if (StringUtils.isNotEmpty(retryResponseValue)) {
                    try {
                        retryAfterMilliseconds = Integer.parseInt(retryResponseValue);
                    } catch (NumberFormatException ex) {
                        // Do nothing. It's number format exception
                    }
                }
            }

            response.createAndPopulateResults(request.getOperations(), retryAfterMilliseconds);
        }

        checkState(response.results.size() == request.getOperations().size(),
            "Number of responses should be equal to number of operations in request.");

        return Mono.just(response);
    }

    private static TransactionalBatchResponse populateFromContentAsync(
        final RxDocumentServiceResponse documentServiceResponse,
        final ServerBatchRequest request,
        final boolean shouldPromoteOperationStatus) {

        ArrayList<TransactionalBatchOperationResult<?>> results = new ArrayList<>();
        String responseContent = documentServiceResponse.getResponseBodyAsString();

        if (responseContent.charAt(0) != HYBRID_V1) {
            // Read from a json response body. To enable hybrid row just complete the else part
            ObjectMapper mapper = Utils.getSimpleObjectMapper();

            try{
                ObjectNode [] objectNodes = mapper.readValue(documentServiceResponse.getResponseBodyAsString(), ObjectNode[].class);
                for (ObjectNode objectInArray : objectNodes) {
                    TransactionalBatchOperationResult<?> batchOperationResult = TransactionalBatchOperationResult.readBatchOperationJsonResult(objectInArray);
                    results.add(batchOperationResult);
                }
            } catch (IOException ex) {
                System.out.println("Exception in parsing response {}" + ex);
            }

        } else {
            // TODO(rakkuma): Implement hybrid row response parsing logic here. Parse the response hybrid row buffer
            //  into array list of TransactionalBatchOperationResult. Remaining part is taken care from the caller function.
            logger.error("Hybrid row is not implemented right now");
            return null;
        }

        HttpResponseStatus responseStatusCode = HttpResponseStatus.valueOf(documentServiceResponse.getStatusCode());
        int responseSubStatusCode = Integer.parseInt(
            documentServiceResponse.getResponseHeaders().getOrDefault(SUB_STATUS, String.valueOf(HttpConstants.SubStatusCodes.UNKNOWN)));

        if (responseStatusCode == HttpResponseStatus.MULTI_STATUS
            && shouldPromoteOperationStatus) {
            for (TransactionalBatchOperationResult<?> result : results) {
                if (result.getStatus()!= HttpResponseStatus.FAILED_DEPENDENCY) {
                    responseStatusCode = result.getStatus();
                    responseSubStatusCode = result.getSubStatusCode();
                    break;
                }
            }
        }

        TransactionalBatchResponse response = new TransactionalBatchResponse(
            responseStatusCode,
            responseSubStatusCode,
            null,
            documentServiceResponse.getResponseHeaders(),
            documentServiceResponse.getCosmosDiagnostics(),
            request.getOperations());

        response.results = results;

        return response;
    }

    private void createAndPopulateResults(List<ItemBatchOperation<?>> operations, int retryAfterMilliseconds) {
        this.results = new ArrayList<>();

        for (int i = 0; i < operations.size(); i++) {
            this.results.add(
                new TransactionalBatchOperationResult<>(this.getResponseStatus())
                    .setSubStatusCode(this.getSubStatusCode())
                    .setRetryAfter(Duration.ofMillis(retryAfterMilliseconds)));
        }
    }

    /**
     * Gets the result of the operation at the provided index in the current {@link TransactionalBatchResponse batch}.
     * <p>
     * @param <T> the type parameter.
     * @param index 0-based index of the operation in the batch whose result needs to be returned.
     * de-serialized, when present.
     * @param type class type for which deserialization is needed.
     *
     * @return TransactionalBatchOperationResult containing the individual result of operation.
     * @throws IOException if the body of the resource stream cannot be read.
     */
    public <T> TransactionalBatchOperationResult<T> getOperationResultAtIndex(
        final int index,
        final Class<T> type) throws IOException {

        checkArgument(index >= 0, "expected non-negative index");
        checkNotNull(type, "expected non-null type");

        TransactionalBatchOperationResult<?> result = this.results.get(index);
        T resource = null;

        if (result.getResourceObject() != null) {
            resource = new JsonSerializable(result.getResourceObject()).toObject(type);
        }

        return new TransactionalBatchOperationResult<T>(result, resource);
    }

    public CosmosDiagnostics getCosmosDiagnostics() {
        return cosmosDiagnostics;
    }

    /**
     * Gets the number of operation results.
     */
    public int size() {
        return this.results == null ? 0 : this.results.size();
    }

    /**
     * Returns a value indicating whether the batch was successfully processed.
     *
     * @return a value indicating whether the batch was successfully processed.
     */
    public boolean isSuccessStatusCode() {
        int statusCode = this.responseStatus.code();
        return statusCode >= 200 && statusCode <= 299;
    }

    /**
     * Gets all the activity IDs associated with the response.
     *
     * @return an enumerable that contains the activity IDs.
     */
    public Iterable<String> getActivityIds() {
        return Stream.of(this.getActivityId())::iterator;
    }

    /**
     * Gets the activity ID that identifies the server request made to execute the batch.
     *
     * @return the activity ID that identifies the server request made to execute the batch.
     */
    public String getActivityId() {
        return this.responseHeaders.getOrDefault(ACTIVITY_ID, EMPTY_UUID);
    }

    public final List<ItemBatchOperation<?>> getBatchOperations() {
        return this.operations;
    }

    /**
     * Gets the reason for the failure of the batch request, if any, or {@code null}.
     *
     * @return the reason for the failure of the batch request, if any, or {@code null}.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    /**
     * Gets the request charge for the batch request.
     *
     * @return the request charge measured in request units.
     */
    public double getRequestCharge() {
        return Double.parseDouble(this.responseHeaders.getOrDefault(REQUEST_CHARGE, "0"));
    }

    /**
     * Gets the response status code of the batch request.
     *
     * @return the response status code of the batch request.
     */
    public HttpResponseStatus getResponseStatus() {
        return this.responseStatus;
    }

    /**
     * Gets the response headers.
     *
     * @return the response header map.
     */
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Gets the amount of time to wait before retrying this or any other request due to throttling.
     *
     * @return the amount of time to wait before retrying this or any other request due to throttling.
     */
    public Duration getRetryAfter() {
        return Duration.parse(this.responseHeaders.getOrDefault(RETRY_AFTER, null));
    }

    public int getSubStatusCode() {
        return this.subStatusCode;
    }

    /**
     * Gets the result of the operation at the provided index in the batch.
     *
     * @param index 0-based index of the operation in the batch whose result needs to be returned.
     *
     * @return Result of operation at the provided index in the batch.
     */
    @Override
    public TransactionalBatchOperationResult<?> get(int index) {
        return this.results.get(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.results.indexOf(o);
    }

    @Override
    public Iterator<TransactionalBatchOperationResult<?>> iterator() {
        return this.results.iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<TransactionalBatchOperationResult<?>> listIterator() {
        return null;
    }

    @Override
    public ListIterator<TransactionalBatchOperationResult<?>> listIterator(int index) {
        return null;
    }

    @Override
    public boolean remove(Object result) {
        return this.results.remove(result);
    }

    @Override
    public TransactionalBatchOperationResult<?> remove(int index) {
        return null;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return this.results.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return this.results.retainAll(collection);
    }

    @Override
    public TransactionalBatchOperationResult<?> set(int index, TransactionalBatchOperationResult<?> result) {
        return this.results.set(index, result);
    }

    @Override
    public List<TransactionalBatchOperationResult<?>> subList(int fromIndex, int toIndex) {
        return this.results.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return this.results.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.results.toArray(a);
    }

    @Override
    public boolean isEmpty() {
        return this.results.isEmpty();
    }

    @Override
    public boolean add(TransactionalBatchOperationResult<?> result) {
        return this.results.add(result);
    }

    @Override
    public void add(int index, TransactionalBatchOperationResult<?> element) {
        this.results.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends TransactionalBatchOperationResult<?>> collection) {
        return this.results.addAll(collection);
    }

    @Override
    public boolean addAll(int index, Collection<? extends TransactionalBatchOperationResult<?>> collection) {
        return this.results.addAll(index, collection);
    }

    @Override
    public void clear() {
        this.results.clear();
    }

    @Override
    public boolean contains(Object result) {
        return this.results.contains(result);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    /**
     * Closes the current {@link TransactionalBatchResponse}.
     */
    public void close() {
        if (!this.isDisposed) {
            this.isDisposed = true;
            if (operations != null) {
                for (ItemBatchOperation<?> operation : operations) {
                    operation.close();
                }

                this.operations = null;
            }
        }
    }
}
