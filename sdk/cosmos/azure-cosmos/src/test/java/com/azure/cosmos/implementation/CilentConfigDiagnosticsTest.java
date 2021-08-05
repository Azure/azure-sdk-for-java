// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.io.StringWriter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class CilentConfigDiagnosticsTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test(groups = { "unit" })
    public void bareMinimum() throws Exception {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);

        DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig = new DiagnosticsClientContext.DiagnosticsClientConfig();
        diagnosticsClientConfig.withClientId(1);
        diagnosticsClientConfig.withConnectionMode(ConnectionMode.DIRECT);
        diagnosticsClientConfig.withActiveClientCounter(new AtomicInteger(2));

        Mockito.doReturn(diagnosticsClientConfig).when(clientContext).getConfig();

        StringWriter jsonWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        DiagnosticsClientContext.ClientContextSerializer.INSTACE.serialize(clientContext, jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertThat(objectNode.get("id").asInt()).isEqualTo(1);
        assertThat(objectNode.get("numberOfClients").asInt()).isEqualTo(2);
        assertThat(objectNode.get("consistencyCfg").asText()).isEqualTo("(consistency: null, mm: false, prgns: [])");
        assertThat(objectNode.get("connCfg").get("rntbd").asText()).isEqualTo("null");
        assertThat(objectNode.get("connCfg").get("gw").asText()).isEqualTo("null");
        assertThat(objectNode.get("connCfg").get("other").asText()).isEqualTo("(ed: false, cs: false)");

    }

    @Test(groups = { "unit" })
    public void rntbd() throws Exception {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);

        DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig = new DiagnosticsClientContext.DiagnosticsClientConfig();
        diagnosticsClientConfig.withClientId(1);
        diagnosticsClientConfig.withConnectionMode(ConnectionMode.DIRECT);
        diagnosticsClientConfig.withActiveClientCounter(new AtomicInteger(2));
        diagnosticsClientConfig.withRntbdOptions( new RntbdTransportClient.Options.Builder(ConnectionPolicy.getDefaultPolicy()).build());
        diagnosticsClientConfig.withGatewayHttpClientConfig(new HttpClientConfig(new Configs()));

        Mockito.doReturn(diagnosticsClientConfig).when(clientContext).getConfig();

        StringWriter jsonWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        DiagnosticsClientContext.ClientContextSerializer.INSTACE.serialize(clientContext, jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertThat(objectNode.get("id").asInt()).isEqualTo(1);
        assertThat(objectNode.get("numberOfClients").asInt()).isEqualTo(2);
        assertThat(objectNode.get("consistencyCfg").asText()).isEqualTo("(consistency: null, mm: false, prgns: [])");
        assertThat(objectNode.get("connCfg").get("rntbd").asText()).isEqualTo("(cto:PT5S, rto:PT5S, icto:PT0S, ieto:PT1H, mcpe:130, mrpc:30, cer:false)");
        assertThat(objectNode.get("connCfg").get("gw").asText()).isEqualTo("(cps:null, rto:null, icto:null, p:false)");
        assertThat(objectNode.get("connCfg").get("other").asText()).isEqualTo("(ed: false, cs: false)");

    }

    @Test(groups = { "unit" })
    public void gw() throws Exception {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);

        DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig = new DiagnosticsClientContext.DiagnosticsClientConfig();
        diagnosticsClientConfig.withClientId(1);
        diagnosticsClientConfig.withConnectionMode(ConnectionMode.DIRECT);
        diagnosticsClientConfig.withActiveClientCounter(new AtomicInteger(2));
        HttpClientConfig httpConfig = new HttpClientConfig(new Configs());
        httpConfig.withPoolSize(500);
        httpConfig.withMaxIdleConnectionTimeout(Duration.ofSeconds(17));
        httpConfig.withRequestTimeout(Duration.ofSeconds(18));
        diagnosticsClientConfig.withGatewayHttpClientConfig(httpConfig);

        Mockito.doReturn(diagnosticsClientConfig).when(clientContext).getConfig();

        StringWriter jsonWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        DiagnosticsClientContext.ClientContextSerializer.INSTACE.serialize(clientContext, jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertThat(objectNode.get("id").asInt()).isEqualTo(1);
        assertThat(objectNode.get("numberOfClients").asInt()).isEqualTo(2);
        assertThat(objectNode.get("consistencyCfg").asText()).isEqualTo("(consistency: null, mm: false, prgns: [])");
        assertThat(objectNode.get("connCfg").get("rntbd").asText()).isEqualTo("null");
        assertThat(objectNode.get("connCfg").get("gw").asText()).isEqualTo("(cps:500, rto:PT18S, icto:PT17S, p:false)");
        assertThat(objectNode.get("connCfg").get("other").asText()).isEqualTo("(ed: false, cs: false)");
    }

    @Test(groups = { "unit" })
    public void full() throws Exception {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);

        DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig = new DiagnosticsClientContext.DiagnosticsClientConfig();
        diagnosticsClientConfig.withClientId(1);
        diagnosticsClientConfig.withConnectionMode(ConnectionMode.DIRECT);
        diagnosticsClientConfig.withActiveClientCounter(new AtomicInteger(2));
        HttpClientConfig httpConfig = new HttpClientConfig(new Configs());
        httpConfig.withPoolSize(500);
        httpConfig.withMaxIdleConnectionTimeout(Duration.ofSeconds(17));
        httpConfig.withRequestTimeout(Duration.ofSeconds(18));
        diagnosticsClientConfig.withGatewayHttpClientConfig(httpConfig);
        diagnosticsClientConfig.withPreferredRegions(ImmutableList.of("west us 1", "west us 2"));
        diagnosticsClientConfig.withConnectionSharingAcrossClientsEnabled(true);
        diagnosticsClientConfig.withEndpointDiscoveryEnabled(true);

        Mockito.doReturn(diagnosticsClientConfig).when(clientContext).getConfig();

        StringWriter jsonWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        DiagnosticsClientContext.ClientContextSerializer.INSTACE.serialize(clientContext, jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertThat(objectNode.get("id").asInt()).isEqualTo(1);
        assertThat(objectNode.get("numberOfClients").asInt()).isEqualTo(2);
        assertThat(objectNode.get("consistencyCfg").asText()).isEqualTo("(consistency: null, mm: false, prgns: [westus1,westus2])");
        assertThat(objectNode.get("connCfg").get("rntbd").asText()).isEqualTo("null");
        assertThat(objectNode.get("connCfg").get("gw").asText()).isEqualTo("(cps:500, rto:PT18S, icto:PT17S, p:false)");
        assertThat(objectNode.get("connCfg").get("other").asText()).isEqualTo("(ed: true, cs: true)");
    }
}
