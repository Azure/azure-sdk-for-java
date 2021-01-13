package com.azure.security.attestation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.EnvironmentCredentialBuilder;

import java.util.ArrayList;
import java.util.List;

public class AttestationClientTestBase extends TestBase {
    private final String isolatedEndpoint = Configuration.getGlobalConfiguration().get("ATTESTATION_ISOLATED_URL");
    private final String aadEndpoint = Configuration.getGlobalConfiguration().get("ATTESTATION_AAD_URL");
    private final String regionShortName = Configuration.getGlobalConfiguration().get("locationShortName");
    private final String sharedEndpoint = "https://shared" + regionShortName + "." + regionShortName + ".test.attest.azure.net";

    private static String dataPlaneScope ="https://attest.azure.net/.default";

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new EnvironmentCredentialBuilder().httpClient(httpClient).build();
        }

        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, dataPlaneScope));
        }

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return pipeline;
    }

    String getIsolatedEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "http://localhost:8080"
            : isolatedEndpoint;
    }

    String getAadEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "http://localhost:8080"
            : aadEndpoint;
    }

    String getSharedEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "http://localhost:8080"
            : sharedEndpoint;
    }


}
