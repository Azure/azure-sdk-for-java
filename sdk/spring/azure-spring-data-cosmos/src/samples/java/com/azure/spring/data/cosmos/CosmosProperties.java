// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;
/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 */

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.cosmos")
public class CosmosProperties {

    private String uri;

    private String key;

    private String secondaryKey;

    private String database;

    private boolean queryMetricsEnabled;

    private boolean indexMetricsEnabled;

    private int maxDegreeOfParallelism;

    private int maxBufferedItemCount;

    private int responseContinuationTokenLimitInKb;

    private int pointOperationLatencyThresholdInMS;

    private int nonPointOperationLatencyThresholdInMS;

    private int requestChargeThresholdInRU;

    private int payloadSizeThresholdInBytes;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecondaryKey() {
        return secondaryKey;
    }

    public void setSecondaryKey(String secondaryKey) {
        this.secondaryKey = secondaryKey;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public boolean isQueryMetricsEnabled() {
        return queryMetricsEnabled;
    }

    public boolean isIndexMetricsEnabled() {
        return indexMetricsEnabled;
    }

    public void setQueryMetricsEnabled(boolean enableQueryMetrics) {
        this.queryMetricsEnabled = enableQueryMetrics;
    }

    public void setIndexMetricsEnabled(boolean indexMetricsEnabled) {
        this.indexMetricsEnabled = indexMetricsEnabled;
    }

    public int getMaxDegreeOfParallelism() {
        return maxDegreeOfParallelism;
    }

    public void setMaxDegreeOfParallelism(int maxDegreeOfParallelism) {
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
    }

    public int getMaxBufferedItemCount() {
        return maxBufferedItemCount;
    }

    public void setMaxBufferedItemCount(int maxBufferedItemCount) {
        this.maxBufferedItemCount = maxBufferedItemCount;
    }

    public int getResponseContinuationTokenLimitInKb() {
        return responseContinuationTokenLimitInKb;
    }

    public void setResponseContinuationTokenLimitInKb(int responseContinuationTokenLimitInKb) {
        this.responseContinuationTokenLimitInKb = responseContinuationTokenLimitInKb;
    }

    public int getPointOperationLatencyThresholdInMS() {
        return pointOperationLatencyThresholdInMS;
    }

    public void setPointOperationLatencyThresholdInMS(int pointOperationLatencyThresholdInMS) {
        this.pointOperationLatencyThresholdInMS = pointOperationLatencyThresholdInMS;
    }

    public int getNonPointOperationLatencyThresholdInMS() {
        return nonPointOperationLatencyThresholdInMS;
    }

    public void setNonPointOperationLatencyThresholdInMS(int nonPointOperationLatencyThresholdInMS) {
        this.nonPointOperationLatencyThresholdInMS = nonPointOperationLatencyThresholdInMS;
    }

    public int getRequestChargeThresholdInRU() {
        return requestChargeThresholdInRU;
    }

    public void setRequestChargeThresholdInRU(int requestChargeThresholdInRU) {
        this.requestChargeThresholdInRU = requestChargeThresholdInRU;
    }

    public int getPayloadSizeThresholdInBytes() {
        return payloadSizeThresholdInBytes;
    }

    public void setPayloadSizeThresholdInBytes(int payloadSizeThresholdInBytes) {
        this.payloadSizeThresholdInBytes = payloadSizeThresholdInBytes;
    }
}
