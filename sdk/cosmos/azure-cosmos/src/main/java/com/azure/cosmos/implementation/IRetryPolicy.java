// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import reactor.core.publisher.Mono;

// TODO update documentation
/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public interface IRetryPolicy {
    // this capture all the retry logic
    // TODO: design decision should this return a single or an observable?

    /// <summary>
    /// Method that is called to determine from the policy that needs to retry on the exception
    /// </summary>
    /// <param name="exception">Exception during the callback method invocation</param>
    /// <param name="cancellationToken"></param>
    /// <returns>If the retry needs to be attempted or not</returns>
    Mono<ShouldRetryResult> shouldRetry(Exception e);

    RetryContext getRetryContext();
}
