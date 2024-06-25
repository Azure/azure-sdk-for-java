// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>This package provides HTTP abstractions for Azure SDK client libraries. It serves as a bridge between the
 * AnnotationParser, RestProxy, and the HTTP client.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *     <li>AnnotationParser: Interprets annotations on interface definitions and methods to construct HTTP requests.</li>
 *     <li>RestProxy: Transforms interface definitions into live implementations that convert method invocations into
 *     network calls.</li>
 *     <li>HTTP client: Sends HTTP requests and receives responses.</li>
 * </ul>
 *
 * <p>The HTTP pipeline is a series of policies that are invoked to handle an HTTP request. Each policy is a piece of
 * code that takes an HTTP request, does some processing, and passes the request to the next policy in the pipeline.
 * The last policy in the pipeline would then actually send the HTTP request.</p>
 *
 * <p>Users can create a custom pipeline by creating their own policies and adding them to the pipeline.
 * Here's a code sample:</p>
 *
 * <pre>
 * HttpPipeline pipeline = new HttpPipelineBuilder()
 *     .policies(new UserAgentPolicy(), new RetryPolicy())
 *     .build();
 * </pre>
 *
 * <p>This package is crucial for the communication between Azure SDK client libraries and Azure services.
 * It provides a layer of abstraction over the HTTP protocol, allowing client libraries to focus on service-specific
 * logic. Custom pipelines can be helpful when you want to customize the behavior of HTTP requests and responses in
 * some way, such as, to add a custom header to all requests.</p>
 *
 * @see com.azure.core.http.HttpClient
 * @see HttpRequest
 * @see Response
 * @see HttpPipeline
 * @see com.azure.core.http.HttpHeaders
 * @see com.azure.core.http.HttpMethod
 */
package com.azure.core.v2.http;
