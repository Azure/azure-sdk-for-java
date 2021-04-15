// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public abstract class DocumentClientRetryPolicy implements IRetryPolicy {

    // TODO: this is just a place holder for now. As .Net has this method.
    // I have to spend more time figure out what's the right pattern for this (if method needed)

    /// <summary>
    /// Method that is called before a request is sent to allow the retry policy implementation
    /// to modify the state of the request.
    /// </summary>
    /// <param name="request">The request being sent to the service.</param>
    /// <remarks>
    /// Currently only read operations will invoke this method. There is no scenario for write
    /// operations to modify requests before retrying.
    /// </remarks>

    // TODO: I need to investigate what's the right contract here and/or if/how this is useful
    public abstract void onBeforeSendRequest(RxDocumentServiceRequest request);
}
