// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clientTelemetry;

import com.azure.cosmos.ConnectionMode;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class ClientLevelInfo {
    private String timeStamp;//strict contract
    private String clientId;//strict contract
    private String processId;//strict contract
    private String userAgent;//strict contract
    private ConnectionMode connectionMode;//strict contract
    private String globalDatabaseAccountName;//strict contract
    private String applicationRegion;//strict contract
    private String hostEnvInfo;//strict contract
    private Boolean acceleratedNetworking;//strict contract
    private List<ReportPayload> systemInfo;//strict contract
    private List<ReportPayload> cacheRefreshInfo;//strict contract
    private List<ReportPayload> operationInfo;//strict contract
    private Map<ReportPayload, List<Float>> systemInfoMap;//strict contract
    private Map<ReportPayload, List<Float>> cacheRefreshInfoMap;//strict contract
    private Map<ReportPayload, List<Float>> operationInfoMap;//strict contract

    public ClientLevelInfo(String clientId,
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

    public List<ReportPayload> getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(List<ReportPayload> systemInfo) {
        this.systemInfo = systemInfo;
    }

    public List<ReportPayload> getCacheRefreshInfo() {
        return cacheRefreshInfo;
    }

    public void setCacheRefreshInfo(List<ReportPayload> cacheRefreshInfo) {
        this.cacheRefreshInfo = cacheRefreshInfo;
    }

    public List<ReportPayload> getOperationInfo() {
        return operationInfo;
    }

    public void setOperationInfo(List<ReportPayload> operationInfo) {
        this.operationInfo = operationInfo;
    }

    public Map<ReportPayload, List<Float>> getSystemInfoMap() {
        return systemInfoMap;
    }

    public void setSystemInfoMap(Map<ReportPayload, List<Float>> systemInfoMap) {
        this.systemInfoMap = systemInfoMap;
    }

    public Map<ReportPayload, List<Float>> getCacheRefreshInfoMap() {
        return cacheRefreshInfoMap;
    }

    public void setCacheRefreshInfoMap(Map<ReportPayload, List<Float>> cacheRefreshInfoMap) {
        this.cacheRefreshInfoMap = cacheRefreshInfoMap;
    }

    public Map<ReportPayload, List<Float>> getOperationInfoMap() {
        return operationInfoMap;
    }

    public void setOperationInfoMap(Map<ReportPayload, List<Float>> operationInfoMap) {
        this.operationInfoMap = operationInfoMap;
    }
}
