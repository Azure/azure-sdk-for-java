/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.utils;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * An internal utility class representing an RX function returning the provided type instance
 * from a call with an arbitrary parameter.
 * @param <T> the type to emit as Observable
 */
public final class RXMapper<T> implements Func1<Object, T> {
    private final T value;

    /**
     * Shortcut for mapping an arbitrary observable to one returning an instance of a specific type, using the IO scheduler.
     * @param fromObservable an observable
     * @param toValue the value to emit to the observer
     * @param <T> the type of the value to emit
     * @return an observable to be emitted
     */
    public static <T> Observable<T> map(Observable<?> fromObservable, final T toValue) {
        if (fromObservable != null) {
            return fromObservable.subscribeOn(Schedulers.io())
                    .map(new RXMapper<T>(toValue));
        } else {
            return Observable.empty();
        }
    }

    /**
     * @param s the string to return
     */
    private RXMapper(T value) {
        this.value = value;
    }

    @Override
    public T call(Object t) {
        return this.value;
    }
}
