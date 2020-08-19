// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.core;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.fluent.ResourceGroupsClient;
import com.azure.resourcemanager.resources.fluent.inner.ResourceGroupInner;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Http Pipeline Policy for tagging resource groups created in tests.
 */
public class ResourceGroupTaggingPolicy implements HttpPipelinePolicy {
//    private static final String LOGGING_CONTEXT = "com.microsoft.azure.management.resources.ResourceGroups createOrUpdate";
    private static final String CALLER_METHOD = String.format("%s$ResourceGroupsService.createOrUpdate", ResourceGroupsClient.class.getName());
    private AzureJacksonAdapter adapter = new AzureJacksonAdapter();

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("PUT".equals(context.getHttpRequest().getHttpMethod().name())
                && context.getHttpRequest().getUrl().toString().contains("/resourcegroups/")
                && CALLER_METHOD.equals(context.getData("caller-method").orElse("").toString())) {
            return context.getHttpRequest().copy().getBody().flatMap(
                byteBuffer -> {
                    byte[] body = new byte[byteBuffer.remaining()];
                    byteBuffer.get(body);
                    String bodyStr = new String(body, StandardCharsets.UTF_8);

                    ResourceGroupInner resourceGroupInner;
                    try {
                        resourceGroupInner = adapter.deserialize(bodyStr, ResourceGroupInner.class, SerializerEncoding.JSON);
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                    if (resourceGroupInner == null) {
                        return Mono.error(new RuntimeException("Failed to deserialize " + bodyStr));
                    }

                    Map<String, String> tags = resourceGroupInner.tags();
                    if (tags == null) {
                        tags = new HashMap<>();
                    }
                    tags.put("product", "javasdk");
                    tags.put("cause", "automation");
                    tags.put("date", Instant.now().toString());
                    if (System.getenv("ENV_JOB_NAME") != null) {
                        tags.put("job", System.getenv("ENV_JOB_NAME"));
                    }
                    resourceGroupInner.withTags(tags);
                    String newBody;
                    try {
                        newBody = adapter.serialize(resourceGroupInner, SerializerEncoding.JSON);
                    } catch (IOException e) {
                        return Mono.error(e);
                    }

                    if (newBody == null) {
                        return Mono.error(new RuntimeException("Failed to serialize after resource group tagging.\nOriginal body: " + bodyStr));
                    }

                    HttpRequest newRequest = context.getHttpRequest().copy()
                            .setBody(newBody)
                            .setHeader("Content-Length", String.valueOf(newBody.length()));
                    context.setHttpRequest(newRequest);
                    return next.process();
                }
            ).last();
        }
        return next.process();
    }
}
