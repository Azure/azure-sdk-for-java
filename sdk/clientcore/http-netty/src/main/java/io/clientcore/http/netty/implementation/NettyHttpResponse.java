package io.clientcore.http.netty.implementation;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.util.binarydata.BinaryData;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * Implementation of HttpResponse for Netty synchronous HTTP client.
 */
public class NettyHttpResponse extends HttpResponse<BinaryData> {
    private final FullHttpResponse nettyResponse;
    private BinaryData body;

    public NettyHttpResponse(BinaryData body, int statusCode, HttpRequest request, FullHttpResponse nettyResponse) {
        super(request, statusCode, convertHeaders(nettyResponse.headers()), null);
        this.nettyResponse = nettyResponse;
        this.body = body;
    }

    /**
     * Converts Netty HttpHeaders to core HttpHeaders.
     *
     * @param nettyHeaders Netty HttpHeaders.
     * @return Converted core HttpHeaders.
     */
    private static HttpHeaders convertHeaders(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        HttpHeaders coreHeaders = new HttpHeaders();
        for (String name : nettyHeaders.names()) {
            List<String> values = nettyHeaders.getAll(name);
            coreHeaders.add(new HttpHeader(HttpHeaderName.fromString(name), values));
        }
        return coreHeaders;
    }

    /**
     * Retrieves the body of the response as BinaryData.
     *
     * @return The BinaryData representation of the response body.
     */
    @Override
    public BinaryData getBody() {
        return body;
    }

    @Override
    public void close() {
        if (nettyResponse != null) {
            nettyResponse.release();
        }
    }
}
