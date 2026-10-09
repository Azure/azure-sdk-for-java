// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.validation.http.HttpProtocolVersionTests;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.PemTrustOptions;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Tests HTTP-version negotiation with Vert.x.
 */
public class VertxHttpClientHttp2Tests extends HttpProtocolVersionTests {
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
