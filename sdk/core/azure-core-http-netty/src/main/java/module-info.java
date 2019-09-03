module com.azure.http.netty {
    requires com.azure.core;

    requires reactor.netty;
    requires io.netty.buffer;
    requires io.netty.common;
    requires io.netty.transport;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.codec.http;

    exports com.azure.core.http.netty;

    provides com.azure.core.http.HttpClientProvider
        with com.azure.core.http.netty.implementation.ReactorNettyClientProvider;
}
