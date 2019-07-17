// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

@FunctionalInterface
public interface Function2WithCheckedException<T1, T2, R>{
     R apply(T1 t1, T2 t2) throws Exception;

}
