// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api;

/**
 * Whether the consumer receives messages from the beginning or end of destination
 *
 * @author Warren Zhu
 */
public enum StartPosition {

    /**
     * Consumer receives messages from the beginning of destination
     */
    EARLIEST,

    /**
     * Consumer receives messages from the end of destination
     */
    LATEST
}
