// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

@FunctionalInterface
public interface Function1WithCheckedException<T, R>{

     R apply(T t) throws Exception;

}
