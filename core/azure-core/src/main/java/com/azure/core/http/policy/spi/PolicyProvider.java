// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.policy.spi;

import com.azure.core.http.policy.HttpPipelinePolicy;

// TODO
public interface PolicyProvider {

    /**
     * Creates the policy.
     * @return the policy that was created.
     */
    HttpPipelinePolicy create();
}
