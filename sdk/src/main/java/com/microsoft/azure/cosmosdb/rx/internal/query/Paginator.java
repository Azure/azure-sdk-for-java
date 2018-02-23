/**
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx.internal.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.ChangeFeedOptions;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedOptionsBase;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class Paginator {

    private final static Logger logger = LoggerFactory.getLogger(Paginator.class);

    // TODO FIXME NOTE:
    // This code is taken form Srinath work on read feed.
    // I have a few concerns about this this
    // 1) how does this work with other schedulers? this may cause back-pressure or out of memory exception with non default scheduler
    // 2) how are we going to support back pressure?
    // to @Strinath please spend some time investigating the above questions
    
    public static <T extends Resource> Observable<FeedResponse<T>> getPatinatedChangeFeedQueryResultAsObservable(ChangeFeedOptions feedOptions,
            Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc, Class<T> resourceType,
            int maxPageSize) {
        return getPatinatedQueryResultAsObservable(feedOptions, createRequestFunc, executeFunc, resourceType,
                -1, maxPageSize, true);
    }
    
    public static <T extends Resource> Observable<FeedResponse<T>> getPatinatedQueryResultAsObservable(FeedOptions feedOptions,
            Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc, Class<T> resourceType,
            int maxPageSize) {
        return getPatinatedQueryResultAsObservable(feedOptions, createRequestFunc, executeFunc, resourceType,
                -1, maxPageSize);
    }
    
    public static <T extends Resource> Observable<FeedResponse<T>> getPatinatedQueryResultAsObservable(
            FeedOptions options,
            Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc, Class<T> resourceType,
            int top, int maxPageSize) {
        return getPatinatedQueryResultAsObservable(options, createRequestFunc, executeFunc, resourceType,
                top, maxPageSize, false);
    }
    
    private static <T extends Resource> Observable<FeedResponse<T>> getPatinatedQueryResultAsObservable(
            FeedOptionsBase options,
            Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc, Class<T> resourceType,
            int top, int maxPageSize, boolean isChangeFeed) {

        return Observable.defer(() -> {
            try {

                logger.debug("Querying/Reading " + resourceType + "s");
                
                int requestPageSize = Math.min(maxPageSize, top == -1 ? maxPageSize : top);
                
                RxDocumentServiceRequest firstRequest = createRequestFunc.call(options.getRequestContinuation(), requestPageSize);
                return executeFunc.call(firstRequest).single().concatMap(firstPage -> {

                    logger.debug("got first page");

                    BehaviorSubject<FeedResponse<T>> pagingSubject = BehaviorSubject.<FeedResponse<T>>create();
                    
                    //Changed it to a Func1 object instead of a lambda, to be stateful and hold innerTop and innerRequestPageSize variables
                    
                    Observable<FeedResponse<T>> feedResponsePageObservable = pagingSubject.asObservable()
                                .concatMap(new Func1<FeedResponse<T>, Observable<FeedResponse<T>>>() {

                        int innerTop = top; 
                        int innerRequestPageSize = requestPageSize;
                        @Override
                        public Observable<FeedResponse<T>> call(FeedResponse<T> previousPage) {
                            
                            //update inner Top and innerRequestPageSize after each page is received
                            
                            if (innerTop != -1) {
                                innerTop -= innerRequestPageSize;
                                innerRequestPageSize = Math.min(requestPageSize, innerTop);
                            }
                            
                            //if innerTop is 0, out Top limit has been met, so returning empty page
                            
                            if (innerTop == 0) {
                                return Observable.<FeedResponse<T>>empty().doOnCompleted(() -> pagingSubject.onCompleted());
                            }
                            
                            String token = previousPage.getResponseContinuation();
                             
                            if (token != null && (!isChangeFeed || !BridgeInternal.noChanges(previousPage))) {
                                logger.debug("going for next page continuation token is not null");

                                RxDocumentServiceRequest request = createRequestFunc.call(token, innerRequestPageSize);
                                return executeFunc.call(request).doOnNext(page -> {
                                    logger.trace("in do on next");
                                    pagingSubject.onNext(page);
                                });
                            } else {
                                logger.debug("continuation token is null");

                                return Observable.<FeedResponse<T>>empty()
                                        .doOnCompleted(() -> pagingSubject.onCompleted());
                            }
                        }});

                    logger.debug("invoking subject on next");

                    pagingSubject.onNext(firstPage);
                    logger.debug("after invoking subject on next");

                    return Observable.just(firstPage).concatWith(feedResponsePageObservable);
                });
            } catch (Exception e) {
                logger.debug("Failure in querying/reading " + resourceType + "s due to [{}]", e.getMessage(), e);
                return Observable.error(e);
            }
        });
    }
}
