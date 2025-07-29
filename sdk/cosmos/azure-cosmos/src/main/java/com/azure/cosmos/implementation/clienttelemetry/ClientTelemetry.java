// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpMethod;
import org.HdrHistogram.ConcurrentDoubleHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ClientTelemetry {
    public final static boolean DEFAULT_CLIENT_TELEMETRY_ENABLED = false;
    public final static String VM_ID_PREFIX = "vmId_";
    public final static String TCP_NEW_CHANNEL_LATENCY_NAME = "TcpNewChannelOpenLatency";
    public final static String TCP_NEW_CHANNEL_LATENCY_UNIT = "MilliSecond";
    public final static int TCP_NEW_CHANNEL_LATENCY_MAX_MILLI_SEC = 300000;
    public final static int TCP_NEW_CHANNEL_LATENCY_PRECISION = 2;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static AtomicReference<AzureVMMetadata> azureVmMetaDataSingleton =
        new AtomicReference<>(null);
    private final ClientTelemetryInfo clientTelemetryInfo;
    private final boolean clientMetricsEnabled;
    private final Configs configs;
    private final CosmosClientTelemetryConfig clientTelemetryConfig;
    private final HttpClient metadataHttpClient;
    private static final Logger logger = LoggerFactory.getLogger(ClientTelemetry.class);
    private static final String USER_AGENT = Utils.getUserAgent();

    //IMDS Constants
    public static final String IMDS_AZURE_VM_METADATA = "http://169.254.169.254:80/metadata/instance?api-version=2020-06-01";
    public static final Duration IMDS_DEFAULT_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration IMDS_DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    public static final Duration IMDS_DEFAULT_CONNECTION_ACQUIRE_TIMEOUT = Duration.ofSeconds(5);
    public static final int IMDS_DEFAULT_MAX_CONNECTION_POOL_SIZE = 5;

    public ClientTelemetry(DiagnosticsClientContext diagnosticsClientContext,
                           Boolean acceleratedNetworking,
                           String clientId,
                           String processId,
                           ConnectionMode connectionMode,
                           String globalDatabaseAccountName,
                           String applicationRegion,
                           String hostEnvInfo,
                           Configs configs,
                           CosmosClientTelemetryConfig clientTelemetryConfig,
                           List<String> preferredRegions
    ) {
        clientTelemetryInfo = new ClientTelemetryInfo(
            getMachineId(diagnosticsClientContext.getConfig()),
            clientId,
            processId,
            USER_AGENT,
            connectionMode,
            globalDatabaseAccountName,
            applicationRegion,
            hostEnvInfo,
            acceleratedNetworking,
            preferredRegions);

        checkNotNull(clientTelemetryConfig, "Argument 'clientTelemetryConfig' cannot be null");

        this.configs = configs;
        this.clientTelemetryConfig = clientTelemetryConfig;
        ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor
            clientTelemetryAccessor = ImplementationBridgeHelpers
                .CosmosClientTelemetryConfigHelper
                .getCosmosClientTelemetryConfigAccessor();
        assert(clientTelemetryAccessor != null);
        this.clientMetricsEnabled = clientTelemetryAccessor
            .isClientMetricsEnabled(clientTelemetryConfig);
        this.metadataHttpClient = getHttpClientForIMDS(configs);
    }

    public ClientTelemetryInfo getClientTelemetryInfo() {
        return clientTelemetryInfo;
    }

    @JsonIgnore
    public CosmosClientTelemetryConfig getClientTelemetryConfig() {
        return clientTelemetryConfig;
    }

    public static String getMachineId(DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig) {
        AzureVMMetadata metadataSnapshot = azureVmMetaDataSingleton.get();

        if (metadataSnapshot != null && metadataSnapshot.getVmId() != null) {
            String machineId = VM_ID_PREFIX + metadataSnapshot.getVmId();
            if (diagnosticsClientConfig != null) {
                diagnosticsClientConfig.withMachineId(machineId);
            }
            return machineId;
        }

        if (diagnosticsClientConfig == null) {
            return "";
        }

        return diagnosticsClientConfig.getMachineId();
    }

    public static String blockingGetOrLoadMachineId(
        DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig) {

        AzureVMMetadata metadataSnapshot = azureVmMetaDataSingleton.get();

        if (metadataSnapshot == null) {
            loadAzureVmMetaData(null).block();
        }

        metadataSnapshot = azureVmMetaDataSingleton.get();

        if (metadataSnapshot != null && metadataSnapshot.getVmId() != null) {
            String machineId = VM_ID_PREFIX + metadataSnapshot.getVmId();
            if (diagnosticsClientConfig != null) {
                diagnosticsClientConfig.withMachineId(machineId);
            }
            return machineId;
        }

        if (diagnosticsClientConfig == null) {
            return "";
        }

        return diagnosticsClientConfig.getMachineId();
    }

    public static void recordValue(ConcurrentDoubleHistogram doubleHistogram, long value) {
        try {
            doubleHistogram.recordValue(value);
        } catch (Exception ex) {
            logger.warn("Error while recording value for client telemetry. ", ex);
        }
    }

    public boolean isClientMetricsEnabled() {
        return this.clientMetricsEnabled;
    }

    public Mono<?> init() {
        return loadAzureVmMetaData(this);
    }

    public void close() {
        logger.debug("GlobalEndpointManager closed.");
    }

    private static HttpClient getHttpClientForIMDS(Configs configs) {
        // Proxy is not supported for azure instance metadata service
        HttpClientConfig httpClientConfig = new HttpClientConfig(configs)
                .withMaxIdleConnectionTimeout(IMDS_DEFAULT_IDLE_CONNECTION_TIMEOUT)
                .withPoolSize(IMDS_DEFAULT_MAX_CONNECTION_POOL_SIZE)
                .withNetworkRequestTimeout(IMDS_DEFAULT_NETWORK_REQUEST_TIMEOUT)
                .withConnectionAcquireTimeout(IMDS_DEFAULT_CONNECTION_ACQUIRE_TIMEOUT);

        return HttpClient.createFixed(httpClientConfig);
    }

    private void populateAzureVmMetaData(AzureVMMetadata azureVMMetadata) {
        this.clientTelemetryInfo.setApplicationRegion(azureVMMetadata.getLocation());
        this.clientTelemetryInfo.setMachineId(VM_ID_PREFIX + azureVMMetadata.getVmId());
        this.clientTelemetryInfo.setHostEnvInfo(azureVMMetadata.getOsType() + "|" + azureVMMetadata.getSku() +
            "|" + azureVMMetadata.getVmSize() + "|" + azureVMMetadata.getAzEnvironment());
    }

    private static Mono<?> loadAzureVmMetaData(ClientTelemetry thisPtr) {
        if (Configs.shouldDisableIMDSAccess()) {
            logger.info("Access to IMDS to get Azure VM metadata is disabled");
            return Mono.empty();
        }
        AzureVMMetadata metadataSnapshot = azureVmMetaDataSingleton.get();

        if (metadataSnapshot != null) {
            if (thisPtr != null) {
                thisPtr.populateAzureVmMetaData(metadataSnapshot);
            }
            return Mono.empty();
        }

        URI targetEndpoint;
        try {
            targetEndpoint = new URI(IMDS_AZURE_VM_METADATA);
        } catch (URISyntaxException ex) {
            logger.info("Unable to parse azure vm metadata url");
            return Mono.empty();
        }
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Metadata", "true");
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, targetEndpoint, targetEndpoint.getPort(),
            httpHeaders);
        HttpClient httpClient = thisPtr != null ? thisPtr.metadataHttpClient : getHttpClientForIMDS(new Configs());
        Mono<HttpResponse> httpResponseMono =  httpClient.send(httpRequest);

        return httpResponseMono
            .flatMap(HttpResponse::bodyAsString)
            .map(ClientTelemetry::parse)
            .doOnSuccess(metadata -> {
                azureVmMetaDataSingleton.compareAndSet(null, metadata);
                if (thisPtr != null) {
                    thisPtr.populateAzureVmMetaData(metadata);
                }
            }).onErrorResume(throwable -> {
                logger.info("Client is not on azure vm");
                logger.debug("Unable to get azure vm metadata", throwable);
                return Mono.empty();
            });
    }

    private static AzureVMMetadata parse(String itemResponseBodyAsString) {
        try {
            return OBJECT_MAPPER.readValue(itemResponseBodyAsString, AzureVMMetadata.class);
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to parse string [" + itemResponseBodyAsString + "] to POJO.", e);
        }
    }
}
