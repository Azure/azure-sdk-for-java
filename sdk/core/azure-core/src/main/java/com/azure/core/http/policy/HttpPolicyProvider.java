// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.policy;

/**
 * Implementing classes automatically provide policies.
 */
public interface HttpPolicyProvider {

    /**
     * Creates the policy.
     * @return the policy that was created.
     */
    HttpPipelinePolicy create();
}
