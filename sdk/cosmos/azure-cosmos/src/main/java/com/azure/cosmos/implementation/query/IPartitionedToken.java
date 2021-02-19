// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.routing.Range;

public interface IPartitionedToken {
    Range<String> getRange();
}
