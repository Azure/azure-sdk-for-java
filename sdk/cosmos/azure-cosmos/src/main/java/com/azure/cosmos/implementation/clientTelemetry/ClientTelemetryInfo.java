// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clientTelemetry;

import com.azure.cosmos.ConnectionMode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.HdrHistogram.DoubleHistogram;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@JsonSerialize(using = ClientTelemetrySerializer.class)
public class ClientTelemetryInfo {
    private String timeStamp;
    private String clientId;
    private String processId;
    private String userAgent;
    private ConnectionMode connectionMode;
    private String globalDatabaseAccountName;
    private String applicationRegion;
    private String hostEnvInfo;
    private Boolean acceleratedNetworking;
    private Map<ReportPayload, DoubleHistogram> systemInfoMap;
    private Map<ReportPayload, DoubleHistogram> cacheRefreshInfoMap;
    private Map<ReportPayload, DoubleHistogram> operationInfoMap;

    public ClientTelemetryInfo(String clientId,
                               String processId,
                               String userAgent,
                               ConnectionMode connectionMode,
                               String globalDatabaseAccountName,
                               String applicationRegion,
                               String hostEnvInfo,
                               Boolean acceleratedNetworking) {
        this.clientId = clientId;
        this.processId = processId;
        this.userAgent = userAgent;
        this.connectionMode = connectionMode;
        this.globalDatabaseAccountName = globalDatabaseAccountName;
        this.applicationRegion = applicationRegion;
        this.hostEnvInfo = hostEnvInfo;
        this.acceleratedNetworking = acceleratedNetworking;
        this.systemInfoMap = new ConcurrentHashMap<>();
        this.cacheRefreshInfoMap = new ConcurrentHashMap<>();
        this.operationInfoMap = new ConcurrentHashMap<>();
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(ConnectionMode connectionMode) {
        this.connectionMode = connectionMode;
    }

    public String getGlobalDatabaseAccountName() {
        return globalDatabaseAccountName;
    }

    public void setGlobalDatabaseAccountName(String globalDatabaseAccountName) {
        this.globalDatabaseAccountName = globalDatabaseAccountName;
    }

    public String getApplicationRegion() {
        return applicationRegion;
    }

    public void setApplicationRegion(String applicationRegion) {
        this.applicationRegion = applicationRegion;
    }

    public String getHostEnvInfo() {
        return hostEnvInfo;
    }

    public void setHostEnvInfo(String hostEnvInfo) {
        this.hostEnvInfo = hostEnvInfo;
    }

    public Boolean getAcceleratedNetworking() {
        return acceleratedNetworking;
    }

    public void setAcceleratedNetworking(Boolean acceleratedNetworking) {
        this.acceleratedNetworking = acceleratedNetworking;
    }

    public Map<ReportPayload, DoubleHistogram> getSystemInfoMap() {
        return systemInfoMap;
    }

    public void setSystemInfoMap(Map<ReportPayload, DoubleHistogram> systemInfoMap) {
        this.systemInfoMap = systemInfoMap;
    }

    public Map<ReportPayload, DoubleHistogram> getCacheRefreshInfoMap() {
        return cacheRefreshInfoMap;
    }

    public void setCacheRefreshInfoMap(Map<ReportPayload, DoubleHistogram> cacheRefreshInfoMap) {
        this.cacheRefreshInfoMap = cacheRefreshInfoMap;
    }

    public Map<ReportPayload, DoubleHistogram> getOperationInfoMap() {
        return operationInfoMap;
    }

    public void setOperationInfoMap(Map<ReportPayload, DoubleHistogram> operationInfoMap) {
        this.operationInfoMap = operationInfoMap;
    }
}
