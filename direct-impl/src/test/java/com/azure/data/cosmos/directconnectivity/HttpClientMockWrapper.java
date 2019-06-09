/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.directconnectivity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import rx.Observable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HttpClientMockWrapper {
    public static HttpClientBehaviourBuilder httpClientBehaviourBuilder() {
        return new HttpClientBehaviourBuilder();
    }

    public static class HttpClientBehaviourBuilder {
        private int status;
        private String content;
        private HttpHeaders httpHeaders = new DefaultHttpHeaders();
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
                this.httpHeaders.add(pairs[2*i], pairs[2*i +1]);
            }

            return this;
        }

        public HttpClientBehaviourBuilder withContent(String content) {
            this.content = content;
            return this;
        }

        public HttpClientBehaviourBuilder withHeaderLSN(long lsn) {
            this.httpHeaders.add(WFConstants.BackendHeaders.LSN, Long.toString(lsn));
            return this;
        }

        public HttpClientBehaviourBuilder withHeaderPartitionKeyRangeId(String partitionKeyRangeId) {
            this.httpHeaders.add(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId);
            return this;
        }

        public HttpClientBehaviourBuilder withHeaderSubStatusCode(int subStatusCode) {
            this.httpHeaders.add(WFConstants.BackendHeaders.SUB_STATUS, Integer.toString(subStatusCode));
            return this;
        }

        public HttpClientResponse<ByteBuf> asHttpClientResponse() {
            if (this.networkFailure != null) {
                return null;
            }

            HttpClientResponse<ByteBuf> resp = Mockito.mock(HttpClientResponse.class);
            Mockito.doReturn(HttpResponseStatus.valueOf(status)).when(resp).getStatus();
            Mockito.doReturn(Observable.just(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, content))).when(resp).getContent();

            DefaultHttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(status), httpHeaders);

            try {
                Constructor<HttpResponseHeaders> constructor = HttpResponseHeaders.class.getDeclaredConstructor(HttpResponse.class);
                constructor.setAccessible(true);
                HttpResponseHeaders httpResponseHeaders = constructor.newInstance(httpResponse);
                Mockito.doReturn(httpResponseHeaders).when(resp).getHeaders();

            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new IllegalStateException("Failed to instantiate class object.", e);
            }

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

    private final CompositeHttpClient httpClient;
    private final List<ImmutablePair<HttpClientRequest<ByteBuf>, RxClient.ServerInfo>> requests = Collections.synchronizedList(new ArrayList<>());

    public HttpClientMockWrapper(long responseAfterMillis, HttpClientResponse<ByteBuf> httpClientResponse) {
        this(responseAfterMillis, httpClientResponse, null);
    }

    private static Observable<HttpClientResponse<ByteBuf>> httpClientResponseOrException(HttpClientResponse<ByteBuf> httpClientResponse, Exception e) {
        assert ((httpClientResponse != null && e == null) || (httpClientResponse == null && e != null));
        return httpClientResponse != null ? Observable.just(httpClientResponse) : Observable.error(e);
    }

    public HttpClientMockWrapper(long responseAfterMillis, Exception e) {
        this(responseAfterMillis, null, e);
    }

    public HttpClientMockWrapper(HttpClientResponse<ByteBuf> httpClientResponse) {
        this(0, httpClientResponse);
    }

    private HttpClientMockWrapper(long responseAfterMillis, final HttpClientResponse<ByteBuf> httpClientResponse, final Exception e) {
        httpClient = Mockito.mock(CompositeHttpClient.class);
        assert httpClientResponse == null || e == null;

        Mockito.doAnswer(new Answer() {
            @Override
            public Observable<HttpClientResponse<ByteBuf>> answer(InvocationOnMock invocationOnMock) throws Throwable {
                RxClient.ServerInfo serverInfo = invocationOnMock.getArgumentAt(0, RxClient.ServerInfo.class);
                HttpClientRequest<ByteBuf> req = invocationOnMock.getArgumentAt(1, HttpClientRequest.class);

                requests.add(new ImmutablePair<>(req, serverInfo));

                if (responseAfterMillis <= 0) {
                    return httpClientResponseOrException(httpClientResponse, e);
                } else {
                    return Observable.timer(responseAfterMillis, TimeUnit.MILLISECONDS).flatMap(t -> httpClientResponseOrException(httpClientResponse, e));
                }
            }
        }).when(httpClient).submit(Mockito.any(RxClient.ServerInfo.class), Mockito.any(HttpClientRequest.class));
    }

    public HttpClientMockWrapper(HttpClientBehaviourBuilder builder) {
        this(0, builder.asHttpClientResponse(), builder.asNetworkFailure());
    }

    public HttpClientMockWrapper(Exception e) {
        this(0, e);
    }

    public CompositeHttpClient<ByteBuf, ByteBuf> getClient() {
        return httpClient;
    }

    public List<ImmutablePair<HttpClientRequest<ByteBuf>, RxClient.ServerInfo>> getCapturedInvocation() {
        return requests;
    }
}
