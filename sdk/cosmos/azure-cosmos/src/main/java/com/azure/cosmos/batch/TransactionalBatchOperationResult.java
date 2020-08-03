// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.DirectBridgeInternal;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.batch.BatchRequestResponseConstant.*;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents a result for a specific operation that was part of a {@link TransactionalBatch} request.
 *
 * @param <TResource> the type parameter
 */
public class TransactionalBatchOperationResult<TResource> {

    private String eTag;
    private double requestCharge;
    private CosmosDiagnostics cosmosDiagnostics;

    private TResource resource;
    private JSONObject resourceObject;

    /**
     * Gets the completion status of the operation.
     */
    private HttpResponseStatus responseStatus;

    /**
     * In case the operation is rate limited, indicates the time post which a retry can be attempted.
     */
    private Duration retryAfter;

    /**
     * Gets detail on the completion status of the operation.
     */
    private int subStatusCode;

    /**
     * Instantiates a new Transactional batch operation result.
     *
     * @param responseStatus the response status
     */
    public TransactionalBatchOperationResult(final HttpResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    /**
     * Instantiates a new Transactional batch operation result.
     *
     * @param other the other
     */
    public TransactionalBatchOperationResult(final TransactionalBatchOperationResult<?> other) {

        checkNotNull(other, "expected non-null other");

        this.responseStatus = other.responseStatus;
        this.subStatusCode = other.subStatusCode;
        this.eTag = other.eTag;
        this.requestCharge = other.requestCharge;
        this.retryAfter = other.retryAfter;
        this.resourceObject = other.resourceObject;
    }

    /**
     * Instantiates a new Transactional batch operation result.
     *
     * @param result the result
     * @param resource the resource
     */
    public TransactionalBatchOperationResult(TransactionalBatchOperationResult<?> result, TResource resource) {
        this(result);
        this.resource = resource;
    }

    /**
     * Initializes a new instance of the {@link TransactionalBatchOperationResult} class.
     */
    protected TransactionalBatchOperationResult() {
    }

  /**
   * Read batch operation result result.
   *
   * @param jsonResult the value
   *
   * @return the result
   */
  public static TransactionalBatchOperationResult<?> readBatchOperationJsonResult(JSONObject jsonResult) {
        TransactionalBatchOperationResult<?> transactionalBatchOperationResult = new TransactionalBatchOperationResult<>();

        transactionalBatchOperationResult.setResponseStatus(HttpResponseStatus.valueOf(jsonResult.getInt(FIELD_STATUS_CODE)));
        transactionalBatchOperationResult.setSubStatusCode(jsonResult.optInt(FIELD_SUBSTATUS_CODE));
        transactionalBatchOperationResult.setRequestCharge(jsonResult.optDouble(FIELD_REQUEST_CHARGE));
        transactionalBatchOperationResult.setRetryAfter(Duration.ofMillis(jsonResult.optInt(FIELD_RETRY_AFTER_MILLISECONDS)));
        transactionalBatchOperationResult.setETag(jsonResult.optString(FIELD_ETAG));
        transactionalBatchOperationResult.setResourceObject(jsonResult.optJSONObject(FIELD_RESOURCE_BODY));

        return transactionalBatchOperationResult;
    }

    /**
     * Converts the current {@link TransactionalBatchOperationResult transactional batch operation result} to a {@link
     * RxDocumentServiceResponse batch response message}.
     *
     * @return a new {@link RxDocumentServiceResponse batch response message}.
     */
    public final RxDocumentServiceResponse toResponseMessage() {

        Map<String, String> headers =  new HashMap<String, String>() {{
            put(HttpConstants.HttpHeaders.SUB_STATUS, String.valueOf(getSubStatusCode()));
            put(HttpConstants.HttpHeaders.E_TAG, getETag());
            put(HttpConstants.HttpHeaders.REQUEST_CHARGE, String.valueOf(requestCharge));
        }};

        if (getRetryAfter() != null) {
            headers.put(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS, String.valueOf(getRetryAfter().toMillis()));
        }

        StoreResponse storeResponse = new StoreResponse(
            this.getResponseStatus().code(),
            new ArrayList<>(headers.entrySet()),
            this.getResourceObject() != null ? Utils.getUTF8BytesOrNull(this.getResourceObject().toString()) : null);


        if (this.getCosmosDiagnostics() != null) {
            DirectBridgeInternal.setCosmosDiagnostics(storeResponse, this.getCosmosDiagnostics());
        }

        return new RxDocumentServiceResponse(storeResponse);
    }

    /**
     * Gets the Cosmos diagnostic information for the current request to the Azure Cosmos DB service.
     *
     * @return Cosmos diagnostic information for the current request to the Azure Cosmos DB service.
     */
    public CosmosDiagnostics getCosmosDiagnostics() {
        return cosmosDiagnostics;
    }

    /**
     * Sets diagnostics context.
     *
     * @param cosmosDiagnostics the diagnostic value
     */
    public void setCosmosDiagnostics(CosmosDiagnostics cosmosDiagnostics) {
        this.cosmosDiagnostics = cosmosDiagnostics;
    }

    /**
     * Gets the entity tag associated with the current resource.
     * <p>
     * ETags are used for concurrency checking when updating resources.
     *
     * @return Entity tag associated with the current resource.
     */
    public String getETag() {
        return this.eTag;
    }

    /**
     * Sets e tag.
     *
     * @param value the value
     *
     * @return the e tag
     */
    public TransactionalBatchOperationResult<?> setETag(final String value) {
        this.eTag = value;
        return this;
    }

    /**
     * Gets the request charge in request units for the current operation.
     *
     * @return Request charge in request units for the current operation.
     */
    public double getRequestCharge() {
        return requestCharge;
    }

    /**
     * Sets request charge.
     *
     * @param value the value
     *
     * @return the request charge
     */
    public TransactionalBatchOperationResult<?> setRequestCharge(final double value) {
        this.requestCharge = value;
        return this;
    }

    /**
     * Gets the resource associated with the current result.
     *
     * @return Resource associated with the current result.
     */
    public TResource getResource() {
        return this.resource;
    }

    /**
     * Sets resource.
     *
     * @param value the value
     *
     * @return the resource
     */
    public TransactionalBatchOperationResult<TResource> setResource(final TResource value) {
        this.resource = value;
        return this;
    }

    /**
     * Gets retry after.
     *
     * @return the retry after
     */
    public Duration getRetryAfter() {
        return this.retryAfter;
    }

    /**
     * Sets retry after.
     *
     * @param value the value
     *
     * @return the retry after
     */
    public TransactionalBatchOperationResult<?> setRetryAfter(final Duration value) {
        this.retryAfter = value;
        return this;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public HttpResponseStatus getStatus() {
        return this.responseStatus;
    }

    /**
     * Gets status code.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return this.responseStatus.code();
    }

    /**
     * Gets sub status code.
     *
     * @return the sub status code
     */
    public int getSubStatusCode() {
        return this.subStatusCode;
    }

    /**
     * Sets sub status code.
     *
     * @param value the value
     *
     * @return the sub status code
     */
    public TransactionalBatchOperationResult<?> setSubStatusCode(final int value) {
        this.subStatusCode = value;
        return this;
    }

    /**
     * Gets a value indicating whether the current operation completed successfully.
     *
     * @return {@code true} if the current operation completed successfully; {@code false} otherwise.
     */
    public boolean isSuccessStatusCode() {
        final int statusCode = this.responseStatus.code();
        return 200 <= statusCode && statusCode <= 299;
    }

    /**
     * Gets response status.
     *
     * @return the response status
     */
    protected HttpResponseStatus getResponseStatus() {
        return this.responseStatus;
    }

    private TransactionalBatchOperationResult<?> setResponseStatus(HttpResponseStatus value) {
        this.responseStatus = value;
        return this;
    }

    public JSONObject getResourceObject() {
        return resourceObject;
    }

    public void setResourceObject(JSONObject resourceObject) {
        this.resourceObject = resourceObject;
    }
}
