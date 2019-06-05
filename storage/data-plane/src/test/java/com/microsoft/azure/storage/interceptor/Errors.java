// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.interceptor;

import java.io.Serializable;

public class Errors implements Serializable {
    public Class<?> classType;
    public Class<?>[] types;
    public Object[] args;
}
