// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.guava27.Strings;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@JsonSerialize(using = DiagnosticsClientContext.ClientContextSerializer.class)
public interface DiagnosticsClientContext {

    DiagnosticsClientConfig getConfig();

    CosmosDiagnostics createDiagnostics();


    static final class ClientContextSerializer extends StdSerializer<DiagnosticsClientContext> {
        private final static Logger logger = LoggerFactory.getLogger(ClientContextSerializer.class);
        public final static ClientContextSerializer INSTACE = new ClientContextSerializer();

        private static final long serialVersionUID = 1;

        protected ClientContextSerializer() {
            this(null);
        }

        protected ClientContextSerializer(Class<DiagnosticsClientContext> t) {
            super(t);
        }

        @Override
        public void serialize(DiagnosticsClientContext clientContext, JsonGenerator generator,
                              SerializerProvider serializerProvider) throws IOException {
            generator.writeStartObject();
            try {
                generator.writeNumberField("id", clientContext.getConfig().getClientId());
                generator.writeStringField("connectionMode", clientContext.getConfig().getConnectionMode().toString());
                generator.writeNumberField("numberOfClients", clientContext.getConfig().getActiveClientsCount());
                generator.writeObjectFieldStart("connCfg");
                try {
                    generator.writeStringField("rntbd", clientContext.getConfig().rntbdConfig());
                    generator.writeStringField("gw", clientContext.getConfig().gwConfig());
                    generator.writeStringField("other", clientContext.getConfig().otherConnectionConfig());
                } catch (Exception e) {
                    logger.debug("unexpected failure", e);
                }
                generator.writeEndObject();
                generator.writeStringField("consistencyCfg", clientContext.getConfig().consistencyRelatedConfig());
            } catch (Exception e) {
                logger.debug("unexpected failure", e);
            }
            generator.writeEndObject();
        }
    }

    class DiagnosticsClientConfig {

        private AtomicInteger activeClientsCnt;
        private int clientId;

        private ConsistencyLevel consistencyLevel;
        private boolean connectionSharingAcrossClientsEnabled;
        private String consistencyRelatedConfigAsString;
        private String httpConfigAsString;
        private String otherCfgAsString;
        private List<String> preferredRegions;
        private boolean endpointDiscoveryEnabled;
        private boolean multipleWriteRegionsEnabled;

        private HttpClientConfig httpClientConfig;
        private RntbdTransportClient.Options options;
        private String rntbdConfigAsString;
        private ConnectionMode connectionMode;

        public void withActiveClientCounter(AtomicInteger activeClientsCnt) {
            this.activeClientsCnt = activeClientsCnt;
        }

        public void withClientId(int clientId) {
            this.clientId = clientId;
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
            this.preferredRegions = preferredRegions;
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

        public DiagnosticsClientConfig withRntbdOptions(RntbdTransportClient.Options options) {
            this.options = options;
            return this;
        }

        public DiagnosticsClientConfig withGatewayHttpClientConfig(HttpClientConfig httpClientConfig) {
            this.httpClientConfig = httpClientConfig;
            return this;
        }

        public DiagnosticsClientConfig withConnectionMode(ConnectionMode connectionMode) {
            this.connectionMode = connectionMode;
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
            if (this.rntbdConfigAsString == null) {
                this.rntbdConfigAsString = this.rntbdConfigInternal(this.options);
            }

            return this.rntbdConfigAsString;
        }

        public String gwConfig() {
            if (this.httpConfigAsString == null) {
                this.httpConfigAsString = this.gwConfigInternal();
            }

            return this.httpConfigAsString;
        }

        public String otherConnectionConfig() {
            if (this.otherCfgAsString == null) {
                this.otherCfgAsString = Strings.lenientFormat("(ed: %s, cs: %s)",
                    this.endpointDiscoveryEnabled,
                    this.connectionSharingAcrossClientsEnabled);
            }

            return this.otherCfgAsString;
        }

        public int getClientId() {
            return this.clientId;
        }

        public int getActiveClientsCount() {
            return this.activeClientsCnt != null ? this.activeClientsCnt.get() : -1;
        }

        private String gwConfigInternal() {
            if (this.httpClientConfig == null) {
                return null;
            }
            return Strings.lenientFormat("(cps:%s, rto:%s, icto:%s, p:%s)",
                this.httpClientConfig.getMaxPoolSize(),
                this.httpClientConfig.getRequestTimeout(),
                this.httpClientConfig.getMaxIdleConnectionTimeout(),
                this.httpClientConfig.getProxy() != null);
        }

        private String rntbdConfigInternal(RntbdTransportClient.Options rntbdOptions) {
            if (rntbdOptions == null) {
                return null;
            }
            return Strings.lenientFormat("(cto:%s, rto:%s, icto:%s, ieto:%s, mcpe:%s, mrpc:%s, cer:%s)",
                rntbdOptions.connectTimeout(),
                rntbdOptions.requestTimeout(),
                rntbdOptions.idleChannelTimeout(),
                rntbdOptions.idleEndpointTimeout(),
                rntbdOptions.maxChannelsPerEndpoint(),
                rntbdOptions.maxRequestsPerChannel(),
                rntbdOptions.isConnectionEndpointRediscoveryEnabled());
        }

        private String preferredRegionsInternal() {
            if (preferredRegions == null) {
                return "";
            }

            return preferredRegions.stream().map(r -> r.toLowerCase(Locale.ROOT).replaceAll(" ", "")).collect(Collectors.joining(","));
        }

        private String consistencyRelatedConfigInternal() {
            return Strings.lenientFormat("(consistency: %s, mm: %s, prgns: [%s])", this.consistencyLevel,
                this.multipleWriteRegionsEnabled,
                preferredRegionsInternal());
        }
    }
}
