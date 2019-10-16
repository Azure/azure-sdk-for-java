// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.serializer.jsonwrapper;

public class FooWithInner {
    private int intValue;
    private String stringValue;
    private InnerFoo innerFoo;

    public class InnerFoo {
        private String name;

        public String getName() {
            return name;
        }
    }

    public int getIntValue() {
        return intValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public InnerFoo getInnerFoo() {
        return innerFoo;
    }
}
