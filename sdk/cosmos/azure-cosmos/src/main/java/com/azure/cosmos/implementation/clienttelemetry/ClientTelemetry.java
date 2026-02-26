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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Manages per-client telemetry info and shared VM metadata (IMDS).
 *
 * <p>VM metadata is fetched once per JVM from the Azure Instance Metadata Service (IMDS)
 * and cached in a static singleton. The IMDS HTTP client is created on-demand for the
 * first metadata fetch and disposed immediately after; no long-lived HTTP client is kept.</p>
 */
public class ClientTelemetry {
    public final static String VM_ID_PREFIX = "vmId_";
    public final static boolean DEFAULT_CLIENT_TELEMETRY_ENABLED = false;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(ClientTelemetry.class);
    private static final String USER_AGENT = Utils.getUserAgent();

    // Cached IMDS metadata Mono. Reactor's cache() ensures:
    // - The fetch executes at most once
    // - All concurrent subscribers share the single result
    // - The HTTP client is created and disposed within the fetch
    private static final Mono<AzureVMMetadata> CACHED_METADATA = fetchAzureVmMetadata().cache();

    // Sentinel for "not on Azure VM" or "IMDS unreachable"
    private static final AzureVMMetadata METADATA_NOT_AVAILABLE = new AzureVMMetadata();

    // IMDS Constants
    private static final String IMDS_AZURE_VM_METADATA = "http://169.254.169.254:80/metadata/instance?api-version=2020-06-01";
    private static final Duration IMDS_DEFAULT_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration IMDS_DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration IMDS_DEFAULT_CONNECTION_ACQUIRE_TIMEOUT = Duration.ofSeconds(5);
    private static final int IMDS_DEFAULT_MAX_CONNECTION_POOL_SIZE = 5;

    // Per-instance fields
    private final ClientTelemetryInfo clientTelemetryInfo;
    private final boolean clientMetricsEnabled;
    private final CosmosClientTelemetryConfig clientTelemetryConfig;

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

        this.clientTelemetryConfig = clientTelemetryConfig;
        ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor
            clientTelemetryAccessor = ImplementationBridgeHelpers
                .CosmosClientTelemetryConfigHelper
                .getCosmosClientTelemetryConfigAccessor();
        assert(clientTelemetryAccessor != null);
        this.clientMetricsEnabled = clientTelemetryAccessor
            .isClientMetricsEnabled(clientTelemetryConfig);
    }

    public ClientTelemetryInfo getClientTelemetryInfo() {
        return clientTelemetryInfo;
    }

    @JsonIgnore
    public CosmosClientTelemetryConfig getClientTelemetryConfig() {
        return clientTelemetryConfig;
    }

    /**
     * Blocking version of machine ID lookup. Used by Spark connector (CosmosClientCache.scala).
     * Delegates to getMachineId which waits up to 5s for IMDS metadata.
     */
    public static String blockingGetOrLoadMachineId(
            DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig) {
        return getMachineId(diagnosticsClientConfig);
    }

    public static String getMachineId(DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig) {
        // Try to get cached metadata (non-blocking if already resolved)
        try {
            AzureVMMetadata metadata = CACHED_METADATA.block(Duration.ofSeconds(5));
            if (metadata != null && metadata != METADATA_NOT_AVAILABLE && metadata.getVmId() != null) {
                String machineId = VM_ID_PREFIX + metadata.getVmId();
                if (diagnosticsClientConfig != null) {
                    diagnosticsClientConfig.withMachineId(machineId);
                }
                return machineId;
            }
        } catch (Exception ignored) {
            // Timeout or error - fall through to default
        }

        if (diagnosticsClientConfig == null) {
            return "";
        }

        return diagnosticsClientConfig.getMachineId();
    }

    public boolean isClientMetricsEnabled() {
        return this.clientMetricsEnabled;
    }

    /**
     * Initialize this client telemetry instance by loading VM metadata (if not already cached).
     * The first call triggers an IMDS HTTP request; the HTTP client is disposed immediately
     * after the metadata is fetched. Subsequent calls just populate from the static cache.
     */
    public Mono<?> init() {
        return loadAzureVmMetaData(this);
    }

    public void close() {
        // Nothing to clean up -- no per-instance resources.
        // The IMDS HTTP client is created and disposed during init(), not held.
        // Per-instance state (clientTelemetryInfo) will be GC'd with this instance.
        logger.debug("ClientTelemetry closed.");
    }

    private void populateAzureVmMetaData(AzureVMMetadata azureVMMetadata) {
        this.clientTelemetryInfo.setApplicationRegion(azureVMMetadata.getLocation());
        this.clientTelemetryInfo.setMachineId(VM_ID_PREFIX + azureVMMetadata.getVmId());
        this.clientTelemetryInfo.setHostEnvInfo(azureVMMetadata.getOsType() + "|" + azureVMMetadata.getSku() +
            "|" + azureVMMetadata.getVmSize() + "|" + azureVMMetadata.getAzEnvironment());
    }

    /**
     * Creates the one-time IMDS fetch Mono. Called once to initialize CACHED_METADATA.
     * Reactor's cache() operator ensures this executes at most once; all concurrent
     * subscribers share the single result. The HTTP client is ephemeral.
     */
    private static Mono<AzureVMMetadata> fetchAzureVmMetadata() {
        if (Configs.shouldDisableIMDSAccess()) {
            logger.info("Access to IMDS to get Azure VM metadata is disabled");
            return Mono.just(METADATA_NOT_AVAILABLE);
        }

        URI targetEndpoint;
        try {
            targetEndpoint = new URI(IMDS_AZURE_VM_METADATA);
        } catch (URISyntaxException ex) {
            logger.info("Unable to parse azure vm metadata url");
            return Mono.just(METADATA_NOT_AVAILABLE);
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Metadata", "true");
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, targetEndpoint,
            targetEndpoint.getPort(), httpHeaders);

        HttpClientConfig httpClientConfig = new HttpClientConfig(new Configs())
            .withMaxIdleConnectionTimeout(IMDS_DEFAULT_IDLE_CONNECTION_TIMEOUT)
            .withPoolSize(IMDS_DEFAULT_MAX_CONNECTION_POOL_SIZE)
            .withNetworkRequestTimeout(IMDS_DEFAULT_NETWORK_REQUEST_TIMEOUT)
            .withConnectionAcquireTimeout(IMDS_DEFAULT_CONNECTION_ACQUIRE_TIMEOUT);

        return Mono.usingWhen(
            Mono.fromCallable(() -> HttpClient.createFixed(httpClientConfig)),
            httpClient -> httpClient.send(httpRequest)
                .flatMap(HttpResponse::bodyAsString)
                .map(ClientTelemetry::parse),
            httpClient -> Mono.fromRunnable(() -> {
                try { httpClient.shutdown(); } catch (Exception e) { /* ignore */ }
            })
        )
        .onErrorResume(throwable -> {
            logger.debug("Client is not on azure vm");
            logger.debug("Unable to get azure vm metadata", throwable);
            return Mono.just(METADATA_NOT_AVAILABLE);
        })
        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * Load metadata from cache and populate this instance's telemetry info.
     */
    private static Mono<?> loadAzureVmMetaData(ClientTelemetry thisPtr) {
        return CACHED_METADATA.doOnNext(metadata -> {
            if (thisPtr != null && metadata != METADATA_NOT_AVAILABLE) {
                thisPtr.populateAzureVmMetaData(metadata);
            }
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
