package com.azure.resourcemanager.resources;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.*;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpClient;
import com.azure.core.util.Configuration;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author xiaofeicao
 * @createdAt 2021-11-04 15:49
 */
public class HttpPipelineProviderTest {

    static class MockTokenCredential implements TokenCredential {
        final AccessToken token;

        MockTokenCredential(String token) {
            this.token = new AccessToken(token, OffsetDateTime.MAX);
        }

        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            return Mono.just(token);
        }
    }

    static class BeforeRetryPolicy implements HttpPipelinePolicy{

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
            return httpPipelineNextPolicy.process();
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }

    static class AfterRetryPolicy implements HttpPipelinePolicy{

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
            return httpPipelineNextPolicy.process();
        }
    }

    @Test
    public void addPolicyTest(){
        //provide before and after retry policy
        {
            HttpPipeline pipeline = HttpPipelineProvider.buildHttpPipeline(
                new MockTokenCredential("mockToken")
                , new AzureProfile(new AzureEnvironment(new HashMap<>()))
                , new String[0]
                , new HttpLogOptions()
                , new Configuration()
                , new RetryPolicy("Retry-After", ChronoUnit.SECONDS)
                , new ArrayList<>() {{
                    this.add(new AfterRetryPolicy());
                    this.add(new BeforeRetryPolicy());
                }}
                , new MockHttpClient()
            );
            int retryIndex = findRetryPolicyIndex(pipeline);
            int beforeRetryIndex = findBeforeRetryIndex(pipeline);
            int afterRetryIndex = findAfterRetryIndex(pipeline);
            Assertions.assertTrue(retryIndex != -1, "retryIndex -1");
            Assertions.assertTrue(beforeRetryIndex != -1, "beforeRetryIndex -1");
            Assertions.assertTrue(afterRetryIndex != -1, "afterRetryIndex -1");

            Assertions.assertTrue(beforeRetryIndex < retryIndex, "beforeRetryIndex >= retryIndex");
            Assertions.assertTrue(afterRetryIndex > retryIndex, "afterRetryIndex <= retryIndex");
        }

        //only provide after
        {
            HttpPipeline pipeline = HttpPipelineProvider.buildHttpPipeline(
                new MockTokenCredential("mockToken")
                , new AzureProfile(new AzureEnvironment(new HashMap<>()))
                , new String[0]
                , new HttpLogOptions()
                , new Configuration()
                , new RetryPolicy("Retry-After", ChronoUnit.SECONDS)
                , new ArrayList<>() {{
                    this.add(new AfterRetryPolicy());
                }}
                , new MockHttpClient()
            );
            int retryIndex = findRetryPolicyIndex(pipeline);
            int beforeRetryIndex = findBeforeRetryIndex(pipeline);
            int afterRetryIndex = findAfterRetryIndex(pipeline);
            Assertions.assertTrue(retryIndex != -1, "retryIndex -1");
            Assertions.assertEquals(-1, beforeRetryIndex, "beforeRetryIndex not -1");
            Assertions.assertTrue(afterRetryIndex != -1, "afterRetryIndex -1");

            Assertions.assertTrue(afterRetryIndex > retryIndex, "afterRetryIndex <= retryIndex");
        }

        //only provide before
        {
            HttpPipeline pipeline = HttpPipelineProvider.buildHttpPipeline(
                new MockTokenCredential("mockToken")
                , new AzureProfile(new AzureEnvironment(new HashMap<>()))
                , new String[0]
                , new HttpLogOptions()
                , new Configuration()
                , new RetryPolicy("Retry-After", ChronoUnit.SECONDS)
                , new ArrayList<>() {{
                    this.add(new BeforeRetryPolicy());
                }}
                , new MockHttpClient()
            );
            int retryIndex = findRetryPolicyIndex(pipeline);
            int beforeRetryIndex = findBeforeRetryIndex(pipeline);
            int afterRetryIndex = findAfterRetryIndex(pipeline);
            Assertions.assertTrue(retryIndex != -1, "retryIndex -1");
            Assertions.assertEquals(-1, afterRetryIndex, "afterRetryIndex not -1");
            Assertions.assertTrue(beforeRetryIndex != -1, "beforeRetryIndex -1");

            Assertions.assertTrue(beforeRetryIndex < retryIndex, "beforeRetryIndex >= retryIndex");
        }

        //provide none
        {
            HttpPipeline pipeline = HttpPipelineProvider.buildHttpPipeline(
                new MockTokenCredential("mockToken")
                , new AzureProfile(new AzureEnvironment(new HashMap<>()))
                , new String[0]
                , new HttpLogOptions()
                , new Configuration()
                , new RetryPolicy("Retry-After", ChronoUnit.SECONDS)
                , null
                , new MockHttpClient()
            );
        }
    }

    private int findRetryPolicyIndex(HttpPipeline pipeline) {
        return findPolicyIndex(pipeline, RetryPolicy.class);
    }

    private int findBeforeRetryIndex(HttpPipeline pipeline) {
        return findPolicyIndex(pipeline, BeforeRetryPolicy.class);
    }

    private int findAfterRetryIndex(HttpPipeline pipeline) {
        return findPolicyIndex(pipeline, AfterRetryPolicy.class);
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
