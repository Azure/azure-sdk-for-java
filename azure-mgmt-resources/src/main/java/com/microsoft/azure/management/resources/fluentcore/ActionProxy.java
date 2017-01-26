/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore;

import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Class to handle async function handling.
 * @param <T> IMPL class type.
 */
public final class ActionProxy<T> implements InvocationHandler {
    private T impl;

    private ActionProxy(T impl) {
        this.impl = impl;
    }

    /**
     * Proxy function.
     * @param proxy proxy object
     * @param method method for which call is
     * @param args arguments for the method call
     * @return output of the function call.
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();

        try {
            impl.getClass().getMethod(methodName, parameterTypes);
            return method.invoke(impl, args);
        }
        catch (NoSuchMethodException e) {
            // Continue with flow below.
        }

        if (!method.getName().contains("Async")) {
            methodName += "Async";
        }
        if (parameterTypes.length > 0 && parameterTypes[parameterTypes.length - 1].getName().contains("Callback")) {
            parameterTypes = Arrays.copyOfRange(parameterTypes, 0, parameterTypes.length - 1);
        }
        Method implMethod = impl.getClass().getMethod(methodName, parameterTypes);
        implMethod.setAccessible(true);

        if (method.getName().contains("Async")
                && (method.getReturnType().getName().contains("Observable")
                || method.getReturnType().getName().contains("Completable"))) {
            return implMethod.invoke(impl, args);
        } else if (method.getName().contains("Async") && method.getReturnType().getName().contains("ServiceCall")) {
            Object[] newArgs = null;
            if (args.length > 1) {
                newArgs = new Object[args.length - 1];
                for (int index = 0; index < args.length - 1; index++) {
                    newArgs[index] = args[index];
                }
            }
            return ServiceCall.fromBody((Observable<Object>) implMethod.invoke(impl, newArgs), (ServiceCallback<Object>) args[args.length - 1]);
        } else if (!method.getName().contains("Async")) {
            Observable<?> observable = (Observable<?>) implMethod.invoke(impl, args);
            return observable.toBlocking().last();
        } else {
            throw new NoSuchMethodException("Unknown method " + method.getName());
        }
    }

    /**
     * Method to convert impl into full interface.
     * @param fullInterfaceClass class information for full interface.
     * @param impl impl class to wrap.
     * @param <BaseImpl> Type of impl class
     * @param <FullInterface> type of the full interface.
     * @return object of the full interface.
     */
    public static <BaseImpl, FullInterface> FullInterface newInstance(Class<FullInterface> fullInterfaceClass, BaseImpl impl) {
        return (FullInterface) Proxy.newProxyInstance(fullInterfaceClass.getClassLoader(),
                new Class[] {
                        fullInterfaceClass
                }, new ActionProxy<>(impl));
    }
}
