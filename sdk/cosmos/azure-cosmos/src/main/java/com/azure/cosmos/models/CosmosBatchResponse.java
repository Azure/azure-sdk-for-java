// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.batch.BatchExecUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Response of a {@link CosmosBatch} request.
 */
public final class CosmosBatchResponse {

    private final Map<String, String> responseHeaders;
    private final int statusCode;
    private final String errorMessage;
    private final List<CosmosBatchOperationResult> results;
    private final int subStatusCode;
    private final CosmosDiagnostics cosmosDiagnostics;
    private long opCountPerEvaluation;
    private long retriedOpCountPerEvaluation;
    private long globalOpCount;
    private int targetMaxMicroBatchSize;

    /**
     * Initializes a new instance of the {@link CosmosBatchResponse} class.
     *
     * @param statusCode the response status code.
     * @param subStatusCode the response sub-status code.
     * @param errorMessage an error message or {@code null}.
     * @param responseHeaders the response http headers
     * @param cosmosDiagnostics the diagnostic
     */
    CosmosBatchResponse(
        final int statusCode,
        final int subStatusCode,
        final String errorMessage,
        final Map<String, String> responseHeaders,
        final CosmosDiagnostics cosmosDiagnostics) {

        checkNotNull(statusCode, "expected non-null statusCode");
        checkNotNull(responseHeaders, "expected non-null responseHeaders");

        this.statusCode = statusCode;
        this.subStatusCode = subStatusCode;
        this.errorMessage = errorMessage;
        this.responseHeaders = responseHeaders;
        this.cosmosDiagnostics = cosmosDiagnostics;
        this.results = new ArrayList<>();
    }

    /**
     * Gets the diagnostics information for the current request to Azure Cosmos DB service.
     *
     * @return diagnostics information for the current request to Azure Cosmos DB service.
     */
    public CosmosDiagnostics getDiagnostics() {
        return cosmosDiagnostics;
    }

    /**
     * Gets the number of operation results.
     *
     * @return the number of operations results in this response.
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
        return this.statusCode >= 200 && this.statusCode <= 299;
    }

    /**
     * Gets the activity ID that identifies the server request made to execute the batch.
     *
     * @return the activity ID that identifies the server request made to execute the batch.
     */
    public String getActivityId() {
        return BatchExecUtils.getActivityId(this.responseHeaders);
    }

    /**
     * Gets the reason for the failure of the batch request, if any, or {@code null}.
     *
     * @return the reason for the failure of the batch request, if any, or {@code null}.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Gets the request charge as request units (RU) consumed by the batch operation.
     * <p>
     * For more information about the RU and factors that can impact the effective charges please visit
     * <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/request-units">Request Units in Azure Cosmos DB</a>
     *
     * @return the request charge.
     */
    public double getRequestCharge() {
       return BatchExecUtils.getRequestCharge(this.responseHeaders);
    }

    /**
     * Gets the HTTP status code associated with the response.
     *
     * @return the status code.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the token used for managing client's consistency requirements.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return BatchExecUtils.getSessionToken(this.responseHeaders);
    }

    /**
     * Gets the headers associated with the response.
     *
     * @return the response headers.
     */
    public Map<String, String> getResponseHeaders() {
        return this.responseHeaders;
    }

    /**
     * Gets the amount of time to wait before retrying this or any other request due to throttling.
     *
     * @return the amount of time to wait before retrying this or any other request due to throttling.
     */
    public Duration getRetryAfterDuration() {
        return BatchExecUtils.getRetryAfterDuration(this.responseHeaders);
    }

    /**
     * Gets the HTTP sub status code associated with the response.
     *
     * @return the sub status code.
     */
    public int getSubStatusCode() {
        return this.subStatusCode;
    }

    /**
     * Get all the results of the operations in a batch in an unmodifiable instance so no one can
     * change it in the down path.
     *
     * @return Results of operations in a batch.
     */
    public List<CosmosBatchOperationResult> getResults() {
        return Collections.unmodifiableList(this.results);
    }

    /**
     * Get the length of the response of a batch operation
     * change it in the down path.
     *
     * @return length of the response.
     */
    public int getResponseLength() {
        return BatchExecUtils.getResponseLength(this.responseHeaders);
    }

