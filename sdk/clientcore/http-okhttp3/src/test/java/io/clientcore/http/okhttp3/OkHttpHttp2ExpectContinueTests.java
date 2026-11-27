// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.shared.Http2ExpectContinueTests;
import io.clientcore.core.shared.InsecureTrustManager;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class OkHttpHttp2ExpectContinueTests extends Http2ExpectContinueTests {
    private static final HttpClient CLIENT;

    static {
        try {
            X509ExtendedTrustManager trustManager = new InsecureTrustManager();
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { trustManager }, new SecureRandom());
            CLIENT = new OkHttpHttpClientBuilder().configuration(Configuration.none())
                .sslSocketFactory(context.getSocketFactory(), trustManager)
                .hostnameVerifier((hostname, session) -> true)
                .setMaximumHttpVersion(HttpProtocolVersion.HTTP_2)
                .build();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected HttpClient getHttpClient() {
        return CLIENT;
    }

    @Override
    @Disabled("OkHttp 4.12 treats a final HTTP/2 HEADERS frame with END_STREAM after 100 Continue as trailers.")
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void withholdsHttp2BodyUntilContinueWithEmptyFinalResponse(boolean knownLength) {
        super.withholdsHttp2BodyUntilContinueWithEmptyFinalResponse(knownLength);
    }
}
