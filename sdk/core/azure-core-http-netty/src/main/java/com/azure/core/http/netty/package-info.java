// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/en-us/java/api/overview/azure/core-http-netty-readme?view=azure-java-stable">
 * Azure Core Http Netty</a> client library is a plugin for the azure-core HTTP client API. It allows you to use Netty as
 * the underlying HTTP client for communicating with Azure services. It is the default HTTP client used in all
 * Azure SDK for Java libraries, but you can also replace it with other implementations such as OkHttp or
 * JDK 11 HttpClient. You can also configure various aspects of the Netty client, such as proxy, protocol, or
 * chunk size.</p>
 *
 * <p><strong>Sample: Construct NettyAsyncHttpClient with Default Configuration</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a Netty HttpClient that uses port 80 and has no proxy.</p>
 *
 * <!-- src_embed readme-sample-createBasicClient -->
 * <pre>
 * HttpClient client = new NettyAsyncHttpClientBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createBasicClient  -->
 *
 * <hr>
 *
 * <h2><strong>Using NettyAsyncHttpClient with Http Proxy</strong></h2>
 *
 * <p>Configuring the Netty client with a proxy in the context of Azure Java SDK is relevant when your application needs
 * to communicate with Azure services through a proxy server. Proxies are commonly used for security, monitoring, and
 * other purposes.</p>
 *
 * <p>Here are some scenarios where you might need to configure the Netty client with a proxy:</p>
 *
 * <ul>
 *     <li>Corporate Network Environment: If your application is running in a corporate network that requires
 *     communication through a proxy server for internet access, you might need to configure the netty client to use
 *     the proxy.</li>
 *
 *     <li>Security Requirements: In some cases, organizations enforce security policies that mandate communication
 *     through a proxy for monitoring, logging, or filtering purposes. Configuring the Netty client with a proxy helps
 *     in meeting these security requirements.</li>
 *
 *     <li>Restricted Network Access: If your application is deployed in an environment where direct internet access
 *     is restricted, but the proxy allows external communication, configuring the Netty client with a proxy becomes
 *     necessary.</li>
 *
 *     <li>Proxy for Azure Services: Some Azure services may require communication through a proxy, and configuring
 *     the Netty client appropriately becomes essential to establish connections.</li>
 *
 *     <li>Logging and Auditing: If your organization requires detailed logging or auditing of outgoing network
 *     requests, configuring the Netty client with a proxy allows you to capture and analyze the traffic passing
 *     through the proxy.</li>
 *
 * </ul>
 *
 * <p>The following code sample demonstrates the creation of a Netty HttpClient that is using a proxy.</p>
 *
 * <!-- src_embed readme-sample-createProxyClient -->
 * <pre>
 * HttpClient client = new NettyAsyncHttpClientBuilder&#40;&#41;
 *     .proxy&#40;new ProxyOptions&#40;ProxyOptions.Type.HTTP, new InetSocketAddress&#40;&quot;&lt;proxy-host&gt;&quot;, 8888&#41;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createProxyClient  -->
 *
 * <hr>
 *
 * <h2><strong>Using NettyAsyncHttpClient with HTTP/2 Support</strong></h2>
 *
 * <p>Configuring the Netty client with HTTP/2 support is relevant when you want to take advantage of the benefits
 * provided by the HTTP/2 protocol. HTTP/2 is the next version of the HTTP network protocol, designed to improve the
 * performance of web applications by allowing multiple requests and responses to be multiplexed over a
 * single connection.</p>
 *
 * <p>Here are some scenarios where you might consider configuring the Netty client with HTTP/2 support:</p>
 *
 * <ul>
 *     <li>Improved Performance: HTTP/2 is designed to be more efficient than its predecessor, HTTP/1.1. It allows for
 *     the multiplexing of multiple streams over a single connection, reducing latency and improving overall
 *     performance. If you are working with Azure services that support HTTP/2, enabling it can lead to better
 *     performance.</li>
 *
 *     <li>Service Support: Some Azure services might support or even require communication using the HTTP/2 protocol.
 *     In such cases, configuring the Netty client with HTTP/2 support becomes necessary to establish connections to
 *     these services.</li>
 *
 *     <li>Concurrency: HTTP/2 enables concurrent requests and responses, which can be particularly beneficial for
 *     applications that need to make multiple simultaneous requests to Azure services. This concurrency support can
 *     lead to more efficient resource utilization and reduced response times.</li>
 *
 *     <li>Resource Multiplexing: HTTP/2 allows for multiplexing, meaning multiple requests and responses can be sent
 *     and received in parallel over a single connection. This is especially useful for applications that need
 *     to efficiently utilize network resources.</li>
 * </ul>
 *
 * <p>The following code sample demonstrates the creation of a Netty HttpClient that supports both the HTTP/1.1 and
 * HTTP/2 protocols, with HTTP/2 being the preferred protocol.</p>
 *
 * <!-- src_embed readme-sample-useHttp2WithConfiguredNettyClient -->
 * <pre>
 * &#47;&#47; Constructs an HttpClient that supports both HTTP&#47;1.1 and HTTP&#47;2 with HTTP&#47;2 being the preferred protocol.
 * HttpClient client = new NettyAsyncHttpClientBuilder&#40;reactor.netty.http.client.HttpClient.create&#40;&#41;
 *     .protocol&#40;HttpProtocol.HTTP11, HttpProtocol.H2&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-useHttp2WithConfiguredNettyClient  -->
 *
 * <p>It is also possible to create a Netty HttpClient that only supports HTTP/2.</p>
 *
 * <!-- src_embed readme-sample-useHttp2OnlyWithConfiguredNettyClient -->
 * <pre>
 * &#47;&#47; Constructs an HttpClient that only supports HTTP&#47;2.
 * HttpClient client = new NettyAsyncHttpClientBuilder&#40;reactor.netty.http.client.HttpClient.create&#40;&#41;
 *     .protocol&#40;HttpProtocol.H2&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-useHttp2OnlyWithConfiguredNettyClient  -->
 *
 * <hr>
 *
 * <h2>Using NettyAsyncHttpClient with Custom Max Chunk Size</h2>
 *
 * <p>The adjustment of the max chunk size involves the modification of the maximum size of ByteBufs returned by Netty,
 * subsequently converted to {@code ByteBuffer}. Altering the chunk size can yield positive performance effects, particularly
 * notable in APIs like the download to file methods within Azure Storage libraries such as azure-storage-blob,
 * azure-storage-file-datalake, and azure-storage-file-shares. Notably, improvements in performance have been
 * consistently observed in the range of 32KB to 64KB.</p>
 *
 * <p>The following code sample demonstrates the creation of a Netty HttpClient that uses a custom max chunk size.</p>
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
 * <!-- end readme-sample-customMaxChunkSize  -->
 *
 * @see com.azure.core.http.netty.NettyAsyncHttpClient
 * @see com.azure.core.http.netty.NettyAsyncHttpClientBuilder
 * @see reactor.netty.http.client.HttpClient
 */
package com.azure.core.http.netty;
