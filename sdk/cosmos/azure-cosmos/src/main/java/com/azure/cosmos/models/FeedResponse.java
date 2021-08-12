// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePage;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.QueryMetricsConstants;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.query.QueryInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The type Feed response.
 *
 * @param <T> the type parameter
 */
public class FeedResponse<T> implements ContinuablePage<String, T> {

    private final List<T> results;
    private final Map<String, String> header;
    private final HashMap<String, Long> usageHeaders;
    private final HashMap<String, Long> quotaHeaders;
    private final boolean useEtagAsContinuation;
    final boolean nochanges;
    private final ConcurrentMap<String, QueryMetrics> queryMetricsMap;
    private final static String defaultPartition = "0";
    private CosmosDiagnostics cosmosDiagnostics;
    private QueryInfo queryInfo;
    private QueryInfo.QueryPlanDiagnosticsContext queryPlanDiagnosticsContext;

    FeedResponse(List<T> results, Map<String, String> headers) {
        this(results, headers, false, false, new ConcurrentHashMap<>());
    }

    // TODO: probably have to add two booleans
    FeedResponse(List<T> results, RxDocumentServiceResponse response) {
        this(results, response.getResponseHeaders(), false, false, new ConcurrentHashMap<>());
        this.cosmosDiagnostics =response.getCosmosDiagnostics();
        if (this.cosmosDiagnostics != null) {
            BridgeInternal.setFeedResponseDiagnostics(this.cosmosDiagnostics, queryMetricsMap);
        }
    }

    FeedResponse(
        List<T> results,
        Map<String, String> headers,
        ConcurrentMap<String, QueryMetrics> queryMetricsMap,
        boolean useEtagAsContinuation,
        boolean isNoChanges) {

        this(results, headers, useEtagAsContinuation, isNoChanges, queryMetricsMap);
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
        this.cosmosDiagnostics = BridgeInternal.createCosmosDiagnostics(queryMetricsMap);
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
        return this.maxQuotaHeader(Constants.Quota.DATABASE);
    }

    /**
     * Gets the current number of database resources within the account from the Azure Cosmos DB service.
     *
     * @return The current number of databases.
     */
    public long getDatabaseUsage() {
        return this.currentQuotaHeader(Constants.Quota.DATABASE);
    }

    /**
     * Gets the maximum quota for container resources within an account from the Azure Cosmos DB service.
     *
     * @return The maximum quota for the account.
     */
    public long getCollectionQuota() {
        return this.maxQuotaHeader(Constants.Quota.COLLECTION);
    }

    /**
     * Gets the current number of container resources within the account from the Azure Cosmos DB service.
     *
     * @return The current number of containers.
     */
    public long getCollectionUsage() {
        return this.currentQuotaHeader(Constants.Quota.COLLECTION);
    }

    /**
     * Gets the maximum quota for user resources within an account from the Azure Cosmos DB service.
     *
     * @return The maximum quota for the account.
     */
    public long getUserQuota() {
        return this.maxQuotaHeader(Constants.Quota.USER);
    }

    /**
     * Gets the current number of user resources within the account from the Azure Cosmos DB service.
     *
     * @return The current number of users.
     */
    public long getUserUsage() {
        return this.currentQuotaHeader(Constants.Quota.USER);
    }

    /**
     * Gets the maximum quota for permission resources within an account from the Azure Cosmos DB service.
     *
     * @return The maximum quota for the account.
     */
    public long getPermissionQuota() {
        return this.maxQuotaHeader(Constants.Quota.PERMISSION);
    }

    /**
     * Gets the current number of permission resources within the account from the Azure Cosmos DB service.
     *
     * @return The current number of permissions.
     */
    public long getPermissionUsage() {
        return this.currentQuotaHeader(Constants.Quota.PERMISSION);
    }

    /**
     * Gets the maximum size of a container in kilobytes from the Azure Cosmos DB service.
     *
     * @return The maximum quota in kilobytes.
     */
    public long getCollectionSizeQuota() {
        return this.maxQuotaHeader(Constants.Quota.COLLECTION_SIZE);
    }

