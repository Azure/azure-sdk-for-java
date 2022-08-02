package com.azure.spring.cloud.service.implementation.identity.impl.utils;

import com.azure.core.exception.AzureException;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProvider;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProviderOptions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ClassUtil {

    private ClassUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T instantiateClass(Class<T> clazz, Object... args) {
        try {
            // TODO not use this paramater class
            Constructor<?> ctor = clazz.getDeclaredConstructor(TokenCredentialProviderOptions.class);
            ctor.setAccessible(true);
            // TODO pass the args or call default ctor
            return (T) ctor.newInstance(args);
        } catch (InstantiationException e) {
            // TODO
            throw new AzureException(e);
        } catch (IllegalAccessException e) {
            throw new AzureException(e);
        } catch (InvocationTargetException e) {
            throw new AzureException(e);
        } catch (NoSuchMethodException e) {
            throw new AzureException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(String className, Class<TokenCredentialProvider> assignableClass) {
        if (className != null && !className.isEmpty()) {
            try {
                Class<?> clazz = Class.forName(className);
                if (!assignableClass.isAssignableFrom(clazz)) {
                    throw new AzureException("Provided class [" + className + "] is not a [ " + assignableClass.getSimpleName() + "]");
                }
                return (Class<T>) clazz;
            } catch (ClassNotFoundException e) {
                throw new AzureException("The provided class [" + className + "] can't be found", e);
            }
        }
        return null;
    }
}
