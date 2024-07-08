// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * This package contains the HttpPipelinePolicy interface and its implementations. These policies are used to form an
 * HTTP pipeline, which is a series of policies that are invoked to handle an HTTP request.
 *
 * <p>The HttpPipelinePolicy interface defines process and processSync methods. These
 * methods transform an HTTP request into an HttpResponse asynchronously and synchronously respectively.
 * Implementations of this interface can modify the request, pass it to the next policy, and then modify the response.</p>
 *
 * <p><strong>Code Sample:</strong></p>
 *
 * <p>In this example, the UserAgentPolicy, RetryPolicy, and CustomPolicy are added to the pipeline. The pipeline is
 * then used to send an HTTP request, and the response is retrieved.</p>
 *
 * <pre>
 * {@code
 * HttpPipeline pipeline = new HttpPipelineBuilder()
 *     .policies(new UserAgentPolicy(), new RetryPolicy(), new CustomPolicy())
 *     .build();
 *
 * HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://example.com"));
 * Response<?> response = pipeline.send(request).block();
 * }
 * </pre>
 *
 * <p>This package is crucial for the communication between Azure SDK client libraries and Azure services. It provides
 * a layer of abstraction over the HTTP protocol, allowing client libraries to focus on service-specific logic.</p>
 *
 * @see HttpPipelinePolicy
 * @see com.azure.core.http.policy.HttpLogDetailLevel
 * @see com.azure.core.http.policy.HttpLogOptions
 * @see com.azure.core.http.policy.HttpLoggingPolicy
 * @see HttpPipelinePolicy
 * @see com.azure.core.http.policy.RetryPolicy
 * @see com.azure.core.http.policy.UserAgentPolicy
 */
package com.azure.core.v2.http.policy;
