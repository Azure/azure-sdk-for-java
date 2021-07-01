// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import java.io.Serializable;
import java.util.function.Function;

public interface SerializableWrapper<T> extends Serializable {
    T getWrappedItem();
}
