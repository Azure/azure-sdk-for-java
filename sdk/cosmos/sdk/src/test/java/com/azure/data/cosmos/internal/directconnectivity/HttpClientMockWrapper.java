// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.directconnectivity.WFConstants;
import com.azure.data.cosmos.internal.http.HttpClient;
import com.azure.data.cosmos.internal.http.HttpHeaders;
import com.azure.data.cosmos.internal.http.HttpRequest;
import com.azure.data.cosmos.internal.http.HttpResponse;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

;

public class HttpClientMockWrapper {
    public static HttpClientBehaviourBuilder httpClientBehaviourBuilder() {
        return new HttpClientBehaviourBuilder();
    }

    public static class HttpClientBehaviourBuilder {
        private int status;
        private String content;
        private HttpHeaders httpHeaders = new HttpHeaders();
        private Exception networkFailure;

        public HttpClientBehaviourBuilder withNetworkFailure(Exception networkFailure) {
            this.networkFailure = networkFailure;
            return this;
        }

        public HttpClientBehaviourBuilder withStatus(int status) {
            this.status = status;
            return this;
        }

        public HttpClientBehaviourBuilder withHeaders(HttpHeaders httpHeaders) {
            this.httpHeaders = httpHeaders;
            return this;
        }

        public HttpClientBehaviourBuilder withHeaders(String... pairs) {
            if (pairs.length % 2 != 0) {
                throw new IllegalArgumentException();
            }

            for(int i = 0; i < pairs.length/ 2; i++) {
                this.httpHeaders.set(pairs[2*i], pairs[2*i +1]);
            }

            return this;
        }

        public HttpClientBehaviourBuilder withContent(String content) {
            this.content = content;
            return this;
        }

        public HttpClientBehaviourBuilder withHeaderLSN(long lsn) {
            this.httpHeaders.set(WFConstants.BackendHeaders.LSN, Long.toString(lsn));
            return this;
        }

        public HttpClientBehaviourBuilder withHeaderPartitionKeyRangeId(String partitionKeyRangeId) {
            this.httpHeaders.set(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId);
            return this;
        }

        public HttpClientBehaviourBuilder withHeaderSubStatusCode(int subStatusCode) {
            this.httpHeaders.set(WFConstants.BackendHeaders.SUB_STATUS, Integer.toString(subStatusCode));
            return this;
        }

        public HttpResponse asHttpResponse() {
            if (this.networkFailure != null) {
                return null;
            }

            HttpResponse resp = Mockito.mock(HttpResponse.class);
            Mockito.doReturn(this.status).when(resp).statusCode();
            Mockito.doReturn(Flux.just(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, this.content))).when(resp).body();
            Mockito.doReturn(Mono.just(this.content)).when(resp).bodyAsString(StandardCharsets.UTF_8);
            Mockito.doReturn(this.httpHeaders).when(resp).headers();
            return resp;
        }

        public Exception asNetworkFailure() {
            return this.networkFailure;
        }

        @Override
        public String toString() {
            return "HttpClientBehaviourBuilder{" +
                    "status=" + status +
                    ", content='" + content + '\'' +
                    ", httpHeaders=" + httpHeaders +
                    ", networkFailure=" + networkFailure +
                    '}';
        }
    }

    private final HttpClient httpClient;
    private final List<HttpRequest> requests = Collections.synchronizedList(new ArrayList<>());

    public HttpClientMockWrapper(long responseAfterMillis, HttpResponse httpResponse) {
        this(responseAfterMillis, httpResponse, null);
    }

    private static Mono<HttpResponse> httpResponseOrException(HttpResponse httpResponse, Exception e) {
        assert ((httpResponse != null && e == null) || (httpResponse == null && e != null));
        return httpResponse != null ? Mono.just(httpResponse) : Mono.error(e);
    }

    public HttpClientMockWrapper(long responseAfterMillis, Exception e) {
        this(responseAfterMillis, null, e);
    }

    public HttpClientMockWrapper(HttpResponse httpResponse) {
        this(0, httpResponse);
    }

    private HttpClientMockWrapper(long responseAfterMillis, final HttpResponse httpResponse, final Exception e) {
        httpClient = Mockito.mock(HttpClient.class);
        assert httpResponse == null || e == null;

        Mockito.doAnswer(invocationOnMock -> {
            HttpRequest httpRequest = invocationOnMock.getArgumentAt(0, HttpRequest.class);
            requests.add(httpRequest);
            if (responseAfterMillis <= 0) {
                return httpResponseOrException(httpResponse, e);
            } else {
                return Mono.delay(Duration.ofMillis(responseAfterMillis)).flatMap(t -> httpResponseOrException(httpResponse, e));
            }
        }).when(httpClient).send(Mockito.any(HttpRequest.class));
    }

    public HttpClientMockWrapper(HttpClientBehaviourBuilder builder) {
        this(0, builder.asHttpResponse(), builder.asNetworkFailure());
    }

    public HttpClientMockWrapper(Exception e) {
        this(0, e);
    }

    public HttpClient getClient() {
        return httpClient;
    }

    public List<HttpRequest> getCapturedInvocation() {
        return requests;
    }
}
