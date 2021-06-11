// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Hack {
    private static final Unsafe UNSAFE;
    static
    {
        try
        {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe)field.get(null);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void updateStaticField(Class clazz, String name, Object value) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateFinalField(Object target, String name, Object value) {
        try {
            Class clazz = target.getClass();
            Field field = clazz.getDeclaredField(name);
            long offset = UNSAFE.objectFieldOffset(field);
            UNSAFE.putObject(target, offset, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
