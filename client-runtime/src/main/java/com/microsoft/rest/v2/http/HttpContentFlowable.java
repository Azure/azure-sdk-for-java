package com.microsoft.rest.v2.http;

import io.reactivex.Flowable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

// TODO: this is apparently the proper solution, not doOnRequest (?)
public class HttpContentFlowable extends Flowable {

    @Override
    protected void subscribeActual(Subscriber s) {
        s.onSubscribe(new Subscription() {
            @Override
            public void request(long l) {

            }

            @Override
            public void cancel() {

            }
        });
    }
}
