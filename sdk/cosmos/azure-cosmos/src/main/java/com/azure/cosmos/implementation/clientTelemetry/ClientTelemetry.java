// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clientTelemetry;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.cpu.CpuMemoryMonitor;
import com.azure.cosmos.implementation.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

public class ClientTelemetry {
    public final static int TELEMETRY_SCHEDULING_IN_SEC = 60;
    private ClientLevelInfo clientLevelInfo;
    private HttpClient httpClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Scheduler scheduler = Schedulers.fromExecutor(executor);
    private static final Logger logger = LoggerFactory.getLogger(GlobalEndpointManager.class);

    public ClientTelemetry(Boolean acceleratedNetworking,
                           String clientId,
                           String processId,
                           String userAgent,
                           ConnectionMode connectionMode,
                           String globalDatabaseAccountName,
                           String applicationRegion,
                           String hostEnvInfo,
                           HttpClient httpClient
    ) {
        clientLevelInfo = new ClientLevelInfo(clientId, processId, userAgent, connectionMode,
            globalDatabaseAccountName, applicationRegion, hostEnvInfo, acceleratedNetworking);
        ReportPayload cpuReportPayload = new ReportPayload("CPU", "Percentage");
        clientLevelInfo.getSystemInfoMap().put(cpuReportPayload, CpuMemoryMonitor.getCpuLoadForClientTelemetry());

        ReportPayload memoryReportPayload = new ReportPayload("MemoryRemaining", "MB");
        clientLevelInfo.getSystemInfoMap().put(memoryReportPayload, CpuMemoryMonitor.getRemainingForClientTelemetry());
    }

    public ClientLevelInfo getClientLevelInfo() {
        return clientLevelInfo;
    }

    public void clearDataForNextRun() {
        this.clientLevelInfo.getSystemInfoMap().clear();
        this.clientLevelInfo.getCacheRefreshInfoMap().clear();
        this.clientLevelInfo.getOperationInfoMap().clear();
        this.clientLevelInfo.getOperationInfo().clear();
        this.clientLevelInfo.getSystemInfo().clear();
        this.clientLevelInfo.getCacheRefreshInfo().clear();
    }

    public void init() {
        sendClientTelemetry().subscribe();
    }

    private Mono<Void> sendClientTelemetry() {
        return Mono.delay(Duration.ofSeconds(TELEMETRY_SCHEDULING_IN_SEC))
            .flatMap(t -> {
                clearDataForNextRun();
                return this.sendClientTelemetry();
            }).onErrorResume(ex -> {
                logger.error("sendClientTelemetry() - Unable to send client telemetry" +
                    ". Exception: {}", ex.toString(), ex);
                clearDataForNextRun();
                return this.sendClientTelemetry();
            }).subscribeOn(scheduler);
    }
}
