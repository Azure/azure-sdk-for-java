// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

public enum BlobRequestConditionProperty {
    LEASE_ID,
    TAGS_CONDITIONS,
    IF_MODIFIED_SINCE,
    IF_UNMODIFIED_SINCE,
    IF_MATCH,
    IF_NONE_MATCH;
}
