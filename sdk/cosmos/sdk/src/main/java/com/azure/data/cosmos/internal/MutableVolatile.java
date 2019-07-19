// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

public class MutableVolatile<T> {

    public MutableVolatile(T initValue){
        v = initValue;
    }

    public MutableVolatile() {}
    public volatile T v;
}
