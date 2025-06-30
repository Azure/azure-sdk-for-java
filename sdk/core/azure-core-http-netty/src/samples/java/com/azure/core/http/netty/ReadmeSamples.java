// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.DefaultAddressResolverGroup;
import reactor.netty.http.HttpProtocol;

import java.net.InetSocketAddress;

/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    /**
     * Sample code for creating a basic Reactor Netty-based {@link HttpClient}.
     */
    public void createBasicClient() {
        // BEGIN: readme-sample-createBasicClient
        HttpClient client = new NettyAsyncHttpClientBuilder().build();
        // END: readme-sample-createBasicClient
    }

    /**
     * Sample code for creating a Reactor Netty-based {@link HttpClient} that has an unauthenticated proxy.
     */
    public void createProxyClient() {
        // BEGIN: readme-sample-createProxyClient
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
            .build();
        // END: readme-sample-createProxyClient
    }

    /**
     * Sample code for creating a Reactor Netty-based {@link HttpClient} that has an authenticated proxy.
     */
    public void createAuthenticatedProxyClient() {
        // BEGIN: readme-sample-createAuthenticatedProxyClient
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
                .setCredentials("<username>", "<password>"))
            .build();
        // END: readme-sample-createAuthenticatedProxyClient
    }

    /**
     * Sample code for creating a Reactor Netty-based {@link HttpClient} that has a proxy with non-proxy hosts.
     */
    public void createProxyWithNonProxyHostsClient() {
        // BEGIN: readme-sample-createProxyWithNonProxyHostsClient
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
                .setCredentials("<username>", "<password>")
                .setNonProxyHosts("<nonProxyHostRegex>"))
            .build();
        // END: readme-sample-createProxyWithNonProxyHostsClient
    }

    /**
     * Sample code for creating a Reactor Netty-based {@link HttpClient} that has an authenticated proxy with non-proxy
     * hosts.
     * <p>
     * This sample passes a Reactor Netty HttpClient with an {@link AddressResolverGroup} configured to resolve issues
     * in the default behavior.
     */
    public void createProxyWithNonProxyHostsClientCustomResolver() {
        // BEGIN: readme-sample-createProxyWithNonProxyHostsClientCustomResolver
        // Create a Reactor Netty HttpClient with a configured AddressResolverGroup to override the default behavior
        // of NettyAsyncHttpClientBuilder.
        //
        // Passing DefaultAddressResolverGroup here will prevent issues with NoopAddressResolverGroup where it won't
        // resolve the address of a non-proxy host.
        //
        // This may run into other issues when calling proxied hosts that the client machine cannot resolve.
        reactor.netty.http.client.HttpClient reactorNettyHttpClient = reactor.netty.http.client.HttpClient.create()
            .resolver(DefaultAddressResolverGroup.INSTANCE);

        HttpClient client = new NettyAsyncHttpClientBuilder(reactorNettyHttpClient)
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
                .setCredentials("<username>", "<password>")
                .setNonProxyHosts("<nonProxyHostRegex>"))
            .build();
        // END: readme-sample-createProxyWithNonProxyHostsClientCustomResolver
    }

    /**
     * Sample code for creating a Reactor Netty-based {@link HttpClient} supports both the HTTP/1.1 and HTTP/2
     * protocols, with HTTP/2 being the preferred protocol.
     */
    public void useHttp2WithConfiguredNettyClient() {
        // BEGIN: readme-sample-useHttp2WithConfiguredNettyClient
        // Constructs an HttpClient that supports both HTTP/1.1 and HTTP/2 with HTTP/2 being the preferred protocol.
        HttpClient client = new NettyAsyncHttpClientBuilder(reactor.netty.http.client.HttpClient.create()
            .protocol(HttpProtocol.HTTP11, HttpProtocol.H2))
            .build();
        // END: readme-sample-useHttp2WithConfiguredNettyClient
    }

    /**
     * Sample code for creating a Reactor Netty-based {@link HttpClient} that only supports HTTP/2.
     */
    public void useHttp2OnlyWithConfiguredNettyClient() {
        // BEGIN: readme-sample-useHttp2OnlyWithConfiguredNettyClient
        // Constructs an HttpClient that only supports HTTP/2.
        HttpClient client = new NettyAsyncHttpClientBuilder(reactor.netty.http.client.HttpClient.create()
            .protocol(HttpProtocol.H2))
            .build();
        // END: readme-sample-useHttp2OnlyWithConfiguredNettyClient
    }

    /**
     * Sample code for creating a Reactor Netty-based {@link HttpClient} with a customized max chunk size.
     * <p>
     * Max chunk size is used to determine the maximum size of the ByteBuf, later converted to ByteBuffer for use
     * throughout the rest of the SDKs and for compatibility with JDK APIs, returned by Netty. Changing this can
     * positively impact the performance of some APIs such as Storage's download to file methods provided in Blobs,
     * Datalake, and Files.
     */
    @SuppressWarnings("deprecation") // maxChunkSize is deprecated in a future version of Reactor Netty
    public void largerMaxChunkSizeWithConfiguredNettyClient() {
        // BEGIN: readme-sample-customMaxChunkSize
        // Constructs an HttpClient with a modified max chunk size.
        // Max chunk size modifies the maximum size of ByteBufs returned by Netty (later converted to ByteBuffer).
        // Changing the chunk size can positively impact performance of APIs such as Storage's download to file methods
        // provided in azure-storage-blob, azure-storage-file-datalake, and azure-storage-file-shares (32KB - 64KB have
        // shown the most consistent improvement).
        HttpClient httpClient = new NettyAsyncHttpClientBuilder(reactor.netty.http.client.HttpClient.create()
            .httpResponseDecoder(httpResponseDecoderSpec -> httpResponseDecoderSpec.maxChunkSize(64 * 1024)))
            .build();
        // END: readme-sample-customMaxChunkSize
    }

    /**
     * Sample for creating a Reactor Netty-based {@link HttpClient} with a customized max header size.
     * <p>
     * {@code maxHeaderSize} is used to determine the maximum headers size Netty can process. The default value is 8192
     * bytes (8KB). If the headers exceed this size, Netty will throw an exception. Passing a customized Reactor Netty
     * HttpClient to the NettyAsyncHttpClientBuilder allows you to set a different value for this parameter.
     */
    public void overrideMaxHeaderSize() {
        // BEGIN: readme-sample-customMaxHeaderSize
        // Constructs an HttpClient with a modified max header size.
        // This creates a Netty HttpClient with a max headers size of 256 KB.
        HttpClient httpClient = new NettyAsyncHttpClientBuilder(reactor.netty.http.client.HttpClient.create()
            .httpResponseDecoder(httpResponseDecoderSpec -> httpResponseDecoderSpec.maxHeaderSize(256 * 1024)))
            .build();
        // END: readme-sample-customMaxHeaderSize
    }
}
