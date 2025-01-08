// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import java.lang.reflect.Method;
import java.util.Objects;

class TestPropertyGroup {
    private Method getMethod;
    private Method setMethod;
    private Object testValue;

    TestPropertyGroup(Method setMethod, Method getMethod, Object testValue) {
        this.getMethod = getMethod;
        this.setMethod = setMethod;
        this.testValue = testValue;
    }

    public Method getGetMethod() {
        return getMethod;
    }

    public void setGetMethod(Method getMethod) {
        this.getMethod = getMethod;
    }

    public Method getSetMethod() {
        return setMethod;
    }

    public void setSetMethod(Method setMethod) {
        this.setMethod = setMethod;
    }

    public Object getTestValue() {
        return testValue;
    }

    public void setTestValue(Object testValue) {
        this.testValue = testValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestPropertyGroup that = (TestPropertyGroup) o;
        return Objects.equals(getMethod.getName(), that.getMethod.getName())
            && Objects.equals(setMethod.getName(), that.setMethod.getName())
            && Objects.equals(testValue, that.testValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMethod.getName(), setMethod.getName(), testValue);
    }
}
