// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.policy;

import com.azure.core.http.policy.HttpPipelinePolicy;

/**
 * Implementing classes automatically provide policies.
 */
public interface PolicyProvider {

    /**
     * Creates the policy.
     * @return the policy that was created.
     */
    HttpPipelinePolicy create();
}
