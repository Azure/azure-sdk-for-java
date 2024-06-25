// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.http.policy;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The {@code HttpPolicyProviders} class is responsible for adding Service Provider Interface (SPI) pluggable policies
 * to an HTTP pipeline automatically.
 *
 * <p>This class is useful when you need to add custom policies to the HTTP pipeline that are loaded using Java's
 * {@link ServiceLoader}. It provides methods to add policies before and after the retry policy in the pipeline.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, an empty list of policies is created. Then, the
 * {@code HttpPolicyProviders.addBeforeRetryPolicies} method is used to add policies that should be executed before
 * the retry policy. The {@code HttpPolicyProviders.addAfterRetryPolicies} method is used to add policies that should
 * be executed after the retry policy. The list of policies can then be used to build an HTTP pipeline.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.HttpPolicyProviders.usage -->
 * <pre>
 * List&lt;HttpPipelinePolicy&gt; policies = new ArrayList&lt;&gt;&#40;&#41;;
 * &#47;&#47; Add policies that should be executed before the retry policy
 * HttpPolicyProviders.addBeforeRetryPolicies&#40;policies&#41;;
 * &#47;&#47; Add the retry policy
 * policies.add&#40;new RetryPolicy&#40;&#41;&#41;;
 * &#47;&#47; Add policies that should be executed after the retry policy
 * HttpPolicyProviders.addAfterRetryPolicies&#40;policies&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.HttpPolicyProviders.usage -->
 *
 * @see com.azure.core.http.policy
 * @see HttpPipelinePolicy
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 */
public final class HttpPolicyProviders {
    private static final String INVALID_POLICY = "HttpPipelinePolicy created with %s resulted in a null policy.";

    private HttpPolicyProviders() {
        // no-op
    }

    private static void addPolices(List<HttpPipelinePolicy> policies, List<? extends HttpPolicyProvider> providers) {
        for (HttpPolicyProvider provider : providers) {
            HttpPipelinePolicy policy = provider.create();
            if (policy == null) {
                throw new NullPointerException(String.format(INVALID_POLICY, provider.getClass()));
            }

            policies.add(policy);
        }
    }
}
