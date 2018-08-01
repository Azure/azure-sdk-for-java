/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.reactivex.Flowable;
import io.reactivex.Single;

import java.io.Closeable;
import java.nio.ByteBuffer;

import com.microsoft.rest.v2.policy.DecodingPolicyFactory;

/**
 * This class contains all of the details necessary for reacting to a HTTP response from a
 * HttpResponse.
 */
public abstract class HttpResponse implements Closeable {
    private Object deserializedHeaders;
    private Object deserializedBody;
    private boolean isDecoded;
    private HttpRequest request;

    /**
     * Get this response object's HTTP status code.
     * @return This response object's HTTP status code.
     */
    public abstract int statusCode();

    /**
     * Get the header value for the provided header name, or null if the provided header name does
     * not appear in this HttpResponse's headers.
     * @param headerName The name of the header to lookup.
     * @return The value of the header, or null if the header doesn't exist in this HttpResponse.
     */
    public abstract String headerValue(String headerName);

    /**
     * Get all the headers on this HTTP response.
     * @return All headers on this HTTP response.
     */
    public abstract HttpHeaders headers();

    /**
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
     * 
     * <p>
     * The above code is a simplistic example and would probably run fine without
     * the `subscribeOn` and `observeOn` but should be considered a template for
     * more complex situations.
     * 
     * @return The response's body as a stream of {@link ByteBuffer}.
     */
    public abstract Flowable<ByteBuffer> body();

    /**
     * Get this response object's body as a byte[]. If this response object doesn't have a body,
     * then null will be returned.
     * @return This response object's body as a byte[]. If this response object doesn't have a body,
     * then null will be returned.
     */
    public abstract Single<byte[]> bodyAsByteArray();

    /**
     * Get this response object's body as a string. If this response object doesn't have a body,
     * then null will be returned.
     * @return This response object's body as a string. If this response object doesn't have a body,
     * then null will be returned.
     */
    public abstract Single<String> bodyAsString();

    /**
     * Buffers the HTTP response body into memory, allowing the content to be inspected and replayed.
     * @return This HTTP response, with body content buffered into memory.
     */
    public BufferedHttpResponse buffer() {
        return new BufferedHttpResponse(this);
    }

    /**
     * Closes the stream providing this HttpResponse's content, if any.
     */
    public void close() {
        // no-op
    }

    /**
     * Returns a value indicating whether this HttpResponse has been decoded by a {@link DecodingPolicyFactory}.
     * @return whether this HttpResponse has been decoded
     */
    public boolean isDecoded() {
        return isDecoded;
    }

    /**
     * Sets the flag indicating whether this HttpResponse has been decoded by a {@link DecodingPolicyFactory}.
     * @param isDecoded whether this HttpResponse has been decoded
     * @return this HTTP repsonse
     */
    public boolean withIsDecoded(boolean isDecoded) {
        this.isDecoded = isDecoded;
        return isDecoded;
    }

    /**
     * @return the deserialized headers, if present. Otherwise, null.
     */
    public Object deserializedHeaders() {
        return deserializedHeaders;
    }

    /**
     * Set the deserialized headers on this HttpResponse.
     * @param deserializedHeaders the deserialized headers
     * @return this HTTP repsonse
     */
    public HttpResponse withDeserializedHeaders(Object deserializedHeaders) {
        this.deserializedHeaders = deserializedHeaders;
        return this;
    }

    /**
     * @return the deserialized body, if present. Otherwise, null.
     */
    public Object deserializedBody() {
        return deserializedBody;
    }

    /**
     * Sets the deserialized body on this HttpResponse.
     * @param deserializedBody the deserialized body
     * @return this HTTP response
     */
    public HttpResponse withDeserializedBody(Object deserializedBody) {
        this.deserializedBody = deserializedBody;
        return this;
    }

    /**
     * @return The request which resulted in this HttpResponse.
     */
    public final HttpRequest request() {
        return request;
    }


    /**
     * Sets the request on this HttpResponse.
     * @param request the request which resulted in this HttpResponse
     * @return this HTTP response
     */
    public final HttpResponse withRequest(HttpRequest request) {
        this.request = request;
        return this;
    }


}
