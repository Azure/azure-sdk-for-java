// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.policy;

/**
 * Implementing classes are automatically added as policies after the retry policy.
 */
public interface AfterRetryPolicyProvider extends HttpPolicyProvider {
}
