// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.CosmosContainerProactiveInitConfigBuilder;
import com.azure.cosmos.CosmosExcludedRegions;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.CosmosRegionSwitchHint;
import com.azure.cosmos.SessionRetryOptions;
import com.azure.cosmos.SessionRetryOptionsBuilder;
import com.azure.cosmos.implementation.circuitBreaker.PartitionLevelCircuitBreakerConfig;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.StringWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientConfigDiagnosticsTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final ImplementationBridgeHelpers.CosmosContainerIdentityHelper.CosmosContainerIdentityAccessor containerIdentityAccessor = ImplementationBridgeHelpers
        .CosmosContainerIdentityHelper
        .getCosmosContainerIdentityAccessor();
    private static final ImplementationBridgeHelpers.CosmosSessionRetryOptionsHelper.CosmosSessionRetryOptionsAccessor sessionRetryOptionsAccessor = ImplementationBridgeHelpers
        .CosmosSessionRetryOptionsHelper
        .getCosmosSessionRetryOptionsAccessor();

    @DataProvider(name = "clientCfgProvider")
    public Object[][] clientCfgProvider() {

        Duration aggressiveWarmUpDuration1 = Duration.ofSeconds(1);
        int proactiveConnectionRegionCount1 = 1;

        Duration aggressiveWarmUpDuration2 = Duration.ofMillis(1000);
        int proactiveConnectionRegionCount2 = 1;

        Duration aggressiveWarmUpDuration3 = null;
        int proactiveConnectionRegionCount3 = 2;

        List<CosmosContainerIdentity> cosmosContainerIdentities = Arrays.asList(
            new CosmosContainerIdentity("test-db", "test-container-1"),
            new CosmosContainerIdentity("test-db", "test-container-2")
        );

        return new Object[][] {
            {
                new CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                    .setAggressiveWarmupDuration(aggressiveWarmUpDuration1)
                    .setProactiveConnectionRegionsCount(proactiveConnectionRegionCount1)
                    .build(),
                aggressiveWarmUpDuration1,
                proactiveConnectionRegionCount1,
                cosmosContainerIdentities,
                false, // is region scoped session capturing enabled
                false // is partition-level circuit breaking enabled
            },
            {
                new CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                    .setAggressiveWarmupDuration(aggressiveWarmUpDuration2)
                    .setProactiveConnectionRegionsCount(proactiveConnectionRegionCount2)
                    .build(),
                aggressiveWarmUpDuration2,
                proactiveConnectionRegionCount2,
                cosmosContainerIdentities,
                true,  // is region scoped session capturing enabled
                false // is partition-level circuit breaking enabled
            },
            {
                new CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                    .setProactiveConnectionRegionsCount(proactiveConnectionRegionCount3)
                    .build(),
                null,
                proactiveConnectionRegionCount3,
                cosmosContainerIdentities,
                false, // is region scoped session capturing enabled
                true // is partition-level circuit breaking enabled
            }
        };
    }

    @DataProvider(name = "sessionRetryOptionsConfigProvider")
    public Object[][] sessionRetryOptionsConfigProvider() {

        SessionRetryOptions sessionRetryOptionsWithLocalRegionPreferred = new SessionRetryOptionsBuilder()
            .regionSwitchHint(CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED)
            .build();

        SessionRetryOptions sessionRetryOptionsWithRemoteRegionPreferred = new SessionRetryOptionsBuilder()
            .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
            .build();

        SessionRetryOptions sessionRetryOptionsWithNoDefaults = new SessionRetryOptionsBuilder()
            .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
            .minTimeoutPerRegion(Duration.ofSeconds(2))
            .maxRetriesPerRegion(7)
            .build();

        return new Object[][] {
            {
                sessionRetryOptionsWithLocalRegionPreferred,
                reconstructSessionRetryOptionsAsString(
                    sessionRetryOptionsAccessor.getRegionSwitchHint(sessionRetryOptionsWithLocalRegionPreferred),
                    sessionRetryOptionsAccessor.getMinInRegionRetryTime(sessionRetryOptionsWithLocalRegionPreferred),
                    sessionRetryOptionsAccessor.getMaxInRegionRetryCount(sessionRetryOptionsWithLocalRegionPreferred))
            },
            {
                sessionRetryOptionsWithRemoteRegionPreferred,
                reconstructSessionRetryOptionsAsString(
                    sessionRetryOptionsAccessor.getRegionSwitchHint(sessionRetryOptionsWithRemoteRegionPreferred),
                    sessionRetryOptionsAccessor.getMinInRegionRetryTime(sessionRetryOptionsWithRemoteRegionPreferred),
                    sessionRetryOptionsAccessor.getMaxInRegionRetryCount(sessionRetryOptionsWithRemoteRegionPreferred))
            },
            {
                sessionRetryOptionsWithNoDefaults,
                reconstructSessionRetryOptionsAsString(
                    sessionRetryOptionsAccessor.getRegionSwitchHint(sessionRetryOptionsWithNoDefaults),
                    sessionRetryOptionsAccessor.getMinInRegionRetryTime(sessionRetryOptionsWithNoDefaults),
                    sessionRetryOptionsAccessor.getMaxInRegionRetryCount(sessionRetryOptionsWithNoDefaults))
            }
        };
    }

    @Test(groups = { "unit" })
    public void bareMinimum() throws Exception {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);

        DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig = new DiagnosticsClientContext.DiagnosticsClientConfig();
        String machineId = "vmId:" + UUID.randomUUID();
        diagnosticsClientConfig.withMachineId(machineId);
        diagnosticsClientConfig.withClientId(1);
        diagnosticsClientConfig.withConnectionMode(ConnectionMode.DIRECT);
        diagnosticsClientConfig.withActiveClientCounter(new AtomicInteger(2));
        diagnosticsClientConfig.withClientMap(new HashMap<>());

        Mockito.doReturn(diagnosticsClientConfig).when(clientContext).getConfig();

        StringWriter jsonWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        DiagnosticsClientContext.DiagnosticsClientConfigSerializer.INSTANCE.serialize(clientContext.getConfig(), jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertThat(objectNode.get("id").asInt()).isEqualTo(1);
        assertThat(objectNode.get("machineId").asText()).isEqualTo(machineId);
        assertThat(objectNode.get("numberOfClients").asInt()).isEqualTo(2);
        assertThat(objectNode.get("consistencyCfg").asText()).isEqualTo("(consistency: null, mm: false, prgns: [null])");
        assertThat(objectNode.get("connCfg").get("rntbd").asText()).isEqualTo("null");
        assertThat(objectNode.get("connCfg").get("gw").asText()).isEqualTo("null");
        assertThat(objectNode.get("connCfg").get("other").asText()).isEqualTo("(ed: false, cs: false, rv: true)");

    }

    @Test(groups = { "unit" })
    public void rntbd() throws Exception {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);

        DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig = new DiagnosticsClientContext.DiagnosticsClientConfig();
        String machineId = "vmId:" + UUID.randomUUID();
        diagnosticsClientConfig.withMachineId(machineId);
        diagnosticsClientConfig.withClientId(1);
        diagnosticsClientConfig.withConnectionMode(ConnectionMode.DIRECT);
        diagnosticsClientConfig.withActiveClientCounter(new AtomicInteger(2));
        diagnosticsClientConfig.withRntbdOptions( new RntbdTransportClient.Options.Builder(ConnectionPolicy.getDefaultPolicy()).build().toDiagnosticsString());
        diagnosticsClientConfig.withGatewayHttpClientConfig(new HttpClientConfig(new Configs()).toDiagnosticsString());
        diagnosticsClientConfig.withClientMap(new HashMap<>());

        Mockito.doReturn(diagnosticsClientConfig).when(clientContext).getConfig();

        StringWriter jsonWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        DiagnosticsClientContext.DiagnosticsClientConfigSerializer.INSTANCE.serialize(clientContext.getConfig(), jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertThat(objectNode.get("id").asInt()).isEqualTo(1);
        assertThat(objectNode.get("machineId").asText()).isEqualTo(machineId);
        assertThat(objectNode.get("numberOfClients").asInt()).isEqualTo(2);
        assertThat(objectNode.get("consistencyCfg").asText()).isEqualTo("(consistency: null, mm: false, prgns: [null])");
        assertThat(objectNode.get("connCfg").get("rntbd").asText()).isEqualTo("(cto:PT5S, nrto:PT5S, icto:PT0S, ieto:PT1H, mcpe:130, mrpc:30, cer:true)");
        assertThat(objectNode.get("connCfg").get("gw").asText()).isEqualTo("(cps:null, nrto:null, icto:null, p:false)");
        assertThat(objectNode.get("connCfg").get("other").asText()).isEqualTo("(ed: false, cs: false, rv: true)");
    }

    @Test(groups = { "unit" })
    public void gw() throws Exception {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);

        DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig = new DiagnosticsClientContext.DiagnosticsClientConfig();
        String machineId = "vmId:" + UUID.randomUUID();
        diagnosticsClientConfig.withMachineId(machineId);
        diagnosticsClientConfig.withClientId(1);
        diagnosticsClientConfig.withConnectionMode(ConnectionMode.DIRECT);
        diagnosticsClientConfig.withActiveClientCounter(new AtomicInteger(2));
        HttpClientConfig httpConfig = new HttpClientConfig(new Configs());
        httpConfig.withPoolSize(500);
        httpConfig.withMaxIdleConnectionTimeout(Duration.ofSeconds(17));
        httpConfig.withNetworkRequestTimeout(Duration.ofSeconds(18));
        diagnosticsClientConfig.withGatewayHttpClientConfig(httpConfig.toDiagnosticsString());
        diagnosticsClientConfig.withClientMap(new HashMap<>());

        Mockito.doReturn(diagnosticsClientConfig).when(clientContext).getConfig();

        StringWriter jsonWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        DiagnosticsClientContext.DiagnosticsClientConfigSerializer.INSTANCE.serialize(clientContext.getConfig(), jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertThat(objectNode.get("id").asInt()).isEqualTo(1);
        assertThat(objectNode.get("machineId").asText()).isEqualTo(machineId);
        assertThat(objectNode.get("numberOfClients").asInt()).isEqualTo(2);
        assertThat(objectNode.get("consistencyCfg").asText()).isEqualTo("(consistency: null, mm: false, prgns: [null])");
        assertThat(objectNode.get("connCfg").get("rntbd").asText()).isEqualTo("null");
        assertThat(objectNode.get("connCfg").get("gw").asText()).isEqualTo("(cps:500, nrto:PT18S, icto:PT17S, p:false)");
        assertThat(objectNode.get("connCfg").get("other").asText()).isEqualTo("(ed: false, cs: false, rv: true)");
    }

    @Test(groups = { "unit" }, dataProvider = "clientCfgProvider")
    public void full(
        CosmosContainerProactiveInitConfig containerProactiveInitConfig,
        Duration aggressiveWarmupDuration,
        int proactiveConnectionRegionCount,
        List<CosmosContainerIdentity> cosmosContainerIdentities,
        boolean isRegionScopedSessionCapturingEnabled,
        boolean isPartitionLevelCircuitBreakerEnabled) throws Exception {

        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);
        System.setProperty("COSMOS.REPLICA_ADDRESS_VALIDATION_ENABLED", "false");
        AtomicReference<CosmosExcludedRegions> cosmosExcludedRegionsAtomicReference = new AtomicReference<>(
            new CosmosExcludedRegions(new HashSet<>(Arrays.asList("west us 2"))));
        Supplier<CosmosExcludedRegions> excludedRegionsSupplier = () -> cosmosExcludedRegionsAtomicReference.get();

        DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig = new DiagnosticsClientContext.DiagnosticsClientConfig();
        String machineId = "vmId:" + UUID.randomUUID();
        diagnosticsClientConfig.withMachineId(machineId);
        diagnosticsClientConfig.withClientId(1);
        diagnosticsClientConfig.withConnectionMode(ConnectionMode.DIRECT);
        diagnosticsClientConfig.withActiveClientCounter(new AtomicInteger(2));
        HttpClientConfig httpConfig = new HttpClientConfig(new Configs());
        httpConfig.withPoolSize(500);
        httpConfig.withMaxIdleConnectionTimeout(Duration.ofSeconds(17));
        httpConfig.withNetworkRequestTimeout(Duration.ofSeconds(18));
        diagnosticsClientConfig.withGatewayHttpClientConfig(httpConfig.toDiagnosticsString());
        diagnosticsClientConfig.withPreferredRegions(ImmutableList.of("west us 1", "west us 2"));
        diagnosticsClientConfig.withConnectionPolicy(
            new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig()).setExcludedRegionsSupplier(excludedRegionsSupplier));
        diagnosticsClientConfig.withConnectionSharingAcrossClientsEnabled(true);
        diagnosticsClientConfig.withEndpointDiscoveryEnabled(true);
        diagnosticsClientConfig.withClientMap(new HashMap<>());
        diagnosticsClientConfig.withProactiveContainerInitConfig(containerProactiveInitConfig);

        RegionScopedSessionContainer regionScopedSessionContainer = null;

        if (isRegionScopedSessionCapturingEnabled) {
            regionScopedSessionContainer = new RegionScopedSessionContainer("127.0.0.1");
            diagnosticsClientConfig.withRegionScopedSessionContainerOptions(regionScopedSessionContainer);
        }

        if (isPartitionLevelCircuitBreakerEnabled) {
            System.setProperty(
                "COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG",
                "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                    + "\"circuitBreakerType\": \"CONSECUTIVE_EXCEPTION_COUNT_BASED\","
                    + "\"consecutiveExceptionCountToleratedForReads\": 10,"
                    + "\"consecutiveExceptionCountToleratedForWrites\": 5,"
                    + "}");

            PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig = Configs.getPartitionLevelCircuitBreakerConfig();
            diagnosticsClientConfig.withPartitionLevelCircuitBreakerConfig(partitionLevelCircuitBreakerConfig);
        }

        Mockito.doReturn(diagnosticsClientConfig).when(clientContext).getConfig();

        StringWriter jsonWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        DiagnosticsClientContext.DiagnosticsClientConfigSerializer.INSTANCE.serialize(clientContext.getConfig(), jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertThat(objectNode.get("id").asInt()).isEqualTo(1);
        assertThat(objectNode.get("machineId").asText()).isEqualTo(machineId);
        assertThat(objectNode.get("numberOfClients").asInt()).isEqualTo(2);
        assertThat(objectNode.get("consistencyCfg").asText()).isEqualTo("(consistency: null, mm: false, prgns: [westus1,westus2])");
        assertThat(objectNode.get("connCfg").get("rntbd").asText()).isEqualTo("null");
        assertThat(objectNode.get("connCfg").get("gw").asText()).isEqualTo("(cps:500, nrto:PT18S, icto:PT17S, p:false)");
        assertThat(objectNode.get("connCfg").get("other").asText()).isEqualTo("(ed: true, cs: true, rv: false)");
        assertThat(objectNode.get("excrgns").asText()).isEqualTo("[westus2]");

        if (isRegionScopedSessionCapturingEnabled) {
            assertThat(objectNode.get("regionScopedSessionCfg").asText()).isEqualTo(regionScopedSessionContainer.getRegionScopedSessionCapturingOptionsAsString());
        } else {
            assertThat(objectNode.get("regionScopedSessionCfg")).isNull();
        }

        if (isPartitionLevelCircuitBreakerEnabled) {
            assertThat(objectNode.get("partitionLevelCircuitBreakerCfg").asText()).isEqualTo("(cb: true, type: CONSECUTIVE_EXCEPTION_COUNT_BASED, rexcntt: 10, wexcntt: 5)");
        } else {
            assertThat(objectNode.get("partitionLevelCircuitBreakerCfg")).isNull();
        }

        String expectedProactiveInitConfigString = reconstructProactiveInitConfigString(cosmosContainerIdentities, aggressiveWarmupDuration, proactiveConnectionRegionCount);

        assertThat(objectNode.get("proactiveInitCfg").asText()).isEqualTo(expectedProactiveInitConfigString);

        System.clearProperty("COSMOS.REPLICA_ADDRESS_VALIDATION_ENABLED");
        System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
    }

    @Test(groups = {"unit"}, dataProvider = "sessionRetryOptionsConfigProvider")
    public void sessionRetryOptionsInDiagnostics(SessionRetryOptions sessionRetryOptions, String expectedSessionRetryOptionsAsString) throws Exception {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);

        DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig = new DiagnosticsClientContext.DiagnosticsClientConfig();
        String machineId = "vmId:" + UUID.randomUUID();
        diagnosticsClientConfig.withMachineId(machineId);
        diagnosticsClientConfig.withClientId(1);
        diagnosticsClientConfig.withConnectionMode(ConnectionMode.DIRECT);
        diagnosticsClientConfig.withActiveClientCounter(new AtomicInteger(2));
        diagnosticsClientConfig.withClientMap(new HashMap<>());
        diagnosticsClientConfig.withSessionRetryOptions(sessionRetryOptions);

        Mockito.doReturn(diagnosticsClientConfig).when(clientContext).getConfig();

        StringWriter jsonWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        DiagnosticsClientContext.DiagnosticsClientConfigSerializer.INSTANCE.serialize(clientContext.getConfig(), jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertThat(objectNode.get("id").asInt()).isEqualTo(1);
        assertThat(objectNode.get("machineId").asText()).isEqualTo(machineId);
        assertThat(objectNode.get("numberOfClients").asInt()).isEqualTo(2);
        assertThat(objectNode.get("consistencyCfg").asText()).isEqualTo("(consistency: null, mm: false, prgns: [null])");
        assertThat(objectNode.get("connCfg").get("rntbd").asText()).isEqualTo("null");
        assertThat(objectNode.get("connCfg").get("gw").asText()).isEqualTo("null");
        assertThat(objectNode.get("connCfg").get("other").asText()).isEqualTo("(ed: false, cs: false, rv: true)");
        assertThat(objectNode.get("sessionRetryCfg").asText()).isEqualTo(expectedSessionRetryOptionsAsString);
    }

    private static String reconstructProactiveInitConfigString(
        List<CosmosContainerIdentity> containerIdentities,
        Duration aggressiveWarmupDuration,
        int proactiveConnectionRegionCount) {

        return String.format(
            "(containers:%s)(pcrc:%d)(awd:%s)",
            containerIdentities
                .stream()
                .map(ci -> String.join(
                    ".",
                    containerIdentityAccessor.getContainerLink(ci)))
                .collect(Collectors.joining(";")),
            proactiveConnectionRegionCount,
            aggressiveWarmupDuration);
    }

    private static String reconstructSessionRetryOptionsAsString(CosmosRegionSwitchHint regionSwitchHint, Duration minInRegionRetryTime, int maxInRegionRetryCount) {
        return String.format(
            "(rsh:%s, minrrt:%s, maxrrc:%s)",
            regionSwitchHint == CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED ? "REMOTE_REGION_PREFERRED" : "LOCAL_REGION_PREFERRED",
            minInRegionRetryTime.toString(),
            maxInRegionRetryCount
        );
    }
}