    /**
     * Gets the current size of a container in kilobytes from the Azure Cosmos DB service.
     *
     * @return The current size of a container in kilobytes.
     */
    public long getCollectionSizeUsage() {
        return this.currentQuotaHeader(Constants.Quota.COLLECTION_SIZE);
    }

    /**
     * Gets the current size of the documents in a container in kilobytes from the Azure Cosmos DB service.
     *
     * @return The current size of a container in kilobytes.
     */
    public long getDocumentUsage() {
        return this.currentQuotaHeader(Constants.Quota.DOCUMENTS_SIZE);
    }

    /**
     * Current document count usage.
     *
     * @return the document count usage.
     */
    public long getDocumentCountUsage() {
        return this.currentQuotaHeader(Constants.Quota.DOCUMENTS_COUNT);
    }

    /**
     * Gets the maximum quota of stored procedures for a container from the Azure Cosmos DB service.
     *
     * @return The maximum stored procedure quota.
     */
    public long getStoredProceduresQuota() {
        return this.maxQuotaHeader(Constants.Quota.STORED_PROCEDURE);
    }

    /**
     * Gets the current number of stored procedures for a container from the Azure Cosmos DB service.
     *
     * @return The current number of stored procedures.
     */
    public long getStoredProceduresUsage() {
        return this.currentQuotaHeader(Constants.Quota.STORED_PROCEDURE);
    }

    /**
     * Gets the maximum quota of triggers for a container from the Azure Cosmos DB service.
     *
     * @return The maximum triggers quota.
     */
    public long getTriggersQuota() {
        return this.maxQuotaHeader(Constants.Quota.TRIGGER);
    }

    /**
     * Get the current number of triggers for a container from the Azure Cosmos DB service.
     *
     * @return The current number of triggers.
     */
    public long getTriggersUsage() {
        return this.currentQuotaHeader(Constants.Quota.TRIGGER);
    }

    /**
     * Gets the maximum quota of user defined functions for a container from the Azure Cosmos DB service.
     *
     * @return The maximum user defined functions quota.
     */
    public long getUserDefinedFunctionsQuota() {
        return this.maxQuotaHeader(Constants.Quota.USER_DEFINED_FUNCTION);
    }

    /**
     * Gets the current number of user defined functions for a container from the Azure Cosmos DB service.
     *
     * @return the current number of user defined functions.
     */
    public long getUserDefinedFunctionsUsage() {
        return this.currentQuotaHeader(Constants.Quota.USER_DEFINED_FUNCTION);
    }

    /**
     * Gets the maximum size limit for this entity from the Azure Cosmos DB service.
     *
     * @return the maximum size limit for this entity.
     * Measured in kilobytes for item resources and in counts for other resources.
     */
    public String getMaxResourceQuota() {
        return getValueOrNull(header,
            HttpConstants.HttpHeaders.MAX_RESOURCE_QUOTA);
    }

    /**
     * Gets the current size of this entity from the Azure Cosmos DB service.
     *
     * @return the current size for this entity. Measured in kilobytes for item resources
     * and in counts for other resources.
     */
    public String getCurrentResourceQuotaUsage() {
        return getValueOrNull(header,
            HttpConstants.HttpHeaders.CURRENT_RESOURCE_QUOTA_USAGE);
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
        String value = getValueOrNull(header,
            HttpConstants.HttpHeaders.REQUEST_CHARGE);
        if (StringUtils.isEmpty(value)) {
            return 0;
        }
        return Double.parseDouble(value);
    }

    /**
     * Gets the activity ID for the request.
     *
     * @return the activity id.
     */
    public String getActivityId() {
        return getValueOrNull(header, HttpConstants.HttpHeaders.ACTIVITY_ID);
    }

    @Override
    public IterableStream<T> getElements() {
        return IterableStream.of(this.results);
    }

    /**
     * Gets the continuation token to be used for continuing the enumeration.
     *
     * @return the response continuation.
     */
    public String getContinuationToken() {
        String headerName = useEtagAsContinuation
                                ? HttpConstants.HttpHeaders.E_TAG
                                : HttpConstants.HttpHeaders.CONTINUATION;
        return getValueOrNull(header, headerName);
    }

