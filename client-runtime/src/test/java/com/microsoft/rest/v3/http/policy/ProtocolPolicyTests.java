package com.microsoft.rest.v3.http.policy;

import com.microsoft.rest.v3.http.HttpClient;
import com.microsoft.rest.v3.http.HttpMethod;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.HttpPipelineOptions;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class ProtocolPolicyTests {

    @Test
    public void withOverwrite() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("ftp", "ftp://www.bing.com");
        pipeline.send(createHttpRequest("http://www.bing.com"));
    }

    @Test
    public void withNoOverwrite() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("ftp", false, "https://www.bing.com");
        pipeline.send(createHttpRequest("https://www.bing.com"));
    }
    private static HttpPipeline createPipeline(String protocol, String expectedUrl) {
        return new HttpPipeline(new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.empty(); // NOP
            }
        },
        new HttpPipelineOptions(null),
        new ProtocolPolicy(protocol),
        (context, next) -> {
            assertEquals(expectedUrl, context.httpRequest().url().toString());
            return next.process();
        });
    }

    private static HttpPipeline createPipeline(String protocol, boolean overwrite, String expectedUrl) {
        return new HttpPipeline(new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.empty(); // NOP
            }
        },
        new HttpPipelineOptions(null),
        new ProtocolPolicy(protocol, overwrite, new HttpPipelineOptions(null)),
        (context, next) -> {
            assertEquals(expectedUrl, context.httpRequest().url().toString());
            return next.process();
        });
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, new URL(url), null);
    }
}
