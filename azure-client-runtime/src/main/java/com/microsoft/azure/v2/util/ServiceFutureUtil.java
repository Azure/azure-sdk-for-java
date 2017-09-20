package com.microsoft.azure.v2.util;

import com.microsoft.azure.v2.OperationStatus;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

public class ServiceFutureUtil {
    public static <T> ServiceFuture<T> fromLRO(Observable<OperationStatus<T>> observable, ServiceCallback<T> callback) {
        Single<T> single = observable.last().toSingle().map(new Func1<OperationStatus<T>, T>() {
            @Override
            public T call(OperationStatus<T> tOperationStatus) {
                return tOperationStatus.result();
            }
        });

        return ServiceFuture.fromBody(single, callback);
    }
}
