// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.AuthorizationTokenType;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.CosmosDaemonThreadFactory;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.RequestVerb;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RuntimeConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.cpu.CpuMemoryMonitor;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpMethod;
import org.HdrHistogram.ConcurrentDoubleHistogram;
import org.HdrHistogram.DoubleHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

public class ClientTelemetry {
    public final static int ONE_KB_TO_BYTES = 1024;
    public final static int REQUEST_LATENCY_MAX_MICRO_SEC = 300000000;
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

    public final static int MEMORY_MAX_IN_MB = 102400;
    public final static int MEMORY_PRECISION = 2;
    private final static String MEMORY_NAME = "MemoryRemaining";
    private final static String MEMORY_UNIT = "MB";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static AtomicLong instanceCount = new AtomicLong(0);
    private ClientTelemetryInfo clientTelemetryInfo;
    private final HttpClient httpClient;
    private final ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(1,
        new CosmosDaemonThreadFactory("ClientTelemetry-" + instanceCount.incrementAndGet()));
    private final Scheduler scheduler = Schedulers.fromExecutor(scheduledExecutorService);
    private static final Logger logger = LoggerFactory.getLogger(ClientTelemetry.class);
    private volatile boolean isClosed;
    private volatile boolean isClientTelemetryEnabled;
    private static String AZURE_VM_METADATA = "http://169.254.169.254:80/metadata/instance?api-version=2020-06-01";

    private static final double PERCENTILE_50 = 50.0;
    private static final double PERCENTILE_90 = 90.0;
    private static final double PERCENTILE_95 = 95.0;
    private static final double PERCENTILE_99 = 99.0;
    private static final double PERCENTILE_999 = 99.9;
    private final int clientTelemetrySchedulingSec;

