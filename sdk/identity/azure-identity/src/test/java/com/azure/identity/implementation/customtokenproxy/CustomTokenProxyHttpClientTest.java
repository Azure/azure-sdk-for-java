// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.customtokenproxy;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomTokenProxyHttpClientTest {
    @ParameterizedTest
    @CsvSource({
        "proxy.example.com, proxy.example.com, true",
        "PROXY.EXAMPLE.COM, proxy.example.com, true",
        "proxy.example.com, unrelated.example.com, false",
        "proxy.example.com, *.example.com, true",
        "PROXY.EXAMPLE.COM, *.example.com, true",
        "proxy.example.com, *.EXAMPLE.COM, true",
        "xn--bcher-kva.example.com, *.example.com, true",
        "b\u00FCcher.example.com, *.example.com, true",
        "example.com, *.example.com, false",
        "nested.proxy.example.com, *.example.com, false",
        "proxy.example.net, *.example.com, false",
        "badexample.com, *.example.com, false",
        ".example.com, *.example.com, false",
        "-proxy.example.com, *.example.com, false",
        "proxy-.example.com, *.example.com, false",
        "proxy .example.com, *.example.com, false",
        "127.0.0.1, *.0.0.1, false",
        "proxy.example.com, p*.example.com, false",
        "proxy.example.com, example.*.com, false",
        "proxy.example.com, *.*.com, false",
        "proxy.example.com, *, false",
        "proxy.example.com, *., false",
        "*.example.com, *.example.com, false" })
    public void matchesDnsName(String serverName, String certificateName, boolean matches) {
        assertEquals(matches, CustomTokenProxyHttpClient.matchesDnsName(serverName, certificateName));
    }

    @Test
    public void preservesLiteralServerNamesWithoutDnsNormalization() {
        String label = String.join("", Collections.nCopies(63, "a"));
        String serverName = "a" + label + ".example.com";

        assertTrue(CustomTokenProxyHttpClient.matchesDnsName(label + ".example.com", "*.example.com"));
        assertTrue(CustomTokenProxyHttpClient.matchesDnsName(serverName, serverName));
        assertFalse(CustomTokenProxyHttpClient.matchesDnsName(serverName, "*.example.com"));
    }

    @Test
    public void asyncSendOffloadsBlockingIoAndPreservesContext() throws MalformedURLException {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://login.microsoftonline.com/tenant/token");
        Context context = new Context("test-context", "value");
        AtomicInteger requests = new AtomicInteger();
        CustomTokenProxyHttpClient client
            = new CustomTokenProxyHttpClient(new ProxyConfig(new URL("https://proxy.example.com"), null, null, null)) {
                @Override
                public HttpResponse sendSync(HttpRequest actualRequest, Context actualContext) {
                    assertFalse(Schedulers.isInNonBlockingThread(), "Proxy I/O must not run on an event-loop thread");
                    assertSame(request, actualRequest);
                    assertSame(context, actualContext);
                    requests.incrementAndGet();
                    return new MockHttpResponse(actualRequest, 200);
                }
            };

        Mono<HttpResponse> response = client.send(request, context);
        assertEquals(0, requests.get(), "Requests must remain lazy until subscription");

        StepVerifier.create(response.subscribeOn(Schedulers.parallel()))
            .assertNext(result -> assertEquals(200, result.getStatusCode()))
            .expectComplete()
            .verify(Duration.ofSeconds(10));
        assertEquals(1, requests.get());
    }
}
