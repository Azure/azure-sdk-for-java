// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

public enum ChangeFeedStartFromTypes {
    BEGINNING,
    NOW,
    POINT_IN_TIME,
    LEASE
}