    /**
     * Sets the continuation token to be used for continuing the enumeration.
     *
     * @param continuationToken updates the continuation token header of the response
     */
    void setContinuationToken(String continuationToken) {
        String headerName = useEtagAsContinuation
            ? HttpConstants.HttpHeaders.E_TAG
            : HttpConstants.HttpHeaders.CONTINUATION;

        if (!Strings.isNullOrWhiteSpace(continuationToken)) {
            this.header.put(headerName, continuationToken);
        } else {
            this.header.remove(headerName);
        }
    }

    boolean getNoChanges() {
        return this.nochanges;
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

    private String getQueryMetricsString() {
        return getValueOrNull(getResponseHeaders(),
            HttpConstants.HttpHeaders.QUERY_METRICS);
    }

    /**
     * Gets the feed response diagnostics
     *
     * @return Feed response diagnostics
     */
    public CosmosDiagnostics getCosmosDiagnostics() {
        return this.cosmosDiagnostics;
    }

    ConcurrentMap<String, QueryMetrics> queryMetrics() {
        if (queryMetricsMap != null && !queryMetricsMap.isEmpty()) {
            return queryMetricsMap;
        }

        //We parse query metrics for un-partitioned container here
        if (!StringUtils.isEmpty(getQueryMetricsString())) {
            String qm = getQueryMetricsString();
            qm += String.format(";%s=%.2f", QueryMetricsConstants.RequestCharge, getRequestCharge());
            queryMetricsMap.put(defaultPartition, QueryMetrics.createFromDelimitedString(qm));
        }
        return queryMetricsMap;
    }

    ConcurrentMap<String, QueryMetrics> queryMetricsMap() {
        return queryMetricsMap;
    }

    private long currentQuotaHeader(String headerName) {
        if (this.usageHeaders.size() == 0
                && !StringUtils.isEmpty(this.getMaxResourceQuota())
                && !StringUtils.isEmpty(this.getCurrentResourceQuotaUsage())) {
            this.populateQuotaHeader(this.getMaxResourceQuota(), this.getCurrentResourceQuotaUsage());
        }

        if (this.usageHeaders.containsKey(headerName)) {
            return this.usageHeaders.get(headerName);
        }

        return 0;
    }

    private long maxQuotaHeader(String headerName) {
        if (this.quotaHeaders.size() == 0
                && !StringUtils.isEmpty(this.getMaxResourceQuota())
                && !StringUtils.isEmpty(this.getCurrentResourceQuotaUsage())) {
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
            } else if (headerMaxQuotaWords[i].equalsIgnoreCase(Constants.Quota.DOCUMENTS_SIZE)) {
                this.quotaHeaders.put(Constants.Quota.DOCUMENTS_SIZE, Long.valueOf(headerMaxQuotaWords[i + 1]));
                this.usageHeaders.put(Constants.Quota.DOCUMENTS_SIZE, Long.valueOf(headerCurrentUsageWords[i + 1]));
            } else if (headerMaxQuotaWords[i].equalsIgnoreCase(Constants.Quota.DOCUMENTS_COUNT)) {
                this.quotaHeaders.put(Constants.Quota.DOCUMENTS_COUNT, Long.valueOf(headerMaxQuotaWords[i + 1]));
                this.usageHeaders.put(Constants.Quota.DOCUMENTS_COUNT, Long.valueOf(headerCurrentUsageWords[i + 1]));
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

    void setQueryInfo(QueryInfo queryInfo) {
        this.queryInfo = queryInfo;
    }

    QueryInfo getQueryInfo() {
        return this.queryInfo;
    }

    QueryInfo.QueryPlanDiagnosticsContext getQueryPlanDiagnosticsContext() {
        return queryPlanDiagnosticsContext;
    }

    void setQueryPlanDiagnosticsContext(QueryInfo.QueryPlanDiagnosticsContext queryPlanDiagnosticsContext) {
        this.queryPlanDiagnosticsContext = queryPlanDiagnosticsContext;
        BridgeInternal.setQueryPlanDiagnosticsContext(cosmosDiagnostics, queryPlanDiagnosticsContext);
    }
}
