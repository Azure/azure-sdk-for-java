// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosDiagnostics;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public interface IRetryPolicyFactory {
    DocumentClientRetryPolicy getRequestPolicy();
    RetryContext getRetryContext();
}