    private final IAuthorizationTokenProvider tokenProvider;
    private final String globalDatabaseAccountName;

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
                           IAuthorizationTokenProvider tokenProvider
    ) {
        clientTelemetryInfo = new ClientTelemetryInfo(clientId, processId, userAgent, connectionMode,
            globalDatabaseAccountName, applicationRegion, hostEnvInfo, acceleratedNetworking);
        this.isClosed = false;
        this.httpClient = httpClient;
        this.isClientTelemetryEnabled = isClientTelemetryEnabled;
        this.clientTelemetrySchedulingSec = Configs.getClientTelemetrySchedulingInSec();
        this.tokenProvider = tokenProvider;
        this.globalDatabaseAccountName = globalDatabaseAccountName;
    }

    public ClientTelemetryInfo getClientTelemetryInfo() {
        return clientTelemetryInfo;
    }

    public static void recordValue(ConcurrentDoubleHistogram doubleHistogram, long value) {
        try {
            doubleHistogram.recordValue(value);
        } catch (Exception ex) {
            logger.warn("Error while recording value for client telemetry. ", ex);
        }
    }

    public static void recordValue(ConcurrentDoubleHistogram doubleHistogram, double value) {
        try {
            doubleHistogram.recordValue(value);
        } catch (Exception ex) {
            logger.warn("Error while recording value for client telemetry. ", ex);
        }
    }

    public void init() {
        loadAzureVmMetaData();
        sendClientTelemetry().subscribe();
    }

    public void close() {
        this.isClosed = true;
        this.scheduledExecutorService.shutdown();
        logger.debug("GlobalEndpointManager closed.");
    }

    private Mono<Void> sendClientTelemetry() {
        return Mono.delay(Duration.ofSeconds(clientTelemetrySchedulingSec), CosmosSchedulers.COSMOS_PARALLEL)
            .flatMap(t -> {
                if (this.isClosed) {
                    logger.warn("client already closed");
                    return Mono.empty();
                }

                if (!Configs.isClientTelemetryEnabled(this.isClientTelemetryEnabled)) {
                    logger.trace("client telemetry not enabled");
                    return Mono.empty();
                }
                readHistogram();
                try {
                    String endpoint = Configs.getClientTelemetryEndpoint();
                    if (StringUtils.isEmpty(endpoint)) {
                        logger.info("ClientTelemetry {}",
                            OBJECT_MAPPER.writeValueAsString(this.clientTelemetryInfo));
                        clearDataForNextRun();
                        return this.sendClientTelemetry();
                    } else {
                        URI targetEndpoint = new URI(endpoint);
                        ByteBuffer byteBuffer =
                            BridgeInternal.serializeJsonToByteBuffer(this.clientTelemetryInfo,
                                ClientTelemetry.OBJECT_MAPPER);
                        Flux<byte[]> fluxBytes = Flux.just(RxDocumentServiceRequest.toByteArray(byteBuffer));

                        Map<String, String> headers = new HashMap<>();
                        String date = Utils.nowAsRFC1123();
                        headers.put(HttpConstants.HttpHeaders.X_DATE, date);
                        String authorization = this.tokenProvider.getUserAuthorizationToken(
                            "", ResourceType.ClientTelemetry, RequestVerb.POST, headers,
                            AuthorizationTokenType.PrimaryMasterKey, null);
                        try {
                            authorization = URLEncoder.encode(authorization, Constants.UrlEncodingInfo.UTF_8);
                        } catch (UnsupportedEncodingException e) {
                            logger.error("Failed to encode authToken. Exception: ", e);
                            this.clearDataForNextRun();
                            return this.sendClientTelemetry();
                        }

                        HttpHeaders httpHeaders = new HttpHeaders();
                        httpHeaders.set(HttpConstants.HttpHeaders.CONTENT_TYPE, RuntimeConstants.MediaTypes.JSON);
                        httpHeaders.set(HttpConstants.HttpHeaders.CONTENT_ENCODING, RuntimeConstants.Encoding.GZIP);
                        httpHeaders.set(HttpConstants.HttpHeaders.X_DATE, date);
                        httpHeaders.set(HttpConstants.HttpHeaders.DATABASE_ACCOUNT_NAME,
                            this.globalDatabaseAccountName);
                        httpHeaders.set(HttpConstants.HttpHeaders.AUTHORIZATION, authorization);
                        String envName = Configs.getEnvironmentName();
                        if (StringUtils.isNotEmpty(envName)) {
                            httpHeaders.set(HttpConstants.HttpHeaders.ENVIRONMENT_NAME, envName);
                        }

                        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, targetEndpoint,
                            targetEndpoint.getPort(), httpHeaders, fluxBytes);
                        Mono<HttpResponse> httpResponseMono = this.httpClient.send(httpRequest,
                            Duration.ofSeconds(Configs.getHttpResponseTimeoutInSeconds()));
                        return httpResponseMono.flatMap(response -> {
                            if (response.statusCode() != HttpConstants.StatusCodes.OK) {
                                logger.error("Client telemetry request did not succeeded, status code {}",
                                    response.statusCode());
                            }
                            this.clearDataForNextRun();
                            return this.sendClientTelemetry();
                        }).onErrorResume(throwable -> {
                            logger.error("Error while sending client telemetry request Exception: ", throwable);
                            this.clearDataForNextRun();
                            return this.sendClientTelemetry();
                        });
                    }
                } catch (JsonProcessingException | URISyntaxException ex) {
                    logger.error("Error while preparing client telemetry. Exception: ", ex);
                    this.clearDataForNextRun();
                    return this.sendClientTelemetry();
                }
            }).onErrorResume(ex -> {
                logger.error("sendClientTelemetry() - Unable to send client telemetry" +
                    ". Exception: ", ex);
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
        httpResponseMono.flatMap(response -> response.bodyAsString()).map(metadataJson -> parse(metadataJson,
            AzureVMMetadata.class)).doOnSuccess(azureVMMetadata -> {
            this.clientTelemetryInfo.setApplicationRegion(azureVMMetadata.getLocation());
            this.clientTelemetryInfo.setHostEnvInfo(azureVMMetadata.getOsType() + "|" + azureVMMetadata.getSku() +
                "|" + azureVMMetadata.getVmSize() + "|" + azureVMMetadata.getAzEnvironment());
        }).onErrorResume(throwable -> {
            logger.info("Client is not on azure vm");
            logger.debug("Unable to get azure vm metadata", throwable);
            return Mono.empty();
        }).subscribe();
    }

    private static <T> T parse(String itemResponseBodyAsString, Class<T> itemClassType) {
        try {
            return OBJECT_MAPPER.readValue(itemResponseBodyAsString, itemClassType);
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to parse string [" + itemResponseBodyAsString + "] to POJO.", e);
        }
    }

    private void clearDataForNextRun() {
        this.clientTelemetryInfo.getOperationInfoMap().clear();
        this.clientTelemetryInfo.getCacheRefreshInfoMap().clear();
        for (ConcurrentDoubleHistogram histogram : this.clientTelemetryInfo.getSystemInfoMap().values()) {
            histogram.reset();
        }
    }

    private void readHistogram() {
        //Filling cpu information
        ConcurrentDoubleHistogram cpuHistogram = new ConcurrentDoubleHistogram(ClientTelemetry.CPU_MAX,
            ClientTelemetry.CPU_PRECISION);
        cpuHistogram.setAutoResize(true);
        for (double val : CpuMemoryMonitor.getClientTelemetryCpuLatestList()) {
            recordValue(cpuHistogram, val);
        }
        ReportPayload cpuReportPayload = new ReportPayload(CPU_NAME, CPU_UNIT);
        clientTelemetryInfo.getSystemInfoMap().put(cpuReportPayload, cpuHistogram);

        //Filling memory information
        ConcurrentDoubleHistogram memoryHistogram = new ConcurrentDoubleHistogram(ClientTelemetry.MEMORY_MAX_IN_MB,
            ClientTelemetry.MEMORY_PRECISION);
        memoryHistogram.setAutoResize(true);
        for (double val : CpuMemoryMonitor.getClientTelemetryMemoryLatestList()) {
            recordValue(memoryHistogram, val);
        }
        ReportPayload memoryReportPayload = new ReportPayload(MEMORY_NAME, MEMORY_UNIT);
        clientTelemetryInfo.getSystemInfoMap().put(memoryReportPayload, memoryHistogram);

        this.clientTelemetryInfo.setTimeStamp(Instant.now().toString());
        for (Map.Entry<ReportPayload, ConcurrentDoubleHistogram> entry :
            this.clientTelemetryInfo.getSystemInfoMap().entrySet()) {
            fillMetricsInfo(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<ReportPayload, ConcurrentDoubleHistogram> entry :
            this.clientTelemetryInfo.getCacheRefreshInfoMap().entrySet()) {
            fillMetricsInfo(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<ReportPayload, ConcurrentDoubleHistogram> entry :
            this.clientTelemetryInfo.getOperationInfoMap().entrySet()) {
            fillMetricsInfo(entry.getKey(), entry.getValue());
        }
    }

    private void fillMetricsInfo(ReportPayload payload, ConcurrentDoubleHistogram histogram) {
        DoubleHistogram copyHistogram = histogram.copy();
        payload.getMetricInfo().setCount(copyHistogram.getTotalCount());
        payload.getMetricInfo().setMax(copyHistogram.getMaxValue());
        payload.getMetricInfo().setMin(copyHistogram.getMinValue());
        payload.getMetricInfo().setMean(copyHistogram.getMean());
        Map<Double, Double> percentile = new HashMap<>();
        percentile.put(PERCENTILE_50, copyHistogram.getValueAtPercentile(PERCENTILE_50));
        percentile.put(PERCENTILE_90, copyHistogram.getValueAtPercentile(PERCENTILE_90));
        percentile.put(PERCENTILE_95, copyHistogram.getValueAtPercentile(PERCENTILE_95));
        percentile.put(PERCENTILE_99, copyHistogram.getValueAtPercentile(PERCENTILE_99));
        percentile.put(PERCENTILE_999, copyHistogram.getValueAtPercentile(PERCENTILE_999));
        payload.getMetricInfo().setPercentiles(percentile);
    }
}
