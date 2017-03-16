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
     * Shortcut for mapping the output of an arbitrary observable to one returning an instance of a specific type, using the IO scheduler.
     * @param fromObservable the source observable
     * @param toValue the value to emit to the observer
     * @param <T> the type of the value to emit
     * @return an observable emitting the specified value
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
     * Shortcut for mapping an arbitrary observable to void, using the IO scheduler.
     * @param fromObservable the source observable
     * @return a void-emitting observable
     */
    public static Observable<Void> mapToVoid(Observable<?> fromObservable) {
        if (fromObservable != null) {
            return fromObservable.subscribeOn(Schedulers.io())
                    .map(new RXMapper<Void>());
        } else {
            return Observable.empty();
        }
    }

    /**
     * @param value the value to emit
     */
    private RXMapper(T value) {
        this.value = value;
    }

    /**
     * Void emitting mapper.
     */
    private RXMapper() {
        this.value = null;
    }

    @Override
    public T call(Object t) {
        return this.value;
    }
}
