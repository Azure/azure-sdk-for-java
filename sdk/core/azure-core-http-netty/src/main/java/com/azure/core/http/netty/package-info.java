// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>
 * <a href="https://learn.microsoft.com/java/api/overview/azure/core-http-netty-readme?view=azure-java-stable">
 * Azure Core Http Netty</a> client library is a plugin for the azure-core HTTP client API. It allows you to use Netty
 * as
 * the underlying HTTP client for communicating with Azure services. It is the default HTTP client used in all
 * Azure SDK for Java libraries, but you can also replace it with other implementations such as OkHttp or
 * the JDK 11 HttpClient. You can also configure various aspects of the Netty client, such as proxy, protocol, or
 * chunk size. For more details refer to our
 * <a href="https://learn.microsoft.com/azure/developer/java/sdk/http-client-pipeline#http-clients">conceptual
 * documentation</a>.
 * </p>
 *
 * <p>
 * <strong>Sample: Construct NettyAsyncHttpClient with Default Configuration</strong>
 * </p>
 *
 * <p>
 * The following code sample demonstrates the creation of a Netty HttpClient that uses port 80 and has no proxy.
 * </p>
 *
 * <!-- src_embed readme-sample-createBasicClient -->
 * <pre>
 * HttpClient client = new NettyAsyncHttpClientBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createBasicClient -->
 *
 * <hr>
 *
 * <h2><strong>Using NettyAsyncHttpClient with Http Proxy</strong></h2>
 *
 * <p>
 * Configuring the Netty client with a proxy in the context of Azure Java SDK is relevant when your application needs
 * to communicate with Azure services through a proxy server. For more details refer to our
 * <a href="https://learn.microsoft.com/azure/developer/java/sdk/proxying#http-proxy-configuration">conceptual
 * documentation</a>.
 * </p>
 *
 * <p>
 * The following code sample demonstrates the creation of a Netty HttpClient that is using a proxy.
 * </p>
 *
 * <!-- src_embed readme-sample-createProxyClient -->
 * <pre>
 * HttpClient client = new NettyAsyncHttpClientBuilder&#40;&#41;
 *     .proxy&#40;new ProxyOptions&#40;ProxyOptions.Type.HTTP, new InetSocketAddress&#40;&quot;&lt;proxy-host&gt;&quot;, 8888&#41;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createProxyClient -->
 *
 * <hr>
 *
 * <h2><strong>Using NettyAsyncHttpClient with HTTP/2 Support</strong></h2>
 *
 * <p>
 * The following code sample demonstrates the creation of a Netty HttpClient that supports both the HTTP/1.1 and
 * HTTP/2 protocols, with HTTP/2 being the preferred protocol.
 * </p>
 *
 * <!-- src_embed readme-sample-useHttp2WithConfiguredNettyClient -->
 * <pre>
 * &#47;&#47; Constructs an HttpClient that supports both HTTP&#47;1.1 and HTTP&#47;2 with HTTP&#47;2 being the preferred protocol.
 * HttpClient client = new NettyAsyncHttpClientBuilder&#40;reactor.netty.http.client.HttpClient.create&#40;&#41;
 *     .protocol&#40;HttpProtocol.HTTP11, HttpProtocol.H2&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-useHttp2WithConfiguredNettyClient -->
 *
 * <p>
 * It is also possible to create a Netty HttpClient that only supports HTTP/2.
 * </p>
 *
 * <!-- src_embed readme-sample-useHttp2OnlyWithConfiguredNettyClient -->
 * <pre>
 * &#47;&#47; Constructs an HttpClient that only supports HTTP&#47;2.
 * HttpClient client = new NettyAsyncHttpClientBuilder&#40;reactor.netty.http.client.HttpClient.create&#40;&#41;
 *     .protocol&#40;HttpProtocol.H2&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-useHttp2OnlyWithConfiguredNettyClient -->
 *
 * <hr>
 *
 * <h2>Using NettyAsyncHttpClient with Custom Max Chunk Size</h2>
 *
 * <p>
 * The adjustment of the max chunk size involves the modification of the maximum size of ByteBufs returned by Netty,
 * subsequently converted to {@code ByteBuffer}. Altering the chunk size can yield positive performance effects,
 * particularly
 * notable in APIs like the download to file methods within Azure Storage libraries such as azure-storage-blob,
 * azure-storage-file-datalake, and azure-storage-file-shares. Notably, improvements in performance have been
 * consistently observed in the range of 32KB to 64KB.
 * </p>
 *
 * <p>
 * The following code sample demonstrates the creation of a Netty HttpClient that uses a custom max chunk size.
 * </p>
 *
 * <!-- src_embed readme-sample-customMaxChunkSize -->
 * <pre>
 * &#47;&#47; Constructs an HttpClient with a modified max chunk size.
 * &#47;&#47; Max chunk size modifies the maximum size of ByteBufs returned by Netty &#40;later converted to ByteBuffer&#41;.
 * &#47;&#47; Changing the chunk size can positively impact performance of APIs such as Storage's download to file methods
 * &#47;&#47; provided in azure-storage-blob, azure-storage-file-datalake, and azure-storage-file-shares &#40;32KB - 64KB have
 * &#47;&#47; shown the most consistent improvement&#41;.
 * HttpClient httpClient = new NettyAsyncHttpClientBuilder&#40;reactor.netty.http.client.HttpClient.create&#40;&#41;
 *     .httpResponseDecoder&#40;httpResponseDecoderSpec -&gt; httpResponseDecoderSpec.maxChunkSize&#40;64 * 1024&#41;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-customMaxChunkSize -->
 *
 * @see com.azure.core.http.netty.NettyAsyncHttpClient
 * @see com.azure.core.http.netty.NettyAsyncHttpClientBuilder
 * @see reactor.netty.http.client.HttpClient
 */
package com.azure.core.http.netty;
