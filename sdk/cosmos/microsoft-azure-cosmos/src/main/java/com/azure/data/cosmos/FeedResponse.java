// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.QueryMetrics;
import com.azure.data.cosmos.internal.QueryMetricsConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FeedResponse<T> {

    private final List<T> results;
    private final Map<String, String> header;
    private final HashMap<String, Long> usageHeaders;
    private final HashMap<String, Long> quotaHeaders;
    private final boolean useEtagAsContinuation;
    boolean nochanges;
    private final ConcurrentMap<String, QueryMetrics> queryMetricsMap;
    private final String DefaultPartition = "0";
    private final FeedResponseDiagnostics feedResponseDiagnostics;

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
        this.feedResponseDiagnostics = new FeedResponseDiagnostics(queryMetricsMap);
    }

    /**
     * Results.
     * 
     * @return the list of results.
     */
    public List<T> results() {
        return results;
    }

    /**
     * Gets the maximum quota for database resources within the account from the Azure Cosmos DB service.
     *
     * @return The maximum quota for the account.
     */
    public long databaseQuota() {
        return this.maxQuotaHeader(Constants.Quota.DATABASE);
    }

    /**
     * Gets the current number of database resources within the account from the Azure Cosmos DB service.
     *
     * @return The current number of databases.
     */
    public long databaseUsage() {
        return this.currentQuotaHeader(Constants.Quota.DATABASE);
    }

    /**
     * Gets the maximum quota for collection resources within an account from the Azure Cosmos DB service.
     *
     * @return The maximum quota for the account.
     */
    public long collectionQuota() {
        return this.maxQuotaHeader(Constants.Quota.COLLECTION);
    }

    /**
     * Gets the current number of collection resources within the account from the Azure Cosmos DB service.
     *
     * @return The current number of collections.
     */
    public long collectionUsage() {
        return this.currentQuotaHeader(Constants.Quota.COLLECTION);
    }

    /**
     * Gets the maximum quota for user resources within an account from the Azure Cosmos DB service.
     *
     * @return The maximum quota for the account.
     */
    public long userQuota() {
        return this.maxQuotaHeader(Constants.Quota.USER);
    }

    /**
     * Gets the current number of user resources within the account from the Azure Cosmos DB service.
     *
     * @return The current number of users.
     */
    public long userUsage() {
        return this.currentQuotaHeader(Constants.Quota.USER);
    }

    /**
     * Gets the maximum quota for permission resources within an account from the Azure Cosmos DB service.
     *
     * @return The maximum quota for the account.
     */
    public long permissionQuota() {
        return this.maxQuotaHeader(Constants.Quota.PERMISSION);
    }

    /**
     * Gets the current number of permission resources within the account from the Azure Cosmos DB service.
     *
     * @return The current number of permissions.
     */
    public long permissionUsage() {
        return this.currentQuotaHeader(Constants.Quota.PERMISSION);
    }

    /**
     * Gets the maximum size of a collection in kilobytes from the Azure Cosmos DB service.
     *
     * @return The maximum quota in kilobytes.
     */
    public long collectionSizeQuota() {
        return this.maxQuotaHeader(Constants.Quota.COLLECTION_SIZE);
    }

    /**
     * Gets the current size of a collection in kilobytes from the Azure Cosmos DB service.
     *
     * @return The current size of a collection in kilobytes.
     */
    public long collectionSizeUsage() {
        return this.currentQuotaHeader(Constants.Quota.COLLECTION_SIZE);
    }

    /**
     * Gets the maximum quota of stored procedures for a collection from the Azure Cosmos DB service.
     *
     * @return The maximum stored procedure quota.
     */
    public long storedProceduresQuota() {
        return this.maxQuotaHeader(Constants.Quota.STORED_PROCEDURE);
    }

    /**
     * Gets the current number of stored procedures for a collection from the Azure Cosmos DB service.
     *
     * @return The current number of stored procedures.
     */
    public long storedProceduresUsage() {
        return this.currentQuotaHeader(Constants.Quota.STORED_PROCEDURE);
    }

    /**
     * Gets the maximum quota of triggers for a collection from the Azure Cosmos DB service.
     *
     * @return The maximum triggers quota.
     */
    public long triggersQuota() {
        return this.maxQuotaHeader(Constants.Quota.TRIGGER);
    }

    /**
     * Get the current number of triggers for a collection from the Azure Cosmos DB service.
     *
     * @return The current number of triggers.
     */
    public long triggersUsage() {
        return this.currentQuotaHeader(Constants.Quota.TRIGGER);
    }

    /**
     * Gets the maximum quota of user defined functions for a collection from the Azure Cosmos DB service.
     *
     * @return The maximum user defined functions quota.
     */
    public long userDefinedFunctionsQuota() {
        return this.maxQuotaHeader(Constants.Quota.USER_DEFINED_FUNCTION);
    }

    /**
     * Gets the current number of user defined functions for a collection from the Azure Cosmos DB service.
     *
     * @return the current number of user defined functions.
     */
    public long userDefinedFunctionsUsage() {
        return this.currentQuotaHeader(Constants.Quota.USER_DEFINED_FUNCTION);
    }

    /**
     * Gets the maximum size limit for this entity from the Azure Cosmos DB service.
     *
     * @return the maximum size limit for this entity.
     * Measured in kilobytes for document resources and in counts for other resources.
     */
    public String maxResourceQuota() {
        return getValueOrNull(header,
                HttpConstants.HttpHeaders.MAX_RESOURCE_QUOTA);
    }

    /**
     * Gets the current size of this entity from the Azure Cosmos DB service.
     *
     * @return the current size for this entity. Measured in kilobytes for document resources
     * and in counts for other resources.
     */
    public String currentResourceQuotaUsage() {
        return getValueOrNull(header,
                HttpConstants.HttpHeaders.CURRENT_RESOURCE_QUOTA_USAGE);
    }

    /**
     * Gets the number of index paths (terms) generated by the operation.
     *
     * @return the request charge.
     */
    public double requestCharge() {
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
    public String activityId() {
        return getValueOrNull(header, HttpConstants.HttpHeaders.ACTIVITY_ID);
    }

    /**
     * Gets the continuation token to be used for continuing the enumeration.
     *
     * @return the response continuation.
     */
    public String continuationToken() {
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
    public String sessionToken() {
        return getValueOrNull(header, HttpConstants.HttpHeaders.SESSION_TOKEN);
    }

    /**
     * Gets the response headers.
     *
     * @return the response headers.
     */
    public Map<String, String> responseHeaders() {
        return header;
    }

    private String queryMetricsString(){
        return getValueOrNull(responseHeaders(),
                HttpConstants.HttpHeaders.QUERY_METRICS);
    }

    /**
     * Gets the feed response diagnostics
     * @return Feed response diagnostics
     */
    public FeedResponseDiagnostics feedResponseDiagnostics() {
        return this.feedResponseDiagnostics;
    }

    ConcurrentMap<String, QueryMetrics> queryMetrics() {
        if (queryMetricsMap != null && !queryMetricsMap.isEmpty()) {
            return queryMetricsMap;
        }

        //We parse query metrics for un-partitioned collection here
        if (!StringUtils.isEmpty(queryMetricsString())) {
            String qm = queryMetricsString();
            qm += String.format(";%s=%.2f", QueryMetricsConstants.RequestCharge, requestCharge());
            queryMetricsMap.put(DefaultPartition, QueryMetrics.createFromDelimitedString(qm));
        }
        return queryMetricsMap;
    }

    ConcurrentMap<String, QueryMetrics> queryMetricsMap(){
        return queryMetricsMap;
    }

    private long currentQuotaHeader(String headerName) {
        if (this.usageHeaders.size() == 0 && !StringUtils.isEmpty(this.maxResourceQuota()) &&
                !StringUtils.isEmpty(this.currentResourceQuotaUsage())) {
            this.populateQuotaHeader(this.maxResourceQuota(), this.currentResourceQuotaUsage());
        }

        if (this.usageHeaders.containsKey(headerName)) {
            return this.usageHeaders.get(headerName);
        }

        return 0;
    }

    private long maxQuotaHeader(String headerName) {
        if (this.quotaHeaders.size() == 0 &&
                !StringUtils.isEmpty(this.maxResourceQuota()) &&
                !StringUtils.isEmpty(this.currentResourceQuotaUsage())) {
            this.populateQuotaHeader(this.maxResourceQuota(), this.currentResourceQuotaUsage());
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
