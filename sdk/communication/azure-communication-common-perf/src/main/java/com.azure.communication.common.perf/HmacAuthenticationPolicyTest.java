package com.azure.communication.common.perf;

import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.*;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HmacAuthenticationPolicyTest extends PerfStressTest<PerfStressOptions> {

    public class CustomPair {
        String signature;
        int count;

        public CustomPair(String signature){
            this.signature = signature;
            this.count = 1;
        }

        @Override
        public String toString() {
            return  " count=" + count +", signature='" + signature;
        }
    }

    private final HmacAuthenticationPolicy hmacAuthenticationPolicy;
    private final HttpPipeline pipeline;
    private final HttpRequest request;
    private ConcurrentHashMap<String, CustomPair> dateToSignature = new ConcurrentHashMap<>();

    class NoOpHttpClient implements HttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            HttpResponse response = new MockHttpResponse(request, 200);
            return Mono.just(response);
        }
    }

    public HmacAuthenticationPolicyTest(PerfStressOptions options) throws MalformedURLException {
        super(options);
        String mockedKey = "JdppJP5eH1w/CQ0cx4RGYWoC7NmQ0nmDbYR2PYWSDTXojV9bI1ck0Eh0sUIg8xj4KYj7tv+ZPLICu3BgLt6mMz==";
        AzureKeyCredential keyCredential = new AzureKeyCredential(mockedKey);
        hmacAuthenticationPolicy = new HmacAuthenticationPolicy(keyCredential);
        pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(hmacAuthenticationPolicy)
            .build();
        request = new HttpRequest(HttpMethod.GET, new URL("https://test.com/"));
    }

    @Override
    public void run() {
        pipeline.send(request);
    }

    @Override
    public Mono<Void> runAsync() {
        HttpResponse response = pipeline.send(request).block();
        String date = response.getRequest().getHeaders().getValue("date");
        String signature = response.getRequest().getHeaders().getValue("Authorization");
        checkSignatureCorrectness(date, signature);
        // System.out.println("***" + response.getRequest().getHeaders().getValue("testio"));
        return Mono.empty();
    }

    private void checkSignatureCorrectness(String date, String signature){
        if(!dateToSignature.containsKey(date))
            dateToSignature.put(date, new CustomPair(signature));
        else if(!dateToSignature.get(date).signature.contentEquals(signature)){
            System.out.println("Incorrectly computed signature:" + signature + " for" + signature);
            System.out.println("Expected:" + dateToSignature.get(date));
            System.out.println();
        }else{
            synchronized(this) {
                dateToSignature.get(date).count++;
            }
        }
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        for (Map.Entry<String, CustomPair> entry : dateToSignature.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue().toString());
        }
        return super.globalCleanupAsync();
    }

}
