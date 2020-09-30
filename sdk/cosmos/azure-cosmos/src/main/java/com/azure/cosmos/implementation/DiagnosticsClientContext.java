// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.guava27.Strings;
import com.azure.cosmos.implementation.http.HttpClientConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public interface DiagnosticsClientContext {

    DiagnosticsClientConfig getConfig();

    CosmosDiagnostics createDiagnostics();

    class DiagnosticsClientConfig {

        private AtomicInteger activeClientsCnt;
        private int clientId;

        private ConsistencyLevel consistencyLevel;
        private boolean connectionSharingAcrossClientsEnabled;
        private String consistencyRelatedConfigAsString;
        private boolean contentResponseOnWriteEnabled;
        private String httpConfigAsString;
        private String otherCfgAsString;
        private List<String> preferredRegions;
        private boolean endpointDiscoveryEnabled;
        private boolean multipleWriteRegionsEnabled;

        private HttpClientConfig httpClientConfig;
        private RntbdTransportClient.Options options;
        private String rntbdConfigAsString;

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
            return this.activeClientsCnt.get();
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
            return Strings.lenientFormat("(cto:%s, rto:%s, icto:%s, ieto:%s, mcpe:%s, mrpc:%s)",
                rntbdOptions.connectTimeout(),
                rntbdOptions.requestTimeout(),
                rntbdOptions.idleChannelTimeout(),
                rntbdOptions.idleEndpointTimeout(),
                rntbdOptions.maxChannelsPerEndpoint(),
                rntbdOptions.maxRequestsPerChannel());
        }

        private String preferredRegionsInternal() {
            if (preferredRegions == null) {
                return "null";
            }

            return preferredRegions.stream().map(rn -> {
                String[] parts = rn.toLowerCase(Locale.ROOT).split(" ");
                return Arrays.stream(parts).map(s -> String.valueOf(s.charAt(0))).collect(Collectors.joining());
            }).collect(Collectors.joining(","));
        }

        private String consistencyRelatedConfigInternal() {
            return Strings.lenientFormat("(consistency: %s, mm: %s, prgns: [%s])", this.consistencyLevel, this.multipleWriteRegionsEnabled,
                preferredRegionsInternal());
        }
    }
}
