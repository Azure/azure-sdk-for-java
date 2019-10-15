// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.serializer.jsonwrapper.models;

public class FooWithInner {
    public int intValue;
    public String stringValue;
    public InnerFoo innerFoo;

    public class InnerFoo {
        public String name;
    }
}
