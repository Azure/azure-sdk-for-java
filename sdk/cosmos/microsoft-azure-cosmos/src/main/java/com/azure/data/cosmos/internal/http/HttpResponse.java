// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.http;

import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

import java.nio.charset.Charset;

/**
 * The type representing response of {@link HttpRequest}.
 */
public abstract class HttpResponse implements AutoCloseable {
    private HttpRequest request;

    /**
     * Get the response status code.
     *
     * @return the response status code
     */
    public abstract int statusCode();

    /**
     * Lookup a response header with the provided name.
     *
     * @param name the name of the header to lookup.
     * @return the value of the header, or null if the header doesn't exist in the response.
     */
    public abstract String headerValue(String name);

    /**
     * Get all response headers.
     *
     * @return the response headers
     */
    public abstract HttpHeaders headers();

    /**
     * Get the publisher emitting response content chunks.
     *
     * <p>
     * Returns a stream of the response's body content. Emissions may occur on the
     * Netty EventLoop threads which are shared across channels and should not be
     * blocked. Blocking should be avoided as much as possible/practical in reactive
     * programming but if you do use methods like {@code blockingSubscribe} or {@code blockingGet}
     * on the stream then be sure to use {@code subscribeOn} and {@code observeOn}
     * before the blocking call. For example:
     *
     * <pre>
     * {@code
     *   response.body()
     *     .map(bb -> bb.limit())
     *     .reduce((x,y) -> x + y)
     *     .subscribeOn(Schedulers.io())
     *     .observeOn(Schedulers.io())
     *     .blockingGet();
     * }
     * </pre>
     * <p>
     * The above code is a simplistic example and would probably run fine without
     * the `subscribeOn` and `observeOn` but should be considered a template for
     * more complex situations.
     *
     * @return The response's content as a stream of {@link ByteBuf}.
     */
    public abstract Flux<ByteBuf> body();

    /**
     * Get the response content as a byte[].
     *
     * @return this response content as a byte[]
     */
    public abstract Mono<byte[]> bodyAsByteArray();

    /**
     * Get the response content as a string.
     *
     * @return This response content as a string
     */
    public abstract Mono<String> bodyAsString();

    /**
     * Get the response content as a string.
     *
     * @param charset the charset to use as encoding
     * @return This response content as a string
     */
    public abstract Mono<String> bodyAsString(Charset charset);

    /**
     * Get the request which resulted in this response.
     *
     * @return the request which resulted in this response.
     */
    public final HttpRequest request() {
        return request;
    }

    /**
     * Sets the request which resulted in this HttpResponse.
     *
     * @param request the request
     * @return this HTTP response
     */
    public final HttpResponse withRequest(HttpRequest request) {
        this.request = request;
        return this;
    }

    /**
     * Get a new Response object wrapping this response with it's content
     * buffered into memory.
     *
     * @return the new Response object
     */
    public HttpResponse buffer() {
        return new BufferedHttpResponse(this);
    }

    /**
     * Closes the response content stream, if any.
     */
    @Override
    public void close() {
    }

    // package private for test purpose
    Connection internConnection() {
        return null;
    }
}
