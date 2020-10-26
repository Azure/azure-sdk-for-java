// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clientTelemetry;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.cpu.CpuMemoryMonitor;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpMethod;
import org.HdrHistogram.DoubleHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientTelemetry {
    public final static int REQUEST_LATENCY_MAX = 300000000;
    public final static int REQUEST_LATENCY_SUCCESS_PRECISION = 4;
    public final static int REQUEST_LATENCY_FAILURE_PRECISION = 2;
    public final static String REQUEST_LATENCY_NAME = "RequestLatency";
    public final static String REQUEST_LATENCY_UNIT = "MicroSec";

    public final static int REQUEST_CHARGE_MAX = 10000;
    public final static int REQUEST_CHARGE_PRECISION = 2;
    public final static String REQUEST_CHARGE_NAME = "RequestCharge";
    public final static String REQUEST_CHARGE_UNIT = "RU";

    public final static int CPU_MAX = 100;
    public final static int CPU_PRECISION = 2;
    private final static String CPU_NAME = "CPU";
    private final static String CPU_UNIT = "Percentage";

    public final static int MEMORY_MAX = 102400;
    public final static int MEMORY_PRECISION = 2;
    private final static String MEMORY_NAME = "MemoryRemaining";
    private final static String MEMORY_UNIT = "MB";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ClientTelemetryInfo clientTelemetryInfo;
    private HttpClient httpClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Scheduler scheduler = Schedulers.fromExecutor(executor);
    private static final Logger logger = LoggerFactory.getLogger(GlobalEndpointManager.class);
    private volatile boolean isClosed;
    private volatile boolean isClientTelemetryEnabled;
    private static String AZURE_VM_METADATA = "http://169.254.169.254:80/metadata/instance?api-version=2020-06-01";

    private static final double PERCENTILE_50 = 50.0;
    private static final double PERCENTILE_90 = 90.0;
    private static final double PERCENTILE_95 = 95.0;
    private static final double PERCENTILE_99 = 99.0;
    private static final double PERCENTILE_999 = 99.9;
    private final int clientTelemetrySchedulingSec;

    public ClientTelemetry(Boolean acceleratedNetworking,
                           String clientId,
                           String processId,
                           String userAgent,
                           ConnectionMode connectionMode,
                           String globalDatabaseAccountName,
                           String applicationRegion,
                           String hostEnvInfo,
                           HttpClient httpClient,
                           boolean isClientTelemetryEnabled,
                           Configs Configs
    ) {
        clientTelemetryInfo = new ClientTelemetryInfo(clientId, processId, userAgent, connectionMode,
            globalDatabaseAccountName, applicationRegion, hostEnvInfo, acceleratedNetworking);
        this.isClosed = false;
        this.httpClient = httpClient;
        this.isClientTelemetryEnabled = isClientTelemetryEnabled;
        this.clientTelemetrySchedulingSec = Configs.getClientTelemetrySchedulingInSec();
    }

    public ClientTelemetryInfo getClientTelemetryInfo() {
        return clientTelemetryInfo;
    }

    public static void recordValue(DoubleHistogram doubleHistogram, long value) {
        try {
            doubleHistogram.recordValue(value);
        } catch (Exception ex) {
            logger.warn("Error while recording value for client telemetry", ex.getMessage());
        }
    }

    public static void recordValue(DoubleHistogram doubleHistogram, double value) {
        try {
            doubleHistogram.recordValue(value);
        } catch (Exception ex) {
            logger.warn("Error while recording value for client telemetry", ex.getMessage());
        }
    }

    public void init() {
        loadAzureVmMetaData();
        sendClientTelemetry().subscribe();
    }

    public void close() {
        this.isClosed = true;
        this.executor.shutdown();
        logger.debug("GlobalEndpointManager closed.");
    }

    private Mono<Void> sendClientTelemetry() {
        return Mono.delay(Duration.ofSeconds(clientTelemetrySchedulingSec))
            .flatMap(t -> {
                if (this.isClosed) {
                    logger.warn("client already closed");
                    return Mono.empty();
                }

                if(!Configs.isClientTelemetryEnabled(this.isClientTelemetryEnabled)) {
                    logger.trace("client telemetry not enabled");
                    return Mono.empty();
                }

                readHistogram();
                try {
                    logger.info("ClientTelemetry {}", OBJECT_MAPPER.writeValueAsString(this.clientTelemetryInfo));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                clearDataForNextRun();
                return this.sendClientTelemetry();
            }).onErrorResume(ex -> {
                logger.error("sendClientTelemetry() - Unable to send client telemetry" +
                    ". Exception: {}", ex.toString(), ex);
                clearDataForNextRun();
                return this.sendClientTelemetry();
            }).subscribeOn(scheduler);
    }

    private void loadAzureVmMetaData() {
        URI targetEndpoint = null;
        try {
            targetEndpoint = new URI(AZURE_VM_METADATA);
        } catch (URISyntaxException ex) {
            logger.info("Unable to parse azure vm metadata url");
            return;
        }
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Metadata", "true");
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, targetEndpoint, targetEndpoint.getPort(),
            httpHeaders);
        Mono<HttpResponse> httpResponseMono = this.httpClient.send(httpRequest);
        httpResponseMono.flatMap(response -> response.bodyAsString()).map(metadataJson -> Utils.parse(metadataJson,
            AzureVMMetadata.class)).doOnSuccess(azureVMMetadata -> {
            this.clientTelemetryInfo.setApplicationRegion(azureVMMetadata.getLocation());
        }).onErrorResume(throwable -> {
            logger.info("Unable to get azure vm metadata");
            return Mono.empty();
        }).subscribe();
    }

    private void clearDataForNextRun() {
        this.clientTelemetryInfo.getOperationInfoMap().clear();
        this.clientTelemetryInfo.getCacheRefreshInfoMap().clear();
        for (DoubleHistogram histogram : this.clientTelemetryInfo.getSystemInfoMap().values()) {
            histogram.reset();
        }
    }

    private void readHistogram() {
        //Filling cpu information
        DoubleHistogram cpuHistogram = new DoubleHistogram(ClientTelemetry.CPU_MAX,
            ClientTelemetry.CPU_PRECISION);
        for(double val : CpuMemoryMonitor.getClientTelemetryCpuLatestList()) {
            recordValue(cpuHistogram, val);
        }
        ReportPayload cpuReportPayload = new ReportPayload(CPU_NAME, CPU_UNIT);
        clientTelemetryInfo.getSystemInfoMap().put(cpuReportPayload, cpuHistogram);

        //Filling memory information
        DoubleHistogram memoryHistogram = new DoubleHistogram(ClientTelemetry.MEMORY_MAX,
            ClientTelemetry.MEMORY_PRECISION);
        for(double val : CpuMemoryMonitor.getClientTelemetryMemoryLatestList()) {
            recordValue(memoryHistogram, val);
        }
        ReportPayload memoryReportPayload = new ReportPayload(MEMORY_NAME, MEMORY_UNIT);
        clientTelemetryInfo.getSystemInfoMap().put(memoryReportPayload, memoryHistogram);

        this.clientTelemetryInfo.setTimeStamp(Instant.now().toString());
        for (Map.Entry<ReportPayload, DoubleHistogram> entry : this.clientTelemetryInfo.getSystemInfoMap().entrySet()) {
            fillMetricsInfo(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<ReportPayload, DoubleHistogram> entry :
            this.clientTelemetryInfo.getCacheRefreshInfoMap().entrySet()) {
            fillMetricsInfo(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<ReportPayload, DoubleHistogram> entry : this.clientTelemetryInfo.getOperationInfoMap().entrySet()) {
            fillMetricsInfo(entry.getKey(), entry.getValue());
        }
    }

    private void fillMetricsInfo(ReportPayload payload, DoubleHistogram histogram) {
        payload.getMetricInfo().setCount(histogram.getTotalCount());
        payload.getMetricInfo().setMax(histogram.getMaxValue());
        payload.getMetricInfo().setMin(histogram.getMinValue());
        payload.getMetricInfo().setMean(histogram.getMean());
        Map<Double, Double> percentile = new HashMap<>();
        percentile.put(PERCENTILE_50, histogram.getValueAtPercentile(PERCENTILE_50));
        percentile.put(PERCENTILE_90, histogram.getValueAtPercentile(PERCENTILE_90));
        percentile.put(PERCENTILE_95, histogram.getValueAtPercentile(PERCENTILE_95));
        percentile.put(PERCENTILE_99, histogram.getValueAtPercentile(PERCENTILE_99));
        percentile.put(PERCENTILE_999, histogram.getValueAtPercentile(PERCENTILE_999));
        payload.getMetricInfo().setPercentiles(percentile);
    }
}
