// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.SessionRetryOptions;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.circuitBreaker.PartitionLevelCircuitBreakerConfig;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.guava27.Strings;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface DiagnosticsClientContext {

    DiagnosticsClientConfig getConfig();

    CosmosDiagnostics createDiagnostics();

    String getUserAgent();

    CosmosDiagnostics getMostRecentlyCreatedDiagnostics();

    final class DiagnosticsClientConfigSerializer extends StdSerializer<DiagnosticsClientConfig> {
        private final static Logger logger = LoggerFactory.getLogger(DiagnosticsClientConfigSerializer.class);
        public final static DiagnosticsClientConfigSerializer INSTANCE = new DiagnosticsClientConfigSerializer();

        private static final Pattern SPACE_PATTERN = Pattern.compile(" ");

        private static final long serialVersionUID = 1;

        protected DiagnosticsClientConfigSerializer() {
            this(null);
        }

        protected DiagnosticsClientConfigSerializer(Class<DiagnosticsClientConfig> t) {
            super(t);
        }

        @Override
        public void serialize(DiagnosticsClientConfig clientConfig, JsonGenerator generator,
                              SerializerProvider serializerProvider) throws IOException {
            generator.writeStartObject();
            try {
                generator.writeNumberField("id", clientConfig.getClientId());
                generator.writeStringField("machineId", ClientTelemetry.getMachineId(clientConfig));
                generator.writeStringField("connectionMode", clientConfig.getConnectionMode().toString());
                generator.writeNumberField("numberOfClients", clientConfig.getActiveClientsCount());
                generator.writeStringField("excrgns", clientConfig.excludedRegionsRelatedConfig());
                generator.writeObjectFieldStart("clientEndpoints");
                for (Map.Entry<String, Integer> entry: clientConfig.clientMap.entrySet()) {
                    try {
                        generator.writeNumberField(entry.getKey(), entry.getValue());
                    } catch (Exception e) {
                        logger.debug("unexpected failure", e);
                    }
                }
                generator.writeEndObject();
                generator.writeObjectFieldStart("connCfg");
                try {
                    generator.writeStringField("rntbd", clientConfig.rntbdConfig());
                    generator.writeStringField("gw", clientConfig.gwConfig());
                    generator.writeStringField("other", clientConfig.otherConnectionConfig());
                } catch (Exception e) {
                    logger.debug("unexpected failure", e);
                }
                generator.writeEndObject();
                generator.writeStringField("consistencyCfg", clientConfig.consistencyRelatedConfig());
                generator.writeStringField("proactiveInitCfg", clientConfig.proactivelyInitializedContainersAsString);
                generator.writeStringField("e2ePolicyCfg", clientConfig.endToEndOperationLatencyPolicyConfigAsString);
                generator.writeStringField("sessionRetryCfg", clientConfig.sessionRetryOptionsAsString);

                if (!StringUtils.isEmpty(clientConfig.regionScopedSessionContainerOptionsAsString)) {
                    generator.writeStringField("regionScopedSessionCfg", clientConfig.regionScopedSessionContainerOptionsAsString);
                }

                if (!StringUtils.isEmpty(clientConfig.partitionLevelCircuitBreakerConfigAsString)) {
                    generator.writeStringField("partitionLevelCircuitBreakerCfg", clientConfig.partitionLevelCircuitBreakerConfigAsString);
                }

            } catch (Exception e) {
                logger.debug("unexpected failure", e);
            }
            generator.writeEndObject();
        }
    }

    @JsonSerialize(using = DiagnosticsClientContext.DiagnosticsClientConfigSerializer.class)
    class DiagnosticsClientConfig {

        private AtomicInteger activeClientsCnt;
        private int clientId;
        private Map<String, Integer> clientMap;

        private ConsistencyLevel consistencyLevel;
        private boolean connectionSharingAcrossClientsEnabled;
        private String consistencyRelatedConfigAsString;
        private String httpConfigAsString;
        private String otherCfgAsString;
        private String preferredRegionsAsString;
        private String proactivelyInitializedContainersAsString;

        private String endToEndOperationLatencyPolicyConfigAsString;
        private boolean endpointDiscoveryEnabled;
        private boolean multipleWriteRegionsEnabled;
        private String rntbdConfigAsString;
        private ConnectionMode connectionMode;
        private String machineId;
        private boolean replicaValidationEnabled = Configs.isReplicaAddressValidationEnabled();
        private ConnectionPolicy connectionPolicy;
        private String sessionRetryOptionsAsString;
        private String regionScopedSessionContainerOptionsAsString;
        private String partitionLevelCircuitBreakerConfigAsString;

        public DiagnosticsClientConfig withMachineId(String machineId) {
            this.machineId = machineId;
            return this;
        }

        public DiagnosticsClientConfig withActiveClientCounter(AtomicInteger activeClientsCnt) {
            this.activeClientsCnt = activeClientsCnt;
            return this;
        }

        public DiagnosticsClientConfig withClientId(int clientId) {
            this.clientId = clientId;
            return this;
        }

        public DiagnosticsClientConfig withClientMap(Map<String, Integer> clientMap) {
            this.clientMap = clientMap;
            return this;
        }

        public DiagnosticsClientConfig withEndpointDiscoveryEnabled(boolean endpointDiscoveryEnabled) {
            this.endpointDiscoveryEnabled = endpointDiscoveryEnabled;
            return this;
        }

        public DiagnosticsClientConfig withMultipleWriteRegionsEnabled(boolean multipleWriteRegionsEnabled) {
            this.multipleWriteRegionsEnabled = multipleWriteRegionsEnabled;
            return this;
        }

        public DiagnosticsClientConfig withPreferredRegions(List<String> preferredRegions) {
            if (preferredRegions == null || preferredRegions.isEmpty()) {
                this.preferredRegionsAsString = "";
            } else {
                this.preferredRegionsAsString = preferredRegions
                    .stream()
                    .map(r -> DiagnosticsClientConfigSerializer.SPACE_PATTERN.matcher(r.toLowerCase(Locale.ROOT)).replaceAll(""))
                    .collect(Collectors.joining(","));
            }
            return this;
        }

        public DiagnosticsClientConfig withConnectionPolicy(ConnectionPolicy connectionPolicy) {
            this.connectionPolicy = connectionPolicy;
            return this;
        }

        public DiagnosticsClientConfig withProactiveContainerInitConfig(
            CosmosContainerProactiveInitConfig config) {

            if (config == null) {
                this.proactivelyInitializedContainersAsString = "";
            } else {
                this.proactivelyInitializedContainersAsString = config.toString();
            }

            return this;
        }

        public DiagnosticsClientConfig withEndToEndOperationLatencyPolicy(
            CosmosEndToEndOperationLatencyPolicyConfig config) {

            if (config == null) {
                this.endToEndOperationLatencyPolicyConfigAsString = "";
            } else {
                this.endToEndOperationLatencyPolicyConfigAsString = config.toString();
            }

            return this;
        }

        public DiagnosticsClientConfig withConnectionSharingAcrossClientsEnabled(boolean connectionSharingAcrossClientsEnabled) {
            this.connectionSharingAcrossClientsEnabled = connectionSharingAcrossClientsEnabled;
            return this;
        }

        public DiagnosticsClientConfig withConsistency(ConsistencyLevel consistencyLevel) {
            this.consistencyLevel = consistencyLevel;
            return this;
        }

        public DiagnosticsClientConfig withRntbdOptions(String rntbdConfigAsString) {
            this.rntbdConfigAsString = rntbdConfigAsString;
            return this;
        }

        public DiagnosticsClientConfig withGatewayHttpClientConfig(String httpConfigAsString) {
            this.httpConfigAsString = httpConfigAsString;
            return this;
        }

        public DiagnosticsClientConfig withConnectionMode(ConnectionMode connectionMode) {
            this.connectionMode = connectionMode;
            return this;
        }

        public DiagnosticsClientConfig withSessionRetryOptions(SessionRetryOptions sessionRetryOptions) {
            if (sessionRetryOptions == null) {
                this.sessionRetryOptionsAsString = "";
            } else {
                this.sessionRetryOptionsAsString = sessionRetryOptions.toString();
            }

            return this;
        }

        public DiagnosticsClientConfig withPartitionLevelCircuitBreakerConfig(PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig) {
            if (partitionLevelCircuitBreakerConfig == null) {
                this.partitionLevelCircuitBreakerConfigAsString = "";
            } else {
                this.partitionLevelCircuitBreakerConfigAsString = partitionLevelCircuitBreakerConfig.getConfigAsString();
            }

            return this;
        }

        public DiagnosticsClientConfig withRegionScopedSessionContainerOptions(RegionScopedSessionContainer regionScopedSessionContainer) {

            if (regionScopedSessionContainer == null) {
                this.regionScopedSessionContainerOptionsAsString = "";
            } else {
                this.regionScopedSessionContainerOptionsAsString = regionScopedSessionContainer.getRegionScopedSessionCapturingOptionsAsString();
            }

            return this;
        }

        public ConnectionMode getConnectionMode() {
            return connectionMode;
        }

        public String consistencyRelatedConfig() {
            if (consistencyRelatedConfigAsString == null) {
                this.consistencyRelatedConfigAsString = this.consistencyRelatedConfigInternal();
            }

            return this.consistencyRelatedConfigAsString;
        }

        public String rntbdConfig() {
            return this.rntbdConfigAsString;
        }

        public String gwConfig() {
            return this.httpConfigAsString;
        }

        public String otherConnectionConfig() {
            if (this.otherCfgAsString == null) {
                this.otherCfgAsString = Strings.lenientFormat("(ed: %s, cs: %s, rv: %s)",
                    this.endpointDiscoveryEnabled,
                    this.connectionSharingAcrossClientsEnabled,
                    this.replicaValidationEnabled);
            }

            return this.otherCfgAsString;
        }

        public int getClientId() {
            return this.clientId;
        }

        public String getMachineId() { return this.machineId; }

        public int getActiveClientsCount() {
            return this.activeClientsCnt != null ? this.activeClientsCnt.get() : -1;
        }

        private String consistencyRelatedConfigInternal() {
            return Strings.lenientFormat("(consistency: %s, mm: %s, prgns: [%s])", this.consistencyLevel,
                this.multipleWriteRegionsEnabled,
                preferredRegionsAsString);
        }

        private String excludedRegionsRelatedConfig() {
            if (this.connectionPolicy == null) {
                return "[]";
            } else {
                return this.connectionPolicy.getExcludedRegionsAsString();
            }
        }
    }
}
