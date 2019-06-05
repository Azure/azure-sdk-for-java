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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.cosmosdb.internal.Constants;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;

public class FeedResponse<T extends Resource> {

    private final List<T> results;
    private final Map<String, String> header;
    private final HashMap<String, Long> usageHeaders;
    private final HashMap<String, Long> quotaHeaders;
    private final boolean useEtagAsContinuation;
    boolean nochanges;
    private final ConcurrentMap<String, QueryMetrics> queryMetricsMap;
    private final String DefaultPartition = "0";

    FeedResponse(List<T> results, Map<String, String> headers) {
        this(results, headers, false, false, new ConcurrentHashMap<>());
    }
    
    FeedResponse(List<T> results, Map<String, String> headers, ConcurrentMap<String, QueryMetrics> queryMetricsMap) {
        this(results, headers, false, false, queryMetricsMap);
    }

    FeedResponse(List<T> results, Map<String, String> header, boolean nochanges) {
        this(results, header, true, nochanges, new ConcurrentHashMap<>());
    }

    // TODO: need to more sure the query metrics can round trip just from the headers.
    // We can then remove it as a parameter.
    private FeedResponse(
            List<T> results, 
            Map<String, String> header, 
            boolean useEtagAsContinuation, 
            boolean nochanges, 
            ConcurrentMap<String, QueryMetrics> queryMetricsMap) {
        this.results = results;
        this.header = header;
        this.usageHeaders = new HashMap<>();
        this.quotaHeaders = new HashMap<>();
        this.useEtagAsContinuation = useEtagAsContinuation;
        this.nochanges = nochanges;
        this.queryMetricsMap = new ConcurrentHashMap<>(queryMetricsMap);
    }

    /**
     * Results.
     * 
     * @return the list of results.
     */
    public List<T> getResults() {
        return results;
    }

