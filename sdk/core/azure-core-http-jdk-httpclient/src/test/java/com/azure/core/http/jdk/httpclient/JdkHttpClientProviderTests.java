// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.util.Configuration;
import com.azure.core.validation.http.HttpClientOptionsProviderTests;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Tests {@link JdkHttpClientProvider}.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
public class JdkHttpClientProviderTests extends HttpClientOptionsProviderTests {
    @Override
    protected HttpClientProvider createProvider(Configuration configuration) {
        return new JdkHttpClientProvider(configuration);
    }

    @Override
    protected void assertMaximumHttpVersion(HttpClient client, HttpProtocolVersion version)
        throws ReflectiveOperationException {
        assertInstanceOf(JdkHttpClient.class, client);
        assertEquals(version == HttpProtocolVersion.HTTP_2
            ? java.net.http.HttpClient.Version.HTTP_2
            : java.net.http.HttpClient.Version.HTTP_1_1, JdkHttpClientHttp2Tests.getNativeClient(client).version());
    }

    @Override
    protected void closeHttpClient(HttpClient client) throws Exception {
        JdkHttpClientHttp2Tests.closeNativeClient(client);
    }
}
