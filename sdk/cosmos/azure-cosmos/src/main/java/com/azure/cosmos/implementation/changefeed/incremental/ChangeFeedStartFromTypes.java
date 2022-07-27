// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.incremental;

public enum ChangeFeedStartFromTypes {
    BEGINNING,
    NOW,
    POINT_IN_TIME,
    LEASE,
    LEGACY_CHECKPOINT
}
