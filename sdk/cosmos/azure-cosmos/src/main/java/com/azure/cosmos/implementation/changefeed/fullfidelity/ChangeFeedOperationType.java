// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.fullfidelity;

import com.azure.cosmos.util.Beta;

@Beta(value = Beta.SinceVersion.V4_28_0)
public enum ChangeFeedOperationType {
    CREATE,
    REPLACE,
    DELETE
}
