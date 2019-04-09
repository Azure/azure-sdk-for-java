package com.azure.common.implementation;

import com.azure.common.exception.ServiceRequestException;

import java.lang.reflect.Method;

public class UnexpectedException {
    private Class<? extends ServiceRequestException> exceptionType;
    private Class<?> exceptionBodyType;

    public UnexpectedException(Class<? extends ServiceRequestException> exceptionType) {
        this.exceptionType = exceptionType;

        try {
            final Method exceptionBodyMethod = exceptionType.getDeclaredMethod("value");
            this.exceptionBodyType = exceptionBodyMethod.getReturnType();
        } catch (NoSuchMethodException e) {
            // Should always have a value() method. Register Object as a fallback plan.
            this.exceptionBodyType = Object.class;
        }
    }

    public Class<? extends ServiceRequestException> exceptionType() {
        return exceptionType;
    }

    public Class<?> exceptionBodyType() {
        return exceptionBodyType;
    }
}
