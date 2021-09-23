// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.tracing.sleuth;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.CustomerProvidedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SleuthHttpPolicyTest {

    @Mock
    private Tracer tracer;

    @Mock
    private Propagator propagator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void addPolicyForBlobServiceClientBuilder() {
        SleuthHttpPolicy sleuthHttpPolicy = new SleuthHttpPolicy(tracer, propagator);
        // key is test-key
        CustomerProvidedKey providedKey = new CustomerProvidedKey("dGVzdC1rZXk=");
        TokenCredential tokenCredential = new ClientSecretCredentialBuilder()
            .clientSecret("dummy-secret")
            .clientId("dummy-client-id")
            .tenantId("dummy-tenant-id")
            .build();
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .customerProvidedKey(providedKey)
            .credential(tokenCredential)
            .addPolicy(sleuthHttpPolicy)
            .endpoint("https://test.blob.core.windows.net/")
            .buildClient();

        HttpPipeline pipeline = blobServiceClient.getHttpPipeline();
        assertEquals(10, pipeline.getPolicyCount());
        assertEquals(SleuthHttpPolicy.class, pipeline.getPolicy(6).getClass());
    }

    @Test
    public void addAfterPolicyForHttpPipeline() {
        final HttpPipeline pipeline = createHttpPipeline();
        assertEquals(1, pipeline.getPolicyCount());
        assertEquals(SleuthHttpPolicy.class, pipeline.getPolicy(0).getClass());
    }

    private HttpPipeline createHttpPipeline() {
        final HttpClient httpClient = HttpClient.createDefault();
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new SleuthHttpPolicy(tracer, propagator));
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
        return httpPipeline;
    }
}
