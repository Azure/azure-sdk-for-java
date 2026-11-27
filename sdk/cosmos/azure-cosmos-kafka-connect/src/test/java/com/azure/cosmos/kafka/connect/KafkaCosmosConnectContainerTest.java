// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.apache.kafka.connect.json.JsonConverter;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorStatus;
import org.testcontainers.utility.DockerImageName;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KafkaCosmosConnectContainerTest {
    private static final String STATUS = "{\"name\":\"test connector\",\"type\":\"source\","
        + "\"connector\":{\"state\":\"RUNNING\",\"worker_id\":\"connect:8083\"},"
        + "\"tasks\":[{\"id\":0,\"state\":\"RUNNING\",\"worker_id\":\"connect:8083\"}]}";

    @Test(groups = "unit")
    public void jsonConverterUsesSharedJacksonModules() throws Exception {
        try (JsonConverter converter = new JsonConverter()) {
            converter.configure(Map.of("schemas.enable", false), false);
            Map<String, String> value = Map.of("id", "example");
            byte[] encoded = converter.fromConnectData("topic", null, value);
            assertThat(converter.toConnectData("topic", encoded).value()).isEqualTo(value);
        }
    }

    @Test(groups = "unit")
    public void statusDeserializesSnakeCaseAndEncodesConnectorName() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> path = new AtomicReference<>();
        server.createContext("/connectors/", exchange -> {
            path.set(exchange.getRequestURI().getRawPath());
            byte[] body = STATUS.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            ConnectorStatus status = container(server).getConnectorStatus("test connector");
            assertThat(status.getTasks().get(0).getWorkerId()).isEqualTo("connect:8083");
            assertThat(status.getConnector().get("state")).isEqualTo("RUNNING");
            assertThat(path.get()).isEqualTo("/connectors/test%20connector/status");
        } finally {
            server.stop(0);
        }
    }

    @Test(groups = "unit")
    public void registerPreservesConnectorConfiguration() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> payload = new AtomicReference<>();
        server.createContext("/connectors", exchange -> {
            payload.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        });
        server.start();
        try {
            container(server).registerConnector("sink", Map.of("connector.class", "CosmosSinkConnector"));
            assertThat(new ObjectMapper().readTree(payload.get()).path("config").path("connector.class").asText())
                .isEqualTo("CosmosSinkConnector");
        } finally {
            server.stop(0);
        }
    }

    @Test(groups = "unit")
    public void statusRetriesTransientNotFound() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/connectors/", exchange -> {
            int status = requests.incrementAndGet() == 1 ? 404 : 200;
            byte[] body = STATUS.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            assertThat(container(server).getConnectorStatus("test connector").getName()).isEqualTo("test connector");
            assertThat(requests.get()).isEqualTo(2);
        } finally {
            server.stop(0);
        }
    }

    @Test(groups = "unit")
    public void deleteMissingConnectorSucceedsButPauseErrorsAreSurfaced() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/connectors/", exchange -> {
            exchange.sendResponseHeaders("DELETE".equals(exchange.getRequestMethod()) ? 404 : 500, -1);
            exchange.close();
        });
        server.start();
        try {
            KafkaCosmosConnectContainer container = container(server);
            container.deleteConnector("missing");
            assertThatThrownBy(() -> container.pauseConnector("missing")).hasMessageContaining("HTTP 500");
        } finally {
            server.stop(0);
        }
    }

    private static KafkaCosmosConnectContainer container(HttpServer server) {
        return new KafkaCosmosConnectContainer(DockerImageName.parse("confluentinc/cp-kafka-connect:8.2.5")) {
            @Override
            public String getTarget() {
                return "http://127.0.0.1:" + server.getAddress().getPort();
            }
        };
    }
}
