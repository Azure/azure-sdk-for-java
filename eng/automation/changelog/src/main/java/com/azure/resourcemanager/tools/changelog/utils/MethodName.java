// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.tools.changelog.utils;

import javassist.CtMethod;
import javassist.bytecode.Descriptor;

public class MethodName {
    public static String name(CtMethod method) {
        return method.getName() + Descriptor.toString(method.getSignature());
    }
}
