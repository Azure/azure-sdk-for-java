// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import java.io.Serializable;

interface IEqualityComparer<TValue> extends Serializable {
    boolean areEqual(TValue v1, TValue v2);
}
