// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.client;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.client.JdkHttpClientBuilder;
import io.clientcore.core.shared.Http2ExpectContinueTests;
import io.clientcore.core.shared.InsecureTrustManager;
import io.clientcore.core.shared.TestConfigurationSource;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

@DisabledForJreRange(max = JRE.JAVA_11)
public class JdkHttp2ExpectContinueIT extends Http2ExpectContinueTests {
    private static final boolean EXPECT_ALLOWED
        = "expect".equalsIgnoreCase(System.getProperty("jdk.httpclient.allowRestrictedHeaders"));
    private static final HttpClient CLIENT;

    static {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { new InsecureTrustManager() }, new SecureRandom());
            Configuration configuration = EXPECT_ALLOWED
                ? Configuration
                    .from(new TestConfigurationSource().put("jdk.httpclient.allowRestrictedHeaders", "expect"))
                : Configuration.none();
            CLIENT = new JdkHttpClientBuilder().configuration(configuration)
                .sslContext(context)
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
    protected boolean forwardsOtherExpectations() {
        return EXPECT_ALLOWED;
    }
}
