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
import java.util.concurrent.ConcurrentHashMap;
/**
 * HmacAuthenticationPolicyTest is designed to verify the correctness of the calculation
 * of the request signature header in the HmacAuthenticationPolicy in a race condition.
 *
 * <p> The test checks if the same signature is calculated for the request with the exact same
 * signed headers values in parallel threads, otherwise an exception is thrown. </p>
 *
 */
public class HmacAuthenticationPolicyTest extends PerfStressTest<PerfStressOptions> {

    private final static ConcurrentHashMap<String, String> dateToSignature = new ConcurrentHashMap<>();
    // Do not change this otherwise CredScan will flag this.
    private final static String mockedKey = "JdppJP5eH1w/CQ0cx4RGYWoC7NmQ0nmDbYR2PYWSDTXojV9bI1ck0Eh0sUIg8xj4KYj7tv+ZPLICu3BgLt6mMz==";
    private final static HmacAuthenticationPolicy hmacAuthenticationPolicy = new HmacAuthenticationPolicy(new AzureKeyCredential(mockedKey));

    private final HttpPipeline pipeline;
    private final HttpRequest request;

    class NoOpHttpClient implements HttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            HttpResponse response = new MockHttpResponse(request, 200);
            return Mono.just(response);
        }
    }

    public HmacAuthenticationPolicyTest(PerfStressOptions options) throws MalformedURLException {
        super(options);
        pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(hmacAuthenticationPolicy)
            .build();
        request = new HttpRequest(HttpMethod.GET, new URL("https://test.com/"));
    }

    @Override
    public void run() {
        HttpResponse response = pipeline.send(request).block();
        String date = response.getRequest().getHeaders().getValue("x-ms-date");
        String signature = response.getRequest().getHeaders().getValue("Authorization");
        checkSignatureCorrectness(date, signature);
    }

    @Override
    public Mono<Void> runAsync() {
        return pipeline.send(request)
            .flatMap(response -> {
                String date = response.getRequest().getHeaders().getValue("x-ms-date");
                String signature = response.getRequest().getHeaders().getValue("Authorization");
                try {
                    checkSignatureCorrectness(date, signature);
                } catch (Exception e) {
                    return Mono.error(e);
                }
                return Mono.empty();
            }).then();
    }

    private void checkSignatureCorrectness(String date, String signature){
        if(!dateToSignature.containsKey(date))
            dateToSignature.put(date, signature);
        else if(!dateToSignature.get(date).contentEquals(signature)){
            String warning = "Incorrectly computed signature:" + signature + " for " + date
                + "\nExpected:" + dateToSignature.get(date);
            throw new IllegalStateException(warning);
        }
    }

}
