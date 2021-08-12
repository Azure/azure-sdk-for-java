// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosDiagnostics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Represents the response returned from a stored procedure in the Azure Cosmos DB database service.
 * Wraps the response body and headers.
 */
public final class StoredProcedureResponse {
    private final static Logger logger = LoggerFactory.getLogger(StoredProcedureResponse.class);
    private final RxDocumentServiceResponse response;

    /**
     * Constructs StoredProcedureResponse.
     *
     * @param response the document service response.
     */
    public StoredProcedureResponse(RxDocumentServiceResponse response) {
        this.response = response;
    }

    /**
     * Gets the Activity ID of the request.
     *
     * @return the activity id.
     */
    public String getActivityId() {
        return this.response.getResponseHeaders().get(HttpConstants.HttpHeaders.ACTIVITY_ID);
    }

    /**
     * Gets the token for use with session consistency requests.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return this.response.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
    }

    /**
     * Gets the request completion status code.
     *
     * @return the status code.
     */
    public int getStatusCode() {
        return this.response.getStatusCode();
    }

    /**
     * Gets the maximum size limit for this entity (in megabytes (MB) for server resources and in count for master
     * resources).
     *
     * @return the max resource quota.
     */
    public String getMaxResourceQuota() {
        return this.response.getResponseHeaders().get(HttpConstants.HttpHeaders.MAX_RESOURCE_QUOTA);
    }

    /**
     * Gets the current size of this entity (in megabytes (MB) for server resources and in count for master resources)
     *
     * @return the current resource quota usage.
     */
    public String getCurrentResourceQuotaUsage() {
        return this.response.getResponseHeaders().get(HttpConstants.HttpHeaders.CURRENT_RESOURCE_QUOTA_USAGE);
    }

    /**
     * Gets the request charge as request units (RU) consumed by the operation.
     * <p>
     * For more information about the RU and factors that can impact the effective charges please visit
     * <a href="https://docs.microsoft.com/azure/cosmos-db/request-units">Request Units in Azure Cosmos DB</a>
     *
     * @return the request charge.
     */
    public double getRequestCharge() {
        String value = this.response.getResponseHeaders().get(HttpConstants.HttpHeaders.REQUEST_CHARGE);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.warn("INVALID x-ms-request-charge value {}.", value);
            return 0;
        }
    }

    /**
     * Gets the headers associated with the response.
     *
     * @return the response headers.
     */
    public Map<String, String> getResponseHeaders() {
        return this.response.getResponseHeaders();
    }

    /**
     * Gets the response of a stored procedure, serialized into a document.
     *
     * @return the response as a document.
     */
    public Document getResponseAsDocument() {
        return this.response.getResource(Document.class);
    }

    /**
     * Gets the response of a stored procedure as a string.
     *
     * @return the response as a string.
     */
    public String getResponseAsString() {
        return this.response.getResponseBodyAsString();
    }

    /**
     * Gets the output from stored procedure console.log() statements.
     *
     * @return the output string from the stored procedure console.log() statements.
     */
    public String getScriptLog() {
        String scriptLog = this.response.getResponseHeaders().get(HttpConstants.HttpHeaders.SCRIPT_LOG_RESULTS);
        return Utils.decodeAsUTF8String(scriptLog);
    }

    /**
     * Gets the diagnostic statics for execution of stored procedure.
     *
     * @return diagnostic statistics for execution of stored procedure.
     */
    public CosmosDiagnostics getCosmosDiagnostics() {
        return this.response.getCosmosDiagnostics();
    }

    public RxDocumentServiceResponse getRxDocumentServiceResponse() {
        return response;
    }
}
