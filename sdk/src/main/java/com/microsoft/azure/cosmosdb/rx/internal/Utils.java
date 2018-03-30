/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class Utils {

    public static int getValueOrDefault(Integer val, int defaultValue) {
        return val != null ? val.intValue() : defaultValue;
    }

    // TODO: the pattern in RX is to treat exception as first class citizen.
    //       So in Rx we should return errors instead of throwing exception.
    //       Think about if we need both methods with throwing exception and returning exception. and change the implementation to use the proper one

    public static void checkStateOrThrow(boolean value, String argumentName, String message) throws IllegalArgumentException {

        IllegalArgumentException t = checkStateOrReturnException(value, argumentName, message);
        if (t != null) {
            throw t;
        }
    }

    public static void checkNotNullOrThrow(Object val, String argumentName, String message) throws NullPointerException {

        NullPointerException t = checkNotNullOrReturnException(val, argumentName, message);
        if (t != null) {
            throw t;
        }
    }

    public static void checkStateOrThrow(boolean value, String argumentName, String messageTemplate, Object... messageTemplateParams) throws IllegalArgumentException {
        IllegalArgumentException t = checkStateOrReturnException(value, argumentName, argumentName, messageTemplateParams);
        if (t != null) {
            throw t;
        }
    }

    public static IllegalArgumentException checkStateOrReturnException(boolean value, String argumentName, String message) {

        if (value) {
            return null;
        }

        return new IllegalArgumentException(String.format("argumentName: %s, message: %s", argumentName, message));
    }

    public static IllegalArgumentException checkStateOrReturnException(boolean value, String argumentName, String messageTemplate, Object... messageTemplateParams) {
        if (value) {
            return null;
        }

        return new IllegalArgumentException(String.format("argumentName: %s, message: %s", argumentName, String.format(messageTemplate, messageTemplateParams)));
    }

    private static NullPointerException checkNotNullOrReturnException(Object val, String argumentName, String messageTemplate, Object... messageTemplateParams) {
        if (val != null) {
            return null;
        }

        return new NullPointerException(String.format("argumentName: %s, message: %s", argumentName, String.format(messageTemplate, messageTemplateParams)));
    }

    public static BadRequestException checkRequestOrReturnException(boolean value, String argumentName, String message) {

        if (value) {
            return null;
        }

        return new BadRequestException(String.format("argumentName: %s, message: %s", argumentName, message));
    }

    public static BadRequestException checkRequestOrReturnException(boolean value, String argumentName, String messageTemplate, Object... messageTemplateParams) {
        if (value) {
            return null;
        }

        return new BadRequestException(String.format("argumentName: %s, message: %s", argumentName, String.format(messageTemplate, messageTemplateParams)));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T as(Throwable e, Class<T> klass) {
        if (e == null) {
            return null;
        }

        if (klass.isInstance(e)) {
            return (T) e;
        } else {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <V> List<V> immutableListOf() {
        return Collections.EMPTY_LIST;
    }
    
    public static <V> List<V> immutableListOf(V v1) {
        List<V> list = new ArrayList<>();
        list.add(v1);
        return Collections.unmodifiableList(list);
    }
    
    public static <K, V> Map<K, V>immutableMapOf() {
        return Collections.emptyMap();
    }
    
    public static <K, V> Map<K, V>immutableMapOf(K k1, V v1) {
        Map<K, V> map = new HashMap<K ,V>();
        map.put(k1,  v1);
        map = Collections.unmodifiableMap(map);
        return map;
    }
}
