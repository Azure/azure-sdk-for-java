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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class IdentityTestBase extends TestProxyTestBase {

    public static final String INVALID_DUMMY_TOKEN = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJlbWFpbCI6IkJvYkBjb250b"
        + "3NvLmNvbSIsImdpdmVuX25hbWUiOiJCb2IiLCJpc3MiOiJodHRwOi8vRGVmYXVsdC5Jc3N1ZXIuY29tIiwiYXVkIjoiaHR0cDovL0RlZm"
        + "F1bHQuQXVkaWVuY2UuY29tIiwiaWF0IjoiMTYwNzk3ODY4MyIsIm5iZiI6IjE2MDc5Nzg2ODMiLCJleHAiOiIxNjA3OTc4OTgzIn0.";

    HttpPipeline getHttpPipeline(HttpClient httpClient) {

        String endpoint = AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
        String authenticationScope = "https://vault.azure.net/.default";

        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            List<TestProxySanitizer> customSanitizers = new ArrayList<>();
            customSanitizers.add(new TestProxySanitizer("$..AccessToken", null, INVALID_DUMMY_TOKEN,
                TestProxySanitizerType.BODY_KEY));
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
}
