// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

/**
 * Groups types of status code errors returned while processing the change feeds.
 */
enum StatusCodeErrorType {
    UNDEFINED,
    PARTITION_NOT_FOUND,
    PARTITION_SPLIT,
    TRANSIENT_ERROR,
    MAX_ITEM_COUNT_TOO_LARGE
}
