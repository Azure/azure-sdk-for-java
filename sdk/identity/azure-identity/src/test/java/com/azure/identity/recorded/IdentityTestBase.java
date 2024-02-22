package com.azure.identity.recorded;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.*;
import com.azure.core.util.Configuration;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.implementation.util.CertificateUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class IdentityTestBase extends TestProxyTestBase {

    public static final String INVALID_DUMMY_TOKEN = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJlbWFpbCI6IkJvYkBjb250b"
        + "3NvLmNvbSIsImdpdmVuX25hbWUiOiJCb2IiLCJpc3MiOiJodHRwOi8vRGVmYXVsdC5Jc3N1ZXIuY29tIiwiYXVkIjoiaHR0cDovL0RlZm"
        + "F1bHQuQXVkaWVuY2UuY29tIiwiaWF0IjoiMTYwNzk3ODY4MyIsIm5iZiI6IjE2MDc5Nzg2ODMiLCJleHAiOiIxNjA3OTc4OTgzIn0.";

    HttpPipeline getHttpPipeline(HttpClient httpClient) {

        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            List<TestProxySanitizer> customSanitizers = new ArrayList<>();
            customSanitizers.add(new TestProxySanitizer("$..access_token", null, INVALID_DUMMY_TOKEN,
                TestProxySanitizerType.BODY_KEY));
            customSanitizers.add(new TestProxySanitizer("client-request-id", null, "REDACTED",
                TestProxySanitizerType.HEADER));
            customSanitizers.add(new TestProxySanitizer("x-client-last-telemetry", null, "REDACTED",
                TestProxySanitizerType.HEADER));
            customSanitizers.add(new TestProxySanitizer(null, "(client_id=)[^&]+", "$1Dummy-Id",
                TestProxySanitizerType.BODY_REGEX));
            customSanitizers.add(new TestProxySanitizer(null, "(client_secret=)[^&]+", "$1Dummy-Secret",
                TestProxySanitizerType.BODY_REGEX));
            customSanitizers.add(new TestProxySanitizer(null, "(client_assertion=)[^&]+", "$1Dummy-Secret",
                TestProxySanitizerType.BODY_REGEX));

            interceptorManager.addSanitizers(customSanitizers);
        }

        if (interceptorManager.isRecordMode()) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("X-MRC-CV")));
            interceptorManager.addMatchers(customMatchers);
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return pipeline;
    }

    String getClientId() {
        return Configuration.getGlobalConfiguration().get("AZURE_CLIENT_ID");
    }

    String getTenantId() {
        return Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID");
    }

    String getClientSecret() {
        return Configuration.getGlobalConfiguration().get("AZURE_CLIENT_SECRET");
    }

    boolean isPlaybackMode() {
        return interceptorManager.isPlaybackMode();
    }
}
