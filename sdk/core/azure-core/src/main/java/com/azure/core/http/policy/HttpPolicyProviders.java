// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.implementation.http.policy.InstrumentationPolicy;

import java.util.ArrayList;
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
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 * @see com.azure.core.http.policy.BeforeRetryPolicyProvider
 * @see com.azure.core.http.policy.AfterRetryPolicyProvider
 */
public final class HttpPolicyProviders {
    private static final String INVALID_POLICY = "HttpPipelinePolicy created with %s resulted in a null policy.";

    private static final List<BeforeRetryPolicyProvider> BEFORE_PROVIDER = new ArrayList<>();
    private static final List<AfterRetryPolicyProvider> AFTER_PROVIDER = new ArrayList<>();

    static {
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't depend on the
        // System classloader to load BeforeRetryPolicyProvider and AfterRetryPolicyProvider classes.
        ServiceLoader.load(BeforeRetryPolicyProvider.class, HttpPolicyProviders.class.getClassLoader())
            .forEach(BEFORE_PROVIDER::add);
        ServiceLoader.load(AfterRetryPolicyProvider.class, HttpPolicyProviders.class.getClassLoader())
            .forEach(AFTER_PROVIDER::add);
    }

    private HttpPolicyProviders() {
        // no-op
    }

    /**
     * Adds SPI policies that implement {@link BeforeRetryPolicyProvider}.
     *
     * @param policies Policy list to append the policies.
     */
    public static void addBeforeRetryPolicies(List<HttpPipelinePolicy> policies) {
        addPolices(policies, BEFORE_PROVIDER);
    }

    /**
     * Adds SPI policies that implement {@link AfterRetryPolicyProvider}.
     *
     * @param policies Policy list to append the policies.
     */
    public static void addAfterRetryPolicies(List<HttpPipelinePolicy> policies) {
        policies.add(new InstrumentationPolicy());
        addPolices(policies, AFTER_PROVIDER);
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
