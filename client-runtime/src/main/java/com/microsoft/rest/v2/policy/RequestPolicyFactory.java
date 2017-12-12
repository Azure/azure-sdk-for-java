/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

/**
 * Factory to create a RequestPolicy. RequestPolicies are instantiated per-request
 * so that they can contain instance state specific to that request/response exchange,
 * for example, the number of retries attempted so far in a counter.
 */
public interface RequestPolicyFactory {
    /**
     * Creates RequestPolicy.
     *
     * @param next the next RequestPolicy in the request-response pipeline.
     * @param options The optional arguments that can be passed to a RequestPolicy.
     * @return the created RequestPolicy
     */
    RequestPolicy create(RequestPolicy next, RequestPolicyOptions options);
}