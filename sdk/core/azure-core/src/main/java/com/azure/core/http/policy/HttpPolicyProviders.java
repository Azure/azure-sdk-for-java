// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.policy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * This class handles adding SPI plug-able policies to a pipeline automatically.
 */
public final class HttpPolicyProviders {

    private static final Map<Class<? extends HttpPolicyProvider>, ServiceLoader<? extends HttpPolicyProvider>>
        SERVICE_LOADERS = new HashMap<>();

    private HttpPolicyProviders() {
        // no-op
    }

    /**
     * Adds SPI policies that implement {@link BeforeRetryPolicyProvider}.
     *
     * @param policies Policy list to append the policies.
     */
    public static void addBeforeRetryPolicies(List<HttpPipelinePolicy> policies) {
        addRetryPolicies(policies, () -> getPolicyProviders(false, BeforeRetryPolicyProvider.class));
    }

    /**
     * Adds SPI policies that implement {@link AfterRetryPolicyProvider}.
     *
     * @param policies Policy list to append the policies.
     */
    public static void addAfterRetryPolicies(List<HttpPipelinePolicy> policies) {
        addRetryPolicies(policies, () -> getPolicyProviders(false, AfterRetryPolicyProvider.class));
    }

    private static void addRetryPolicies(List<HttpPipelinePolicy> policies,
        Supplier<Iterator<? extends HttpPolicyProvider>> policySupplier) {
        Iterator<? extends HttpPolicyProvider> it = policySupplier.get();
        while (it.hasNext()) {
            HttpPolicyProvider policyProvider = it.next();
            HttpPipelinePolicy policy = policyProvider.create();
            if (policy == null) {
                throw new NullPointerException("HttpPipelinePolicy created with " + policyProvider.getClass()
                    + " resulted in a null policy");
            }
            policies.add(policy);
        }
    }

    private static Iterator<? extends HttpPolicyProvider> getPolicyProviders(boolean reload,
        Class<? extends HttpPolicyProvider> cls) {
        ServiceLoader<? extends HttpPolicyProvider> serviceLoader = SERVICE_LOADERS
            .computeIfAbsent(cls, ServiceLoader::load);

        if (reload) {
            serviceLoader.reload();
        }

        return serviceLoader.iterator();
    }
}
