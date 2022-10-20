package com.azure.json.reflect;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodType.methodType;

class MetaFactoryFactory {
    private MetaFactoryFactory() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("unchecked")
    static <T> T createMetaFactory(String methodName, Class<?> implClass, MethodType implType, Class<T> invokedClass, MethodType invokedType, MethodHandles.Lookup lookup) throws Throwable {
        MethodHandle handle = lookup.findVirtual(implClass, methodName, implType);
        return (T) LambdaMetafactory.metafactory(lookup, methodName, methodType(invokedClass), invokedType, handle, handle.type()).getTarget().invoke();
    }
}
