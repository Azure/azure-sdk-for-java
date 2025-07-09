// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author xiaofeicao
 * @createdAt 2021-11-04 15:49
 */
public class HttpPipelineProviderTest {

    static class BeforeRetryPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext,
            HttpPipelineNextPolicy httpPipelineNextPolicy) {
            return httpPipelineNextPolicy.process();
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }

    static class AfterRetryPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext,
            HttpPipelineNextPolicy httpPipelineNextPolicy) {
            return httpPipelineNextPolicy.process();
        }
    }

    @Test
    public void addPolicyTest() {
        //provide before and after retry policy
        HttpPipeline pipeline = createPipeline(Arrays.asList(new AfterRetryPolicy(), new BeforeRetryPolicy()));
        int retryIndex = findPolicyIndex(pipeline, RetryPolicy.class);
        int beforeRetryIndex = findPolicyIndex(pipeline, BeforeRetryPolicy.class);
        int afterRetryIndex = findPolicyIndex(pipeline, AfterRetryPolicy.class);
        Assertions.assertTrue(retryIndex != -1, "retryIndex -1");
        Assertions.assertTrue(beforeRetryIndex != -1, "beforeRetryIndex -1");
        Assertions.assertTrue(afterRetryIndex != -1, "afterRetryIndex -1");

        Assertions.assertTrue(beforeRetryIndex < retryIndex, "beforeRetryIndex >= retryIndex");
        Assertions.assertTrue(afterRetryIndex > retryIndex, "afterRetryIndex <= retryIndex");

        //only provide after
        pipeline = createPipeline(Collections.singletonList(new AfterRetryPolicy()));
        retryIndex = findPolicyIndex(pipeline, RetryPolicy.class);
        beforeRetryIndex = findPolicyIndex(pipeline, BeforeRetryPolicy.class);
        afterRetryIndex = findPolicyIndex(pipeline, AfterRetryPolicy.class);
        Assertions.assertTrue(retryIndex != -1, "retryIndex -1");
        Assertions.assertEquals(-1, beforeRetryIndex, "beforeRetryIndex not -1");
        Assertions.assertTrue(afterRetryIndex != -1, "afterRetryIndex -1");

        Assertions.assertTrue(afterRetryIndex > retryIndex, "afterRetryIndex <= retryIndex");

        //only provide before
        pipeline = createPipeline(Collections.singletonList(new BeforeRetryPolicy()));
        retryIndex = findPolicyIndex(pipeline, RetryPolicy.class);
        beforeRetryIndex = findPolicyIndex(pipeline, BeforeRetryPolicy.class);
        afterRetryIndex = findPolicyIndex(pipeline, AfterRetryPolicy.class);
        Assertions.assertTrue(retryIndex != -1, "retryIndex -1");
        Assertions.assertEquals(-1, afterRetryIndex, "afterRetryIndex not -1");
        Assertions.assertTrue(beforeRetryIndex != -1, "beforeRetryIndex -1");

        Assertions.assertTrue(beforeRetryIndex < retryIndex, "beforeRetryIndex >= retryIndex");

        //provide none
        assertDoesNotThrow(() -> createPipeline(null));
    }

    private HttpPipeline createPipeline(List<HttpPipelinePolicy> policies) {
        return HttpPipelineProvider.buildHttpPipeline(new MockTokenCredential(),
            new AzureProfile(new AzureEnvironment(new HashMap<>())), new String[0], new HttpLogOptions(),
            Configuration.NONE, new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, new NoOpHttpClient());
    }

    private int findPolicyIndex(HttpPipeline pipeline, Class<? extends HttpPipelinePolicy> policyClazz) {
        int policyCount = pipeline.getPolicyCount();
        for (int i = 0; i < policyCount; i++) {
            if (pipeline.getPolicy(i).getClass().isAssignableFrom(policyClazz)) {
                return i;
            }
        }
        return -1;
    }
}
