// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.client;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.client.JdkHttpClientBuilder;
import io.clientcore.core.shared.ExpectContinueTests;
import io.clientcore.core.shared.TestConfigurationSource;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.JRE;

@DisabledForJreRange(max = JRE.JAVA_11)
@EnabledIfSystemProperty(named = "jdk.httpclient.allowRestrictedHeaders", matches = "(?i).*\\bexpect\\b.*")
public class JdkAllowedExpectContinueIT extends ExpectContinueTests {
    private final HttpClient client = new JdkHttpClientBuilder()
        .configuration(
            Configuration.from(new TestConfigurationSource().put("jdk.httpclient.allowRestrictedHeaders", "expect")))
        .setMaximumHttpVersion(HttpProtocolVersion.HTTP_1_1)
        .build();

    @Override
    protected HttpClient getHttpClient() {
        return client;
    }
}
