// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

///**
// * Copyright (c) Microsoft Corporation. All rights reserved.
// * Licensed under the MIT License. See License.txt in the project root for
// * license information.
// */
//package com.azure.management.resources;
//
//import com.azure.resourcemanager.resources.fluentcore.arm.Region;
//import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
//import com.microsoft.rest.RestClient;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import rx.Observable;
//import rx.Subscriber;
//import rx.functions.Func1;
//import rx.schedulers.Schedulers;
//
//public class TooManyRequestsRetryInterceptorTests extends ResourceManagerTestBase {
//    private static ResourceGroups resourceGroups;
//
//    private String rgName;
//    private String testId;
//    private ResourceGroup rg;
//
//    @Override
//    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
//        testId = SdkContext.randomResourceName("", 9);
//        rgName = "rg429" + testId;
//
//        super.initializeClients(restClient, defaultSubscription, domain);
//        resourceGroups = resourceClient.getResourceGroups();
//
//        rg = resourceGroups.define(rgName)
//            .withRegion(Region.US_EAST)
//            .create();
//    }
//
//    @Override
//    protected void cleanUpResources() {
//        resourceGroups.beginDeleteByName(rgName);
//    }
//
//    @Disabled("Not for every testing")
//    public void canGenerate429() throws Exception {
//        Observable.range(1, 1250).flatMap(new Func1<Integer, Observable<Void>>() {
//            @Override
//            public Observable<Void> call(final Integer iteration) {
//                return Observable.create(new Observable.OnSubscribe<Void>() {
//                    @Override
//                    public void call(Subscriber<? super Void> subscriber) {
//                        System.out.format("Current time for %d is: %d\n", iteration, System.currentTimeMillis());
//                        rg.update().apply();
//                        subscriber.onCompleted();
//                    }
//                });
//            }
//        }, 10)
//            .subscribeOn(Schedulers.io()).toBlocking().subscribe();
//    }
//}
