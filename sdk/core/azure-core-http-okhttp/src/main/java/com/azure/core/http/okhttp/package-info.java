// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/en-us/java/api/overview/azure/core-http-okhttp-readme?view=azure-java-stable">
 * Azure Core Http OkHttp</a> client library is a plugin for the azure-core HTTP client API. It allows you to use OkHttp
 * as the underlying HTTP client for communicating with Azure services. OkHttp is a popular and efficient HTTP client
 * that supports features such as HTTP/2, connection pooling, compression, and caching. To use the OkHttp client library,
 * you need to include the dependency in your project and configure it when creating a service client.</p>
 *
 * <p><strong>Sample: Construct OkHttpAsyncHttpClient with Default Configuration</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a OkHttp HttpClient that uses port 80 and has no proxy.</p>
 *
 * <!-- src_embed readme-sample-createBasicClient -->
 * <pre>
 * HttpClient client = new OkHttpAsyncHttpClientBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createBasicClient  -->
 *
 * <hr>
 *
 * <h2><strong>Using OkHttpAsyncHttpClient with Http Proxy</strong></h2>
 *
 * <p>Configuring the OkHttp client with a proxy in the context of Azure Java SDK is relevant when your application needs
 * to communicate with Azure services through a proxy server. Proxies are commonly used for security, monitoring, and
 * other purposes.</p>
 *
 * <p>Here are some scenarios where you might need to configure the OkHttp client with a proxy:</p>
 *
 * <ul>
 *     <li>Corporate Network Environment: If your application is running in a corporate network that requires
 *     communication through a proxy server for internet access, you might need to configure the OkHttp client to use
 *     the proxy.</li>
 *
 *     <li>Security Requirements: In some cases, organizations enforce security policies that mandate communication
 *     through a proxy for monitoring, logging, or filtering purposes. Configuring the OkHttp client with a proxy helps
 *     in meeting these security requirements.</li>
 *
 *     <li>Restricted Network Access: If your application is deployed in an environment where direct internet access
 *     is restricted, but the proxy allows external communication, configuring the OkHttp client with a proxy becomes
 *     necessary.</li>
 *
 *     <li>Proxy for Azure Services: Some Azure services may require communication through a proxy, and configuring
 *     the OkHttp client appropriately becomes essential to establish connections.</li>
 *
 *     <li>Logging and Auditing: If your organization requires detailed logging or auditing of outgoing network
 *     requests, configuring the OkHttp client with a proxy allows you to capture and analyze the traffic passing
 *     through the proxy.</li>
 *
 * </ul>
 *
 * <p>The following code sample demonstrates the creation of a OkHttp HttpClient that is using a proxy.</p>
 *
 * <!-- src_embed com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder.proxy#ProxyOptions -->
 * <pre>
 * final String proxyHost = &quot;&lt;proxy-host&gt;&quot;; &#47;&#47; e.g. localhost
 * final int proxyPort = 9999; &#47;&#47; Proxy port
 * ProxyOptions proxyOptions = new ProxyOptions&#40;ProxyOptions.Type.HTTP,
 *         new InetSocketAddress&#40;proxyHost, proxyPort&#41;&#41;;
 * HttpClient client = new OkHttpAsyncHttpClientBuilder&#40;&#41;
 *         .proxy&#40;proxyOptions&#41;
 *         .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder.proxy#ProxyOptions  -->
 *
 * <hr>
 *
 * <h2><strong>Using OkHttpAsyncHttpClient with HTTP/2 Support</strong></h2>
 *
 * <p>Configuring the OkHttp client with HTTP/2 support is relevant when you want to take advantage of the benefits
 * provided by the HTTP/2 protocol. HTTP/2 is the next version of the HTTP network protocol, designed to improve the
 * performance of web applications by allowing multiple requests and responses to be multiplexed over a
 * single connection.</p>
 *
 * <p>Here are some scenarios where you might consider configuring the OkHttp client with HTTP/2 support:</p>
 *
 * <ul>
 *     <li>Improved Performance: HTTP/2 is designed to be more efficient than its predecessor, HTTP/1.1. It allows for
 *     the multiplexing of multiple streams over a single connection, reducing latency and improving overall
 *     performance. If you are working with Azure services that support HTTP/2, enabling it can lead to better
 *     performance.</li>
 *
 *     <li>Service Support: Some Azure services might support or even require communication using the HTTP/2 protocol.
 *     In such cases, configuring the OkHttp client with HTTP/2 support becomes necessary to establish connections to
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
 * <p>The following code sample demonstrates the creation of a OkHttp HttpClient that supports both the HTTP/1.1 and
 * HTTP/2 protocols, with HTTP/2 being the preferred protocol.</p>
 *
 * <!-- src_embed readme-sample-useHttp2WithConfiguredOkHttpClient -->
 * <pre>
 * &#47;&#47; Constructs an HttpClient that supports both HTTP&#47;1.1 and HTTP&#47;2 with HTTP&#47;2 being the preferred protocol.
 * &#47;&#47; This is the default handling for OkHttp.
 * HttpClient client = new OkHttpAsyncHttpClientBuilder&#40;new OkHttpClient.Builder&#40;&#41;
 *     .protocols&#40;Arrays.asList&#40;Protocol.HTTP_2, Protocol.HTTP_1_1&#41;&#41;
 *     .build&#40;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-useHttp2WithConfiguredOkHttpClient  -->
 *
 * <p>It is also possible to create a OkHttp HttpClient that only supports HTTP/2.</p>
 *
 * <!-- src_embed readme-sample-useHttp2OnlyWithConfiguredOkHttpClient -->
 * <pre>
 * &#47;&#47; Constructs an HttpClient that only supports HTTP&#47;2.
 * HttpClient client = new OkHttpAsyncHttpClientBuilder&#40;new OkHttpClient.Builder&#40;&#41;
 *     .protocols&#40;Collections.singletonList&#40;Protocol.H2_PRIOR_KNOWLEDGE&#41;&#41;
 *     .build&#40;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-useHttp2OnlyWithConfiguredOkHttpClient  -->
 *
 * @see com.azure.core.http.okhttp.OkHttpAsyncHttpClient
 * @see com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder
 * @see okhttp3.OkHttpClient
 */
package com.azure.core.http.okhttp;
