// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ServiceBusSharedKeyCredential;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceBusAdministrationClientBuilderTest {
    private static final String KEY_NAME = "test-policy";
    private static final String KEY = "test-key";
    private static final String CONNECTION_STRING
        = "Endpoint=sb://localhost:5300/;SharedAccessKeyName=" + KEY_NAME + ";SharedAccessKey=" + KEY;

    @ParameterizedTest
    @MethodSource("endpoints")
    void preservesEndpoint(boolean synchronous, String configuration, String expectedEndpoint) throws Exception {
        AtomicReference<HttpRequest> observed = new AtomicReference<>();
        ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .httpClient(recordingClient(observed));
        if (configuration.startsWith("Endpoint=")) {
            builder.connectionString(configuration);
        } else {
            builder.endpoint(configuration).credential(new ServiceBusSharedKeyCredential(KEY_NAME, KEY));
        }

        assertQueueAbsent(builder, synchronous);

        HttpRequest request = observed.get();
        assertNotNull(request);
        assertEquals(expectedEndpoint + "/queue", request.getUrl().toString().split("\\?")[0]);
        String authorization = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertNotNull(authorization);
        assertTrue(authorization.startsWith("SharedAccessSignature "));
        String resource = authorization.substring(authorization.indexOf("sr=") + 3, authorization.indexOf("&sig="));
        assertEquals(expectedEndpoint, URLDecoder.decode(resource, StandardCharsets.UTF_8.name()));
        String expiry = authorization.substring(authorization.indexOf("&se=") + 4, authorization.indexOf("&skn="));
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = URLEncoder.encode(
            Base64.getEncoder().encodeToString(hmac.doFinal((resource + "\n" + expiry).getBytes(StandardCharsets.UTF_8))),
            StandardCharsets.UTF_8.name());
        assertTrue(authorization.contains("&sig=" + signature + "&se="));
        assertTrue(authorization.endsWith("&skn=" + KEY_NAME));
    }

    private static Stream<Arguments> endpoints() {
        return Stream.of(true, false)
            .flatMap(sync -> Stream.of(
                Arguments.of(sync, CONNECTION_STRING + ";UseDevelopmentEmulator=true", "http://localhost:5300"),
                Arguments.of(sync, CONNECTION_STRING + ";UseDevelopmentEmulator=false", "https://localhost:5300"),
                Arguments.of(sync, CONNECTION_STRING, "https://localhost:5300"),
                Arguments.of(sync, CONNECTION_STRING.replace("sb://", "http://"), "https://localhost:5300"),
                Arguments.of(sync, "http://localhost:5300/", "http://localhost:5300"),
                Arguments.of(sync, "http://localhost", "http://localhost"),
                Arguments.of(sync, "https://localhost:5300/", "https://localhost:5300"),
                Arguments.of(sync, "https://namespace.servicebus.windows.net",
                    "https://namespace.servicebus.windows.net")));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void lastEndpointConfigurationWins(boolean synchronous) {
        AtomicReference<HttpRequest> observed = new AtomicReference<>();
        ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .connectionString(CONNECTION_STRING + ";UseDevelopmentEmulator=true")
            .endpoint("https://other.servicebus.windows.net:8443")
            .httpClient(recordingClient(observed));
        assertQueueAbsent(builder, synchronous);
        assertEquals("https://other.servicebus.windows.net:8443/queue",
            observed.get().getUrl().toString().split("\\?")[0]);

        builder.connectionString(CONNECTION_STRING + ";UseDevelopmentEmulator=true");
        assertQueueAbsent(builder, synchronous);
        assertEquals("http://localhost:5300/queue", observed.get().getUrl().toString().split("\\?")[0]);

        builder.credential("namespace.servicebus.windows.net", new ServiceBusSharedKeyCredential(KEY_NAME, KEY));
        assertQueueAbsent(builder, synchronous);
        assertEquals("https://namespace.servicebus.windows.net/queue",
            observed.get().getUrl().toString().split("\\?")[0]);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void rejectsHttpBearerCredentialsBeforeAcquiringToken(boolean synchronous) {
        AtomicBoolean requested = new AtomicBoolean();
        TokenCredential credential = context -> {
            requested.set(true);
            return Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        };
        ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .endpoint("http://localhost:5300")
            .credential(credential);

        assertThrows(IllegalArgumentException.class, () -> {
            if (synchronous) {
                builder.buildClient();
            } else {
                builder.buildAsyncClient();
            }
        });
        assertFalse(requested.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void preservesHttpsTokenAuthentication(boolean synchronous) {
        AtomicReference<HttpRequest> observed = new AtomicReference<>();
        AtomicReference<String> scope = new AtomicReference<>();
        TokenCredential credential = context -> {
            scope.set(context.getScopes().get(0));
            return Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        };
        ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .credential("namespace.servicebus.windows.net", credential)
            .httpClient(recordingClient(observed));

        assertQueueAbsent(builder, synchronous);
        assertEquals("https://servicebus.azure.net/.default", scope.get());
        assertEquals("test-token", observed.get().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        assertEquals("https", observed.get().getUrl().getProtocol());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void preservesExplicitSasCredential(boolean synchronous) {
        AtomicReference<HttpRequest> observed = new AtomicReference<>();
        String sas = "SharedAccessSignature sr=http%3A%2F%2Flocalhost%3A5300&sig=test&se=9999999999&skn=test-policy";
        ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .endpoint("http://localhost:5300")
            .credential(new AzureSasCredential(sas))
            .httpClient(recordingClient(observed));

        assertQueueAbsent(builder, synchronous);
        assertEquals(sas, observed.get().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void customPipelineRetainsPrecedence(boolean synchronous) {
        AtomicReference<HttpRequest> observed = new AtomicReference<>();
        TokenCredential ignored = context -> Mono.error(new AssertionError("Builder credential must be ignored."));
        ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .endpoint("http://localhost:5300")
            .credential(ignored)
            .pipeline(new HttpPipelineBuilder().httpClient(recordingClient(observed)).build());

        assertQueueAbsent(builder, synchronous);
        assertEquals("http", observed.get().getUrl().getProtocol());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void allAdministrationServicesUseConfiguredEndpoint(boolean synchronous) {
        AtomicReference<HttpRequest> observed = new AtomicReference<>();
        ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .connectionString(CONNECTION_STRING + ";UseDevelopmentEmulator=true")
            .httpClient(recordingClient(observed));
        ServiceBusAdministrationClient sync = builder.buildClient();
        ServiceBusAdministrationAsyncClient async = builder.buildAsyncClient();
        List<Runnable> operations = synchronous
            ? Arrays.asList(() -> sync.getTopic("topic"), () -> sync.getSubscription("topic", "subscription"),
                () -> sync.getRule("topic", "subscription", "rule"), () -> sync.getNamespaceProperties())
            : Arrays.asList(() -> async.getTopic("topic").block(Duration.ofSeconds(5)),
                () -> async.getSubscription("topic", "subscription").block(Duration.ofSeconds(5)),
                () -> async.getRule("topic", "subscription", "rule").block(Duration.ofSeconds(5)),
                () -> async.getNamespaceProperties().block(Duration.ofSeconds(5)));
        List<String> paths = Arrays.asList("/topic", "/topic/subscriptions/subscription",
            "/topic/subscriptions/subscription/rules/rule", "/$namespaceinfo");
        for (int i = 0; i < operations.size(); i++) {
            assertThrows(HttpResponseException.class, operations.get(i)::run);
            assertEquals("http://localhost:5300" + paths.get(i),
                observed.get().getUrl().toString().split("\\?")[0]);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "namespace.servicebus.windows.net,https://namespace.servicebus.windows.net/target",
        "https://namespace.servicebus.windows.net,https://namespace.servicebus.windows.net/target",
        "http://localhost:5300,http://localhost:5300/target"
    })
    void forwardingUsesEndpointAuthority(String endpoint, String expected) {
        AdministrationModelConverter converter
            = new AdministrationModelConverter(new ClientLogger(ServiceBusAdministrationClientBuilderTest.class),
                endpoint);
        assertEquals(expected, converter.getAbsoluteUrlFromEntity("target"));
        assertEquals("https://other.servicebus.windows.net/target",
            converter.getAbsoluteUrlFromEntity("https://other.servicebus.windows.net/target"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "ftp://localhost:5300", "http:///queue", "not-a-url" })
    void rejectsInvalidEndpoint(String endpoint) {
        assertThrows(IllegalArgumentException.class,
            () -> new ServiceBusAdministrationClientBuilder().endpoint(endpoint));
    }

    private static HttpClient recordingClient(AtomicReference<HttpRequest> observed) {
        return request -> {
            observed.set(request);
            return Mono.just(new MockHttpResponse(request, 404));
        };
    }

    private static void assertQueueAbsent(ServiceBusAdministrationClientBuilder builder, boolean synchronous) {
        if (synchronous) {
            assertFalse(builder.buildClient().getQueueExists("queue"));
        } else {
            assertEquals(Boolean.FALSE, builder.buildAsyncClient().getQueueExists("queue").block(Duration.ofSeconds(5)));
        }
    }
}