    /**
     * Gets the maximum quota for database resources within the account from the Azure Cosmos DB service.
     *
     * @return The maximum quota for the account.
     */
    public long getDatabaseQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.DATABASE);
    }

    /**
     * Gets the current number of database resources within the account from the Azure Cosmos DB service.
     *
     * @return The current number of databases.
     */
    public long getDatabaseUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.DATABASE);
    }

    /**
     * Gets the maximum quota for collection resources within an account from the Azure Cosmos DB service.
     *
     * @return The maximum quota for the account.
     */
    public long getCollectionQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.COLLECTION);
    }

    /**
     * Gets the current number of collection resources within the account from the Azure Cosmos DB service.
     *
     * @return The current number of collections.
     */
    public long getCollectionUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.COLLECTION);
    }

    /**
     * Gets the maximum quota for user resources within an account from the Azure Cosmos DB service.
     *
     * @return The maximum quota for the account.
     */
    public long getUserQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.USER);
    }

    /**
     * Gets the current number of user resources within the account from the Azure Cosmos DB service.
     *
     * @return The current number of users.
     */
    public long getUserUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.USER);
    }

    /**
     * Gets the maximum quota for permission resources within an account from the Azure Cosmos DB service.
     *
     * @return The maximum quota for the account.
     */
    public long getPermissionQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.PERMISSION);
    }

    /**
     * Gets the current number of permission resources within the account from the Azure Cosmos DB service.
     *
     * @return The current number of permissions.
     */
    public long getPermissionUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.PERMISSION);
    }

    /**
     * Gets the maximum size of a collection in kilobytes from the Azure Cosmos DB service.
     *
     * @return The maximum quota in kilobytes.
     */
    public long getCollectionSizeQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.COLLECTION_SIZE);
    }

    /**
     * Gets the current size of a collection in kilobytes from the Azure Cosmos DB service.
     *
     * @return The current size of a collection in kilobytes.
     */
    public long getCollectionSizeUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.COLLECTION_SIZE);
    }

    /**
     * Gets the maximum quota of stored procedures for a collection from the Azure Cosmos DB service.
     *
     * @return The maximum stored procedure quota.
     */
    public long getStoredProceduresQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.STORED_PROCEDURE);
    }

    /**
     * Gets the current number of stored procedures for a collection from the Azure Cosmos DB service.
     *
     * @return The current number of stored procedures.
     */
    public long getStoredProceduresUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.STORED_PROCEDURE);
    }

    /**
     * Gets the maximum quota of triggers for a collection from the Azure Cosmos DB service.
     *
     * @return The maximum triggers quota.
     */
    public long getTriggersQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.TRIGGER);
    }

    /**
     * Get the current number of triggers for a collection from the Azure Cosmos DB service.
     *
     * @return The current number of triggers.
     */
    public long getTriggersUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.TRIGGER);
    }

    /**
     * Gets the maximum quota of user defined functions for a collection from the Azure Cosmos DB service.
     *
     * @return The maximum user defined functions quota.
     */
    public long getUserDefinedFunctionsQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.USER_DEFINED_FUNCTION);
    }

    /**
     * Gets the current number of user defined functions for a collection from the Azure Cosmos DB service.
     *
     * @return the current number of user defined functions.
     */
    public long getUserDefinedFunctionsUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.USER_DEFINED_FUNCTION);
    }

    /**
     * Gets the maximum size limit for this entity from the Azure Cosmos DB service.
     *
     * @return the maximum size limit for this entity.
     * Measured in kilobytes for document resources and in counts for other resources.
     */
    public String getMaxResourceQuota() {
        return getValueOrNull(header,
                HttpConstants.HttpHeaders.MAX_RESOURCE_QUOTA);
    }

    /**
     * Gets the current size of this entity from the Azure Cosmos DB service.
     *
     * @return the current size for this entity. Measured in kilobytes for document resources
     * and in counts for other resources.
     */
    public String getCurrentResourceQuotaUsage() {
        return getValueOrNull(header,
                HttpConstants.HttpHeaders.CURRENT_RESOURCE_QUOTA_USAGE);
    }

    /**
     * Gets the number of index paths (terms) generated by the operation.
     *
     * @return the request charge.
     */
    public double getRequestCharge() {
        String value = getValueOrNull(header,
                HttpConstants.HttpHeaders.REQUEST_CHARGE);
        if (StringUtils.isEmpty(value)) {
            return 0;
        }
        return Double.valueOf(value);
    }

    /**
     * Gets the activity ID for the request.
     *
     * @return the activity id.
     */
    public String getActivityId() {
        return getValueOrNull(header, HttpConstants.HttpHeaders.ACTIVITY_ID);
    }

    /**
     * Gets the continuation token to be used for continuing the enumeration.
     *
     * @return the response continuation.
     */
    public String getResponseContinuation() {
        String headerName = useEtagAsContinuation
                ? HttpConstants.HttpHeaders.E_TAG
                        : HttpConstants.HttpHeaders.CONTINUATION;
        return getValueOrNull(header, headerName);
    }

    /**
     * Gets the session token for use in session consistency.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return getValueOrNull(header, HttpConstants.HttpHeaders.SESSION_TOKEN);
    }

    /**
     * Gets the response headers.
     *
     * @return the response headers.
     */
    public Map<String, String> getResponseHeaders() {
        return header;
    }

    private String getQueryMetricsString(){
        return getValueOrNull(getResponseHeaders(),
                HttpConstants.HttpHeaders.QUERY_METRICS);
    }

    /**
     * Gets the QueryMetrics for each partition.
     *
     * @return the QueryMetrics for each partition.
     */
    public ConcurrentMap<String, QueryMetrics> getQueryMetrics() {
        if (queryMetricsMap != null && !queryMetricsMap.isEmpty()) {
            return queryMetricsMap;
        }

        //We parse query metrics for un-partitioned collection here
        if (!StringUtils.isEmpty(getQueryMetricsString())) {
            String qm = getQueryMetricsString();
            qm += String.format(";%s=%.2f", QueryMetricsConstants.RequestCharge, getRequestCharge());
            queryMetricsMap.put(DefaultPartition, QueryMetrics.createFromDelimitedString(qm));
        }
        return queryMetricsMap;
    }

    ConcurrentMap<String, QueryMetrics> getQueryMetricsMap(){
        return queryMetricsMap;
    }

    private long getCurrentQuotaHeader(String headerName) {
        if (this.usageHeaders.size() == 0 && !StringUtils.isEmpty(this.getMaxResourceQuota()) &&
                !StringUtils.isEmpty(this.getCurrentResourceQuotaUsage())) {
            this.populateQuotaHeader(this.getMaxResourceQuota(), this.getCurrentResourceQuotaUsage());
        }

        if (this.usageHeaders.containsKey(headerName)) {
            return this.usageHeaders.get(headerName);
        }

        return 0;
    }

    private long getMaxQuotaHeader(String headerName) {
        if (this.quotaHeaders.size() == 0 &&
                !StringUtils.isEmpty(this.getMaxResourceQuota()) &&
                !StringUtils.isEmpty(this.getCurrentResourceQuotaUsage())) {
            this.populateQuotaHeader(this.getMaxResourceQuota(), this.getCurrentResourceQuotaUsage());
        }

        if (this.quotaHeaders.containsKey(headerName)) {
            return this.quotaHeaders.get(headerName);
        }

        return 0;
    }

    private void populateQuotaHeader(String headerMaxQuota,
            String headerCurrentUsage) {
        String[] headerMaxQuotaWords = headerMaxQuota.split(Constants.Quota.DELIMITER_CHARS, -1);
        String[] headerCurrentUsageWords = headerCurrentUsage.split(Constants.Quota.DELIMITER_CHARS, -1);

        for (int i = 0; i < headerMaxQuotaWords.length; ++i) {
            if (headerMaxQuotaWords[i].equalsIgnoreCase(Constants.Quota.DATABASE)) {
                this.quotaHeaders.put(Constants.Quota.DATABASE, Long.valueOf(headerMaxQuotaWords[i + 1]));
                this.usageHeaders.put(Constants.Quota.DATABASE, Long.valueOf(headerCurrentUsageWords[i + 1]));
            } else if (headerMaxQuotaWords[i].equalsIgnoreCase(Constants.Quota.COLLECTION)) {
                this.quotaHeaders.put(Constants.Quota.COLLECTION, Long.valueOf(headerMaxQuotaWords[i + 1]));
                this.usageHeaders.put(Constants.Quota.COLLECTION, Long.valueOf(headerCurrentUsageWords[i + 1]));
            } else if (headerMaxQuotaWords[i].equalsIgnoreCase(Constants.Quota.USER)) {
                this.quotaHeaders.put(Constants.Quota.USER, Long.valueOf(headerMaxQuotaWords[i + 1]));
                this.usageHeaders.put(Constants.Quota.USER, Long.valueOf(headerCurrentUsageWords[i + 1]));
            } else if (headerMaxQuotaWords[i].equalsIgnoreCase(Constants.Quota.PERMISSION)) {
                this.quotaHeaders.put(Constants.Quota.PERMISSION, Long.valueOf(headerMaxQuotaWords[i + 1]));
                this.usageHeaders.put(Constants.Quota.PERMISSION, Long.valueOf(headerCurrentUsageWords[i + 1]));
            } else if (headerMaxQuotaWords[i].equalsIgnoreCase(Constants.Quota.COLLECTION_SIZE)) {
                this.quotaHeaders.put(Constants.Quota.COLLECTION_SIZE, Long.valueOf(headerMaxQuotaWords[i + 1]));
                this.usageHeaders.put(Constants.Quota.COLLECTION_SIZE, Long.valueOf(headerCurrentUsageWords[i + 1]));
            } else if (headerMaxQuotaWords[i].equalsIgnoreCase(Constants.Quota.STORED_PROCEDURE)) {
                this.quotaHeaders.put(Constants.Quota.STORED_PROCEDURE, Long.valueOf(headerMaxQuotaWords[i + 1]));
                this.usageHeaders.put(Constants.Quota.STORED_PROCEDURE, Long.valueOf(headerCurrentUsageWords[i + 1]));
            } else if (headerMaxQuotaWords[i].equalsIgnoreCase(Constants.Quota.TRIGGER)) {
                this.quotaHeaders.put(Constants.Quota.TRIGGER, Long.valueOf(headerMaxQuotaWords[i + 1]));
                this.usageHeaders.put(Constants.Quota.TRIGGER, Long.valueOf(headerCurrentUsageWords[i + 1]));
            } else if (headerMaxQuotaWords[i].equalsIgnoreCase(Constants.Quota.USER_DEFINED_FUNCTION)) {
                this.quotaHeaders.put(Constants.Quota.USER_DEFINED_FUNCTION, Long.valueOf(headerMaxQuotaWords[i + 1]));
                this.usageHeaders.put(Constants.Quota.USER_DEFINED_FUNCTION,
                        Long.valueOf(headerCurrentUsageWords[i + 1]));
            }
        }
    }

    private static String getValueOrNull(Map<String, String> map, String key) {
        if (map != null) {
            return map.get(key);
        }
        return null;
    }
}
