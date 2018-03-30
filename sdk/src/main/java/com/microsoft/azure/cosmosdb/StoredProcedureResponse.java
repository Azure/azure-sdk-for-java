/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceResponse;

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
    StoredProcedureResponse(RxDocumentServiceResponse response) {
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
     * Gets the number of normalized requests charged.
     *
     * @return the request charge.
     */
    public double getRequestCharge() {
        String value = this.response.getResponseHeaders().get(HttpConstants.HttpHeaders.REQUEST_CHARGE);
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid x-ms-request-charge value {}.", value);
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
     * Gets the response of a stored procedure, serialized into an attachment.
     *
     * @return the response as an attachment.
     */
    public Attachment getResponseAsAttachment() {
        return this.response.getResource(Attachment.class);
    }

    /**
     * Gets the response of a stored procedure as a string.
     *
     * @return the response as a string.
     */
    public String getResponseAsString() {
        return this.response.getReponseBodyAsString();
    }

    /**
     * Gets the output from stored procedure console.log() statements.
     *
     * @return the output string from the stored procedure console.log() statements.
     */
    public String getScriptLog() {
        return this.response.getResponseHeaders().get(HttpConstants.HttpHeaders.SCRIPT_LOG_RESULTS);
    }
}
