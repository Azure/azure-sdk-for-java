// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.batch.ItemBatchOperation;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Response of a {@link TransactionalBatch} request.
 */
public class TransactionalBatchResponse implements AutoCloseable, List<TransactionalBatchOperationResult<?>> {

    private final static Logger logger = LoggerFactory.getLogger(TransactionalBatchResponse.class);

    private Map<String, String> responseHeaders;
    private final int responseStatus;
    private String errorMessage;
    private List<TransactionalBatchOperationResult<?>> results;
    private int subStatusCode;
    private List<ItemBatchOperation<?>> operations;
    private CosmosDiagnostics cosmosDiagnostics;

    /**
     * Initializes a new instance of the {@link TransactionalBatchResponse} class.
     *
     * @param responseStatus the  response status.
     * @param subStatusCode the response sub-status code.
     * @param errorMessage an error message or {@code null}.
     * @param responseHeaders the response http headers
     * @param cosmosDiagnostics the diagnostic
     * @param operations a {@link List list} of {@link ItemBatchOperation batch operations}.
     */
    public TransactionalBatchResponse(
        final int responseStatus,
        final int subStatusCode,
        final String errorMessage,
        final Map<String, String> responseHeaders,
        final CosmosDiagnostics cosmosDiagnostics,
        final List<ItemBatchOperation<?>> operations) {

        checkNotNull(responseStatus, "expected non-null responseStatus");
        checkNotNull(responseHeaders, "expected non-null responseHeaders");
        checkNotNull(operations, "expected non-null operations");

        this.responseStatus = responseStatus;
        this.subStatusCode = subStatusCode;
        this.errorMessage = errorMessage;
        this.responseHeaders = responseHeaders;
        this.cosmosDiagnostics = cosmosDiagnostics;
        this.operations = UnmodifiableList.unmodifiableList(operations);
        this.results = new ArrayList<>();
    }

    public void createAndPopulateResults(final List<ItemBatchOperation<?>> operations, final int retryAfterMilliseconds) {
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
     * @throws IOException if the body of the resource cannot be read.
     */
    public <T> TransactionalBatchOperationResult<T> getOperationResultAtIndex(
        final int index,
        final Class<T> type) throws IOException {

        checkArgument(index >= 0, "expected non-negative index");
        checkNotNull(type, "expected non-null type");

        final TransactionalBatchOperationResult<?> result = this.results.get(index);
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
        return this.responseStatus >= 200 && this.responseStatus <= 299;
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
        return this.responseHeaders.get(HttpConstants.HttpHeaders.ACTIVITY_ID);
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
        final String value = this.responseHeaders.get(HttpConstants.HttpHeaders.REQUEST_CHARGE);
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            logger.warn("INVALID x-ms-request-charge value {}.", value);
            return 0;
        }
    }

    /**
     * Gets the response status code of the batch request.
     *
     * @return the response status code of the batch request.
     */
    public int getResponseStatus() {
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
        if (this.responseHeaders.containsKey(HttpConstants.HttpHeaders.RETRY_AFTER)) {
            return Duration.parse(this.responseHeaders.get(HttpConstants.HttpHeaders.RETRY_AFTER));
        }

        return null;
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
        this.operations = null;
        this.responseHeaders = null;
        this.results = null;
        this.cosmosDiagnostics = null;
    }
}
