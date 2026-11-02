// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.validation.http.HttpProtocolVersionTests;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.PemTrustOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Tests HTTP-version negotiation with Vert.x.
 */
public class VertxHttpClientHttp2Tests extends HttpProtocolVersionTests {
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void supportsCleartextUpgradeAndPriorKnowledge(boolean upgrade) throws Exception {
        Vertx vertx = Vertx.vertx();
        try {
            HttpServer server = vertx.createHttpServer()
                .requestHandler(request -> request.response().end(request.version().name()))
                .listen(0, "localhost")
                .toCompletionStage()
                .toCompletableFuture()
                .get(30, TimeUnit.SECONDS);
            HttpClientOptions options = new HttpClientOptions().setHttp2ClearTextUpgrade(upgrade);
            VertxHttpClient client = (VertxHttpClient) new VertxHttpClientBuilder().vertx(vertx)
                .httpClientOptions(options)
                .maximumHttpVersion(HttpProtocolVersion.HTTP_2)
                .build();
            assertNotSame(options, client.buildOptions);
            assertEquals(upgrade, client.buildOptions.isHttp2ClearTextUpgrade());
            HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost:" + server.actualPort());
            try (HttpResponse response = client.sendSync(request, Context.NONE)) {
                assertEquals(200, response.getStatusCode());
                assertEquals(HttpVersion.HTTP_2.name(), response.getBodyAsString().block(Duration.ofSeconds(30)));
            }
        } finally {
            vertx.close().toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
        }
    }

    @Override
    protected HttpClient createHttpClient(HttpProtocolVersion version, boolean applySetting) throws Exception {
        PemTrustOptions trustOptions = new PemTrustOptions();
        for (X509Certificate certificate : getTrustManager().getAcceptedIssuers()) {
            String pem = "-----BEGIN CERTIFICATE-----\n"
                + Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(certificate.getEncoded())
                + "\n-----END CERTIFICATE-----\n";
            trustOptions.addCertValue(Buffer.buffer(pem));
        }
        VertxHttpClientBuilder builder
            = new VertxHttpClientBuilder().httpClientOptions(new HttpClientOptions().setTrustOptions(trustOptions));
        if (applySetting) {
            builder.maximumHttpVersion(version);
        }
        return builder.build();
    }

    @Override
    protected HttpProtocolVersion getDefaultMaximumHttpVersion() {
        return HttpProtocolVersion.HTTP_1_1;
    }

    @Override
    protected void closeHttpClient(HttpClient client) throws Exception {
        ((VertxHttpClient) client).client.close().toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
    }
}
