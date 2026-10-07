// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.util.Configuration;
import com.azure.core.validation.http.HttpProtocolVersionTests;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.lang.reflect.Field;

/**
 * Tests HTTP-version negotiation with the JDK HTTP client.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
public class JdkHttpClientHttp2Tests extends HttpProtocolVersionTests {
    @Override
    protected HttpClient createHttpClient(HttpProtocolVersion version, boolean applySetting) throws Exception {
        JdkHttpClientBuilder builder
            = new JdkHttpClientBuilder(java.net.http.HttpClient.newBuilder().sslContext(getSslContext()))
                .configuration(Configuration.NONE);
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
        closeNativeClient(client);
    }

    static void closeNativeClient(HttpClient client) throws Exception {
        java.net.http.HttpClient nativeClient = getNativeClient(client);
        if (nativeClient instanceof AutoCloseable) {
            ((AutoCloseable) nativeClient).close();
        }
    }

    static java.net.http.HttpClient getNativeClient(HttpClient client) throws ReflectiveOperationException {
        Field field = JdkHttpClient.class.getDeclaredField("jdkHttpClient");
        field.setAccessible(true);
        return (java.net.http.HttpClient) field.get(client);
    }
}
