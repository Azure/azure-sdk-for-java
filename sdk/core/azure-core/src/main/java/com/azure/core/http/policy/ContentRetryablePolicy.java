// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ContentRetryablePolicy implements HttpPipelinePolicy {
    private static String BOOK_KEEP_OBJECT_KEY = "f3ad1096-9c58-410a-8ed4-d1edb45e498d";
    private final ContentRetryConfig contentRetryConfig;

    public ContentRetryablePolicy() {
        this(ContentRetryConfig.DEFAULT);
    }

    public ContentRetryablePolicy(ContentRetryConfig contentRetryConfig) {
        this.contentRetryConfig = contentRetryConfig;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext callContext, HttpPipelineNextPolicy nextPolicy) {
        return nextPolicy.process()
            .map(new Function<HttpResponse, HttpResponse>() {
                @Override
                public HttpResponse apply(HttpResponse firstResponse) {
                    Flux<ByteBuffer> retryableContent
                        = Flux.deferWithContext(new Function<Context, Flux<ByteBuffer>>() {
                        @Override
                        public Flux<ByteBuffer> apply(reactor.util.context.Context context) {
                            final BookKeep bookKeep = context.get(BOOK_KEEP_OBJECT_KEY);
                            return firstResponse.getBody()
                                // If no next chunk emitted for a given duration then break
                                // the possibly blocked Flux<ByteBuffer>.
                                .timeout(contentRetryConfig.getReadTimeout())
                                // Once unblocked (as a result of TimeoutException) then
                                // switch to retry enabled Flux<ByteBuffer>
                                .onErrorResume(onErrorResumer(nextPolicy))
                                .doOnNext(new Consumer<ByteBuffer>() {
                                    @Override
                                    public void accept(ByteBuffer chunk) {
                                        // account for each successful chunk emission.
                                        bookKeep.addToBytesEmitted(chunk.remaining());
                                    }
                                });
                        }
                    }).subscriberContext(new Function<Context, Context>() {
                        @Override
                        public reactor.util.context.Context apply(Context reactorContext) {
                            // When downstream subscribe to the content (which is after response
                            // subscription) create & store a new BookKeep instance.
                            return reactorContext.put(BOOK_KEEP_OBJECT_KEY, new BookKeep());
                        }
                    });
                    //
                    // create a HttpResponse for the downstream subscriber based on the firstResponse
                    // but with retry enabled Flux<ByteBuffer> as body.
                    return new RetryableContentHttpResponse(firstResponse, retryableContent);
                }
            });
    }

    private Function<Throwable, Flux<ByteBuffer>> onErrorResumer(HttpPipelineNextPolicy nextPolicy) {
        return new Function<Throwable, Flux<ByteBuffer>>() {
            @Override
            public Flux<ByteBuffer> apply(Throwable throwable) {
                if (throwable instanceof TimeoutException) {
                    // On TimeoutException provide a retry enabled Flux<ByteBuffer>
                    return retryableContentFlux(nextPolicy, throwable);
                }
                return Flux.error(throwable);
            }
        };
    }

    private Flux<ByteBuffer> retryableContentFlux(HttpPipelineNextPolicy nextPolicy,
                                                  Throwable throwable) {
        return Flux.deferWithContext(new Function<Context, Flux<ByteBuffer>>() {
            @Override
            public Flux<ByteBuffer> apply(reactor.util.context.Context context) {
                // This was subscribed because the blocked Flux<ByteBuffer> that downstream
                // subscribed was killed via TimeoutException, so now we need to swap that
                // with a new Flux<ByteBuffer>, which emit chunks from where the old
                // Flux<ByteBuffer> was left off.
                //
                final BookKeep bookKeep = context.get(BOOK_KEEP_OBJECT_KEY);
                if (bookKeep.getRetryCount() > contentRetryConfig.getMaxRetryCount()) {
                    throw Exceptions.propagate(throwable);
                }
                bookKeep.incRetryCount();
                bookKeep.initBytesToSkip();

                return nextPolicy.process()
                    .delaySubscription(contentRetryConfig.getRetryDelay())
                    .flatMapMany(new Function<HttpResponse, Flux<ByteBuffer>>() {
                        @Override
                        public Flux<ByteBuffer> apply(HttpResponse response) {
                            return response.getBody()
                                // If no next chunk emitted for a given duration then break
                                // the possibly blocked Flux<ByteBuffer>.
                                .timeout(contentRetryConfig.getReadTimeout())
                                // If TimeoutException then swap current Flux<ByteBuffer> with
                                // new retry enabled Flux<ByteBuffer>
                                //
                                .onErrorResume(onErrorResumer(nextPolicy))
                                .skipWhile(new Predicate<ByteBuffer>() {
                                    @Override
                                    public boolean test(ByteBuffer chunk) {
                                        // Since last Flux<ByteBuffer> would have emitted some
                                        // chunks before it got into block state, skip all
                                        // the bytes those are already emitted.
                                        if (bookKeep.getBytesToSkip() > 0) {
                                            return bookKeep.minusFromBytesToSkip(chunk.remaining()) >= 0;
                                        } else {
                                            return false;
                                        }
                                    }
                                }).doOnNext(new Consumer<ByteBuffer>() {
                                    @Override
                                    public void accept(ByteBuffer chunk) {
                                        int bytesToSkip = bookKeep.getBytesToSkip();
                                        if (bytesToSkip < 0) {
                                            chunk.position(Math.abs(bytesToSkip));
                                        }
                                        bookKeep.clearBytesToSkip();
                                    }
                                });
                        }
                    });
            }
        });
    }

    public static class ContentRetryConfig {
        public static ContentRetryConfig DEFAULT = new ContentRetryConfig(Duration.ofMinutes(2),
            Duration.ofSeconds(2),
            5);
        private final Duration readTimeout;
        private final Duration retryDelay;
        private final int maxRetryCount;

        public ContentRetryConfig(Duration readTimeout, Duration retryDelay, int maxRetryCount) {
            this.readTimeout = readTimeout;
            this.retryDelay = retryDelay;
            this.maxRetryCount = maxRetryCount;
        }

        Duration getReadTimeout() {
            return this.readTimeout;
        }

        Duration getRetryDelay() {
            return this.retryDelay;
        }

        int getMaxRetryCount() {
            return this.maxRetryCount;
        }
    }

    private static class BookKeep {
        private int bytesEmitted;
        private int bytesToSkip;
        private int retryCount;

        BookKeep() {}

        int addToBytesEmitted(int count) {
            return this.bytesEmitted += count;
        }

        void initBytesToSkip() {
            this.bytesToSkip = bytesEmitted;
        }

        int getBytesToSkip() {
            return this.bytesToSkip;
        }

        int minusFromBytesToSkip(int count) {
            return this.bytesToSkip -= count;
        }

        void clearBytesToSkip() {
            this.bytesToSkip = 0;
        }

        int getRetryCount() {
            return this.retryCount;
        }

        void incRetryCount() {
            this.retryCount++;
        }
    }

    private static class RetryableContentHttpResponse extends HttpResponse {
        private final HttpResponse firstResponse;
        private final Flux<ByteBuffer> retryableContent;

        protected RetryableContentHttpResponse(HttpResponse firstResponse,
                                               Flux<ByteBuffer> retryableContent) {
            super(firstResponse.getRequest());
            this.firstResponse = firstResponse;
            this.retryableContent = retryableContent;
        }

        @Override
        public int getStatusCode() {
            return this.firstResponse.getStatusCode();
        }

        @Override
        public String getHeaderValue(String s) {
            return this.firstResponse.getHeaderValue(s);
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.firstResponse.getHeaders();
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return this.retryableContent;
        }

        // Following aggregation methods are not currently backed
        // by retryableContent.

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return this.firstResponse.getBodyAsByteArray();
        }

        @Override
        public Mono<String> getBodyAsString() {
            return this.firstResponse.getBodyAsString();
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return this.firstResponse.getBodyAsString(charset);
        }
    }
}
