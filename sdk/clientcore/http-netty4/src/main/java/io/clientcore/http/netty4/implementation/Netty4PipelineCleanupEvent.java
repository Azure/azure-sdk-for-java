// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

public enum Netty4PipelineCleanupEvent {

    /**
     * Event indicating that the channel will be released back to the connection pool.
     */
    CLEANUP_PIPELINE
}
