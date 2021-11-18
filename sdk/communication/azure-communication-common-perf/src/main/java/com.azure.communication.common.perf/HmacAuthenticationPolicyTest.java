package com.azure.communication.common.perf;

import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.*;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

public class HmacAuthenticationPolicyTest extends PerfStressTest<PerfStressOptions> {

    private final HmacAuthenticationPolicy hmacAuthenticationPolicy;
    private final HttpPipeline pipeline;
    private final HttpRequest request;

    class NoOpHttpClient implements HttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.empty(); // NOP
        }
    }

    public HmacAuthenticationPolicyTest(PerfStressOptions options) throws MalformedURLException {
        super(options);
        AzureKeyCredential keyCredential = new AzureKeyCredential("<access_key>");
        hmacAuthenticationPolicy = new HmacAuthenticationPolicy(keyCredential);
        pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(hmacAuthenticationPolicy)
            .build();

       request = new HttpRequest(HttpMethod.GET, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
    }

    @Override
    public void run() {
        pipeline.send(request);
    }

    @Override
    public Mono<Void> runAsync() {
        return pipeline.send(request).then();
    }

}
