// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>
 * <a href="https://learn.microsoft.com/en-us/java/api/overview/azure/core-http-okhttp-readme?view=azure-java-stable">
 * Azure Core Http OkHttp</a> client library is a plugin for the azure-core HTTP client API. It allows you to use OkHttp
 * as the underlying HTTP client for communicating with Azure services. OkHttp is a popular and efficient HTTP client
 * that supports features such as HTTP/2, connection pooling, compression, and caching. To use the OkHttp client
 * library,
 * you need to include the dependency in your project and configure it when creating a service client.
 * For more details refer to our
 * <a href="https://learn.microsoft.com/azure/developer/java/sdk/http-client-pipeline#http-clients">conceptual
 * documentation</a>.
 * </p>
 *
 * <p>
 * <strong>Sample: Construct OkHttpAsyncHttpClient with Default Configuration</strong>
 * </p>
 *
 * <p>
 * The following code sample demonstrates the creation of a OkHttp HttpClient that uses port 80 and has no proxy.
 * </p>
 *
 * <!-- src_embed readme-sample-createBasicClient -->
 * <pre>
 * HttpClient client = new OkHttpAsyncHttpClientBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createBasicClient -->
 *
 * <hr>
 *
 * <h2><strong>Using OkHttpAsyncHttpClient with Http Proxy</strong></h2>
 *
 * <p>
 * Configuring the OkHttp client with a proxy in the context of Azure Java SDK is relevant when your application needs
 * to communicate with Azure services through a proxy server. For more details refer to our
 * <a href="https://learn.microsoft.com/azure/developer/java/sdk/proxying#http-proxy-configuration">conceptual
 * documentation</a>.
 * </p>
 *
 * <p>
 * The following code sample demonstrates the creation of a OkHttp HttpClient that is using a proxy.
 * </p>
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
 * <!-- end com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder.proxy#ProxyOptions -->
 *
 * <hr>
 *
 * <h2><strong>Using OkHttpAsyncHttpClient with HTTP/2 Support</strong></h2>
 *
 * <p>
 * The following code sample demonstrates the creation of a OkHttp HttpClient that supports both the HTTP/1.1 and
 * HTTP/2 protocols, with HTTP/2 being the preferred protocol.
 * </p>
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
 * <!-- end readme-sample-useHttp2WithConfiguredOkHttpClient -->
 *
 * <p>
 * It is also possible to create a OkHttp HttpClient that only supports HTTP/2.
 * </p>
 *
 * <!-- src_embed readme-sample-useHttp2OnlyWithConfiguredOkHttpClient -->
 * <pre>
 * &#47;&#47; Constructs an HttpClient that only supports HTTP&#47;2.
 * HttpClient client = new OkHttpAsyncHttpClientBuilder&#40;new OkHttpClient.Builder&#40;&#41;
 *     .protocols&#40;Collections.singletonList&#40;Protocol.H2_PRIOR_KNOWLEDGE&#41;&#41;
 *     .build&#40;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-useHttp2OnlyWithConfiguredOkHttpClient -->
 *
 * @see com.azure.core.http.okhttp.OkHttpAsyncHttpClient
 * @see com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder
 * @see okhttp3.OkHttpClient
 */
package com.azure.core.http.okhttp;