    /**
     * Gets the end-to-end request latency for the current request to Azure Cosmos DB service.
     *
     * @return end-to-end request latency for the current request to Azure Cosmos DB service.
     */
    public Duration getDuration() {
        if (cosmosDiagnostics == null) {
            return Duration.ZERO;
        }

        return this.cosmosDiagnostics.getDuration();
    }

    /**
     * Set operation count per evaluation
     * @param opCountPerEvaluation  Operation count per evaluation
     * */
    void setOpCountPerEvaluation(long opCountPerEvaluation) {
        this.opCountPerEvaluation = opCountPerEvaluation;
    }

    long getOpCountPerEvaluation() {
        return this.opCountPerEvaluation;
    }

    /**
     * Set global operation count
     * @param globalOpCount  Global operation count
     * */
    void setGlobalOpCount(long globalOpCount) {
        this.globalOpCount = globalOpCount;
    }

    long getGlobalOpCount() {
        return this.globalOpCount;
    }

    /**
     * Set retried operation count per evaluation
     * @param retriedOpCountPerEvaluation  retried operation count per evaluation
     * */
    void setRetriedOpCountPerEvaluation(long retriedOpCountPerEvaluation) {
        this.retriedOpCountPerEvaluation = retriedOpCountPerEvaluation;
    }

    long getRetriedOpCountPerEvaluation() {
        return this.retriedOpCountPerEvaluation;
    }

    /**
     * Set target max micro batch size
     * @param targetMaxMicroBatchSize  target max micro batch size
     * */
    void setTargetMaxMicroBatchSize(int targetMaxMicroBatchSize) {
        this.targetMaxMicroBatchSize = targetMaxMicroBatchSize;
    }

    int getTargetMaxMicroBatchSize() {
        return this.targetMaxMicroBatchSize;
    }

    void addAll(List<? extends CosmosBatchOperationResult> collection) {
        this.results.addAll(collection);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosBatchResponseHelper.setCosmosBatchResponseAccessor(new ImplementationBridgeHelpers.CosmosBatchResponseHelper.CosmosBatchResponseAccessor() {
            @Override
            public List<CosmosBatchOperationResult> getResults(CosmosBatchResponse cosmosBatchResponse) {
                return cosmosBatchResponse.results;
            }

            @Override
            public void setOpCountPerEvaluation(CosmosBatchResponse cosmosBatchResponse, long opCountPerEvaluation) {
                cosmosBatchResponse.setOpCountPerEvaluation(opCountPerEvaluation);
            }

            @Override
            public void setGlobalOpCount(CosmosBatchResponse cosmosBatchResponse, long globalOpCount) {
                cosmosBatchResponse.setGlobalOpCount(globalOpCount);
            }

            @Override
            public void setRetriedOpCountPerEvaluation(CosmosBatchResponse cosmosBatchResponse, long retriedOpCountPerEvaluation) {
                cosmosBatchResponse.setRetriedOpCountPerEvaluation(retriedOpCountPerEvaluation);
            }

            @Override
            public void setTargetMaxMicroBatchSize(CosmosBatchResponse cosmosBatchResponse, int targetMaxMicroBatchSize) {
                cosmosBatchResponse.setTargetMaxMicroBatchSize(targetMaxMicroBatchSize);
            }

            @Override
            public long getOpCountPerEvaluation(CosmosBatchResponse cosmosBatchResponse) {
                return cosmosBatchResponse.getOpCountPerEvaluation();
            }

            @Override
            public long getGlobalOpCount(CosmosBatchResponse cosmosBatchResponse) {
                return cosmosBatchResponse.getGlobalOpCount();
            }

            @Override
            public long getRetriedOpCountPerEvaluation(CosmosBatchResponse cosmosBatchResponse) {
                return cosmosBatchResponse.getRetriedOpCountPerEvaluation();
            }

            @Override
            public int getTargetMaxMicroBatchSize(CosmosBatchResponse cosmosBatchResponse) {
                return cosmosBatchResponse.getTargetMaxMicroBatchSize();
            }
        });
    }

    static { initialize(); }
}
