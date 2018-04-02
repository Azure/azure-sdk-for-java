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

    FeedResponse(List<T> results, Map<String, String> headers) {
        this(results, headers, false, false);
    }

    FeedResponse(List<T> results, Map<String, String> header, boolean nochanges) {
        this(results, header, true, nochanges);
    }

    private FeedResponse(List<T> results, Map<String, String> header, boolean useEtagAsContinuation,
            boolean nochanges) {
        this.results = results;
        this.header = header;
        this.usageHeaders = new HashMap<String, Long>();
        this.quotaHeaders = new HashMap<String, Long>();
        this.useEtagAsContinuation = useEtagAsContinuation;
        this.nochanges = nochanges;
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
     * Max Quota.
     *
     * @return the database quota.
     */
    public long getDatabaseQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.DATABASE);
    }

    /**
     * Current Usage.
     *
     * @return the current database usage.
     */
    public long getDatabaseUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.DATABASE);
    }

    /**
     * Max Quota.
     *
     * @return the collection quota.
     */
    public long getCollectionQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.COLLECTION);
    }

    /**
     * Current Usage.
     *
     * @return the current collection usage.
     */
    public long getCollectionUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.COLLECTION);
    }

    /**
     * Max Quota.
     *
     * @return the user quota.
     */
    public long getUserQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.USER);
    }

    /**
     * Current Usage.
     *
     * @return the current user usage.
     */
    public long getUserUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.USER);
    }

    /**
     * Max Quota.
     *
     * @return the permission quota.
     */
    public long getPermissionQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.PERMISSION);
    }

    /**
     * Current Usage.
     *
     * @return the current permission usage.
     */
    public long getPermissionUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.PERMISSION);
    }

    /**
     * Max Quota.
     *
     * @return the collection size quota.
     */
    public long getCollectionSizeQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.COLLECTION_SIZE);
    }

    /**
     * Current Usage.
     *
     * @return the current collection size usage.
     */
    public long getCollectionSizeUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.COLLECTION_SIZE);
    }

    /**
     * Max Quota.
     *
     * @return the stored procedure quota.
     */
    public long getStoredProceduresQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.STORED_PROCEDURE);
    }

    /**
     * Current Usage.
     *
     * @return the current stored procedure usage.
     */
    public long getStoredProceduresUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.STORED_PROCEDURE);
    }

    /**
     * Max Quota.
     *
     * @return the triggers quota.
     */
    public long getTriggersQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.TRIGGER);
    }

    /**
     * Current Usage.
     *
     * @return the current triggers usage.
     */
    public long getTriggersUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.TRIGGER);
    }

    /**
     * Max Quota.
     *
     * @return the user defined functions quota.
     */
    public long getUserDefinedFunctionsQuota() {
        return this.getMaxQuotaHeader(Constants.Quota.USER_DEFINED_FUNCTION);
    }

    /**
     * Current Usage.
     *
     * @return the current user defined functions usage.
     */
    public long getUserDefinedFunctionsUsage() {
        return this.getCurrentQuotaHeader(Constants.Quota.USER_DEFINED_FUNCTION);
    }

    /**
     * Gets the maximum size limit for this entity (in megabytes (MB) for server resources and in count for master
     * resources).
     *
     * @return the max resource quota.
     */
    public String getMaxResourceQuota() {
        return getValueOrNull(header,
                HttpConstants.HttpHeaders.MAX_RESOURCE_QUOTA);
    }

    /**
     * Gets the current size of this entity (in megabytes (MB) for server resources and in count for master resources).
     *
     * @return the current resource quota usage.
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
