// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.tools.changelog.utils;

import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.bytecode.Descriptor;

public class MethodName {
    public static String name(CtMethod method) {
        return method.getName() + Descriptor.toString(method.getSignature());
    }

    public static String name(CtConstructor constructor) {
        return constructor.getName() + Descriptor.toString(constructor.getSignature());
    }
}
