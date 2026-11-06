// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.config;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import com.azure.spring.cloud.feature.management.FeatureManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigDataIntegrationTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.spring.cloud.config.SelectionTestData#scenarios")
    void appliesLabelAndSelectPriority(SelectionTestData.Scenario scenario) {
        TokenCredential credential = context ->
            Mono.just(new AccessToken("synthetic-test-token", OffsetDateTime.now().plusHours(1)));
        HttpClient transport = request -> {
            if (!"config.example".equals(request.getUrl().getHost()) || !"/kv".equals(request.getUrl().getPath())) {
                return Mono.error(new IllegalStateException("Unexpected HTTP request: " + request.getUrl()));
            }
            String keyFilter = queryParameter(request, "key");
            String label = queryParameter(request, "label");
            return Mono.just(response(request,
                BinaryData.fromObject(Map.of("items", SelectionTestData.settings(keyFilter, label)))));
        };
        SpringApplication application = application();
        application.addBootstrapRegistryInitializer(registry ->
            registry.register(ConfigurationClientCustomizer.class, context -> (builder, endpoint) ->
                builder.credential(credential).httpClient(transport)));

        try (ConfigurableApplicationContext context = application.run(scenario.arguments("https://config.example"))) {
            assertEquals(scenario.expectedEntries(), context.getBean(SelectionProperties.class).entries());
            assertEquals(scenario.expectedEntries().get("shared"),
                context.getEnvironment().getProperty("selection.entries.shared"));
        }
    }

    @Test
    void loads101KeysFollowingTheContinuationLink() {
        AtomicInteger requests = new AtomicInteger();
        List<Map<String, String>> settings = PaginationTestData.expectedEntries().entrySet().stream()
            .map(entry -> Map.of("key", PaginationTestData.KEY_PREFIX + entry.getKey(), "value", entry.getValue()))
            .toList();
        TokenCredential credential = context ->
            Mono.just(new AccessToken("synthetic-test-token", OffsetDateTime.now().plusHours(1)));
        HttpClient transport = request -> {
            String query = URLDecoder.decode(request.getUrl().getQuery(), StandardCharsets.UTF_8);
            if (!"config.example".equals(request.getUrl().getHost())
                || !"/kv".equals(request.getUrl().getPath())
                || !query.contains("key=" + PaginationTestData.KEY_PREFIX + "*")) {
                return Mono.error(new IllegalStateException("Unexpected HTTP request: " + request.getUrl()));
            }
            int page = requests.getAndIncrement();
            if (page > 1 || (page == 1) != query.contains("after=page2")) {
                return Mono.error(new IllegalStateException("Unexpected pagination request: " + request.getUrl()));
            }
            List<Map<String, String>> items = page == 0
                ? settings.subList(0, PaginationTestData.PAGE_SIZE)
                : settings.subList(PaginationTestData.PAGE_SIZE, settings.size());
            String nextLink = request.getUrl().toString() + "&after=page2";
            BinaryData body = BinaryData.fromObject(page == 0
                ? Map.of("items", items, "@nextLink", nextLink)
                : Map.of("items", items));
            HttpResponse response = response(request, body);
            if (page == 0) {
                response.getHeaders().set(HttpHeaderName.LINK,
                    "<" + request.getUrl().getFile() + "&after=page2>; rel=\"next\"");
            }
            return Mono.just(response);
        };
        SpringApplication application = application();
        application.addBootstrapRegistryInitializer(registry ->
            registry.register(ConfigurationClientCustomizer.class, context -> (builder, endpoint) ->
                builder.credential(credential).httpClient(transport).addPolicy(context.get(PaginationPagePolicy.class))));

        try (ConfigurableApplicationContext context = application.run(
            "--spring.config.import=azureAppConfiguration",
            "--spring.cloud.azure.appconfiguration.stores[0].endpoint=https://config.example",
            "--spring.cloud.azure.appconfiguration.stores[0].selects[0].key-filter=" + PaginationTestData.KEY_PREFIX,
            "--spring.cloud.azure.appconfiguration.stores[0].trim-key-prefix[0]=/application/",
            "--spring.cloud.azure.appconfiguration.stores[0].replica-discovery-enabled=false")) {
            assertEquals(2, requests.get());
            assertEquals(List.of(new PaginationPagePolicy.Page(100, true), new PaginationPagePolicy.Page(1, false)),
                context.getBean(PaginationPagePolicy.class).getPages());
            Map<String, String> entries = context.getBean(PaginationProperties.class).entries();
            assertEquals(PaginationTestData.KEY_COUNT, entries.size());
            assertEquals(PaginationTestData.expectedEntries(), entries);
        }
    }

    @Test
    void loadsPropertiesSecretsAndFeaturesThroughConfigData() {
        AtomicInteger configurationRequests = new AtomicInteger();
        AtomicInteger secretRequests = new AtomicInteger();
        TokenCredential credential = context ->
            Mono.just(new AccessToken("synthetic-test-token", OffsetDateTime.now().plusHours(1)));
        HttpClient transport = request -> {
            if ("config.example".equals(request.getUrl().getHost())) {
                configurationRequests.incrementAndGet();
                return Mono.just(response(request, settings()));
            }
            if ("test.vault.azure.net".equals(request.getUrl().getHost())) {
                secretRequests.incrementAndGet();
                return Mono.just(response(request, BinaryData.fromObject(Map.of(
                    "id", "https://test.vault.azure.net/secrets/TestSecret",
                    "value", "Synthetic secret"))));
            }
            return Mono.error(new IllegalStateException("Unexpected HTTP request: " + request.getUrl()));
        };

        SpringApplication application = application();
        application.addBootstrapRegistryInitializer(registry -> {
            registry.register(ConfigurationClientCustomizer.class, context -> (builder, endpoint) ->
                builder.credential(credential).httpClient(transport));
            registry.register(SecretClientCustomizer.class, context -> (builder, endpoint) ->
                builder.credential(credential).httpClient(transport));
        });

        try (ConfigurableApplicationContext context = application.run(
            "--spring.config.import=azureAppConfiguration",
            "--spring.cloud.azure.appconfiguration.stores[0].endpoint=https://config.example",
            "--spring.cloud.azure.appconfiguration.stores[0].replica-discovery-enabled=false",
            "--spring.cloud.azure.appconfiguration.stores[0].feature-flags.enabled=true")) {
            MessageProperties properties = context.getBean(MessageProperties.class);
            assertEquals("Test", properties.getMessage());
            assertEquals("Synthetic secret", properties.getSecret());
            assertEquals("Test", context.getEnvironment().getProperty("config.message"));
            FeatureManager featureManager = context.getBean(FeatureManager.class);
            assertTrue(featureManager.isEnabled("Alpha"));
            assertFalse(featureManager.isEnabled("Disabled"));
            assertTrue(configurationRequests.get() > 0, "The App Configuration customizer must supply the transport.");
            assertTrue(secretRequests.get() > 0, "The Key Vault customizer must supply the transport.");
        }
    }

    @Test
    void rejectsInvalidStartupTimeoutBeforeConnecting() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> application().run(
            "--spring.config.import=azureAppConfiguration",
            "--spring.cloud.azure.appconfiguration.stores[0].endpoint=https://config.example",
            "--spring.cloud.azure.appconfiguration.startup-timeout=1s"));
        assertTrue(exception.getMessage().contains("startupTimeout"));
    }

    private static SpringApplication application() {
        SpringApplication application = new SpringApplication(AppConfiguration.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        return application;
    }

    private static String queryParameter(HttpRequest request, String name) {
        return Arrays.stream(request.getUrl().getQuery().split("&"))
            .filter(parameter -> parameter.startsWith(name + "="))
            .map(parameter -> URLDecoder.decode(parameter.substring(name.length() + 1), StandardCharsets.UTF_8))
            .findFirst().orElseThrow(() -> new IllegalStateException("Missing query parameter: " + name));
    }

    private static BinaryData settings() {
        return BinaryData.fromObject(Map.of("items", List.of(
            Map.of("key", "/application/config.message", "value", "Test"),
            Map.of("key", "/application/config.secret",
                "value", "{\"uri\":\"https://test.vault.azure.net/secrets/TestSecret\"}",
                "content_type", "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8"),
            feature("Alpha", true),
            feature("Disabled", false))));
    }

    private static Map<String, Object> feature(String name, boolean enabled) {
        return Map.of("key", ".appconfig.featureflag/" + name,
            "content_type", "application/vnd.microsoft.appconfig.ff+json;charset=utf-8",
            "value", BinaryData.fromObject(Map.of("id", name, "enabled", enabled,
                "conditions", Map.of("client_filters", List.of()))).toString());
    }

    private static HttpResponse response(HttpRequest request, BinaryData body) {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        HttpResponse response = mock(HttpResponse.class);
        when(response.getRequest()).thenReturn(request);
        when(response.buffer()).thenCallRealMethod();
        when(response.getStatusCode()).thenReturn(200);
        when(response.getHeaders()).thenReturn(headers);
        when(response.getHeaderValue(any(HttpHeaderName.class)))
            .thenAnswer(invocation -> headers.getValue(invocation.getArgument(0, HttpHeaderName.class)));
        when(response.getBody()).thenAnswer(invocation -> Flux.just(ByteBuffer.wrap(body.toBytes())));
        when(response.getBodyAsBinaryData()).thenReturn(body);
        when(response.getBodyAsByteArray()).thenReturn(Mono.just(body.toBytes()));
        when(response.getBodyAsString()).thenReturn(Mono.just(body.toString()));
        when(response.getBodyAsString(any(Charset.class))).thenAnswer(invocation ->
            Mono.just(new String(body.toBytes(), invocation.getArgument(0, Charset.class))));
        return response;
    }
}
