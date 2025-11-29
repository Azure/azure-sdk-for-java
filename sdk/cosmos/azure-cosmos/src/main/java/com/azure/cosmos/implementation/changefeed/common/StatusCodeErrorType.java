// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

/**
 * Groups types of status code errors returned while processing the change feeds.
 */
public enum StatusCodeErrorType {
    UNDEFINED,
    PARTITION_NOT_FOUND,
    PARTITION_SPLIT_OR_MERGE,
    TRANSIENT_ERROR,
    MAX_ITEM_COUNT_TOO_LARGE,
    JACKSON_STREAMS_CONSTRAINED,
    JSON_PARSING_ERROR
}
