/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.RestResponse;
import com.microsoft.rest.v2.http.HttpPipeline;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.io.IOException;
import java.nio.ByteBuffer;

import java.util.function.Function;

/**
 * {@code RetryReader} is used to wrap a download request and automatically retry failed reads as appropriate. If the
 * download is interrupted, the RetryReader will make a request to resume the download from where it left off, allowing
 * the user to consume the data as one continuous stream, for any interruptions are hidden. The retry behavior is
 * defined by the passed options, and the download will resume according to the provided getter function. Note that not
 * even the initial request will be made until the RetryReader is subscribed to
 * <p>
 * A common use case would be to provide a getter function which internally calls download on a blobURL after applying
 * the parameters from the info.
 * <p>
 * Note that the retries performed as a part of this reader are composed with those of any retries in an {@link
 * HttpPipeline} used in conjunction with this reader. That is, if the reader issues a request to resume a a download,
 * an underlying pipeline may issue several retries as a part of that request. Furthermore, this reader only retries on
 * network errors; timeouts and unexpected status codes are not retired. Therefore, the behavior of this reader is
 * entirely independent of and in no way coupled to an {@link HttpPipeline}'s retry mechanism.
 */
public class RetryReader extends Flowable<ByteBuffer> {
    private Single<? extends RestResponse<?, Flowable<ByteBuffer>>> response;

    private HTTPGetterInfo info;

    private RetryReaderOptions options;

    private Function<HTTPGetterInfo, Single<? extends RestResponse<?, Flowable<ByteBuffer>>>> getter;

    public RetryReader(Single<? extends RestResponse<?, Flowable<ByteBuffer>>> initialResponse, HTTPGetterInfo info,
            RetryReaderOptions options,
            Function<HTTPGetterInfo, Single<? extends RestResponse<?, Flowable<ByteBuffer>>>> getter) {
        Utility.assertNotNull("getter", getter);
        Utility.assertInBounds("info.count", info.count, 0, Integer.MAX_VALUE);
        Utility.assertInBounds("options.maxRetryRequests", options.maxRetryRequests, 0, Integer.MAX_VALUE);

        this.response = initialResponse;
        this.info = info;
        this.options = options;
        this.getter = getter;
    }

    @Override
    protected void subscribeActual(Subscriber<? super ByteBuffer> s) {
        /*
        We we were not given a response stream to start with. Get one. The getter does not actually make a request,
        it returns a Single, so it is not eligible to be retried according to our maxTryCount. If the getter itself
        fails, we have no way of continuing, and so report an error.
         */
        boolean initialRequestMade = false;
        if (this.response == null) {
            initialRequestMade = true;
            try {
                this.response = getter.apply(this.info);
            } catch (Throwable throwable) {
                s.onError(throwable);
                return;
            }
        }

        Flowable<ByteBuffer> stream = readActual(s, this.response, initialRequestMade ? 1 : 0);

        stream.subscribe();
    }

    private Flowable<ByteBuffer> readActual(Subscriber<? super ByteBuffer> s,
            Single<? extends RestResponse<?, Flowable<ByteBuffer>>> response, int tryCount) {
        return response.flatMapPublisher(RestResponse::body)
                /*
                Update how much data we have received in case we need to retry and propagate to the user the data we
                have received.
                 */
                .doOnNext(buffer -> {
                    this.info.offset += buffer.remaining();
                    this.info.count -= buffer.remaining();
                    s.onNext(buffer);
                })
                /*
                Each attempt tries to read all of the remaining data. If we make it to the end of the body of a given
                request, this download is complete, and we are done.
                 */
                .doOnComplete(s::onComplete)
                /*
                Note that this will capture errors from mapping the response to the body, which involves making a
                GET request, and errors from trying to read the body.
                 */
                .onErrorResumeNext(new io.reactivex.functions.Function<Throwable, Publisher<? extends ByteBuffer>>() {
                    @Override
                    public Publisher<? extends ByteBuffer> apply(Throwable throwable) throws Exception {
                        // If all the retries are exhausted, report it to the user and error out.
                        if (tryCount > options.maxRetryRequests) {
                            s.onError(throwable);
                            return Flowable.empty();
                        }
                        /*
                        We are unable to get more specific information as to the nature of the error from the protocol
                        layer when reading the stream, so we optimistically assume that an IOException is retryable.
                        */
                        else if (throwable instanceof IOException) {
                            // Get a new response to try reading from.
                            Single<? extends RestResponse<?, Flowable<ByteBuffer>>> newResponse;
                            try {
                                newResponse = getter.apply(info);
                            } catch (Throwable t) {
                                s.onError(t);
                                return Flowable.empty();
                            }

                            // Continue with the same process again and increment the tryCount.
                            return readActual(s, newResponse, tryCount + 1);
                        } else {
                            s.onError(throwable);
                            return Flowable.empty();
                        }
                    }
                });
    }

    /**
     * HTTPGetterInfo is a passed to the getter function of a RetryReader to specify parameters needed for the GET
     * request.
     */
    public static class HTTPGetterInfo {
        /**
         * The start offset that should be used when creating the HTTP GET request's Range header.
         */
        public long offset;

        /**
         * The count of bytes that should be used to calculate the end offset when creating the HTTP GET request's Range
         * header. {@code} null indicates that the entire rest of the blob should be retrieved.
         */
        public Long count;

        /**
         * The resource's etag that should be used when creating the HTTP GET request's If-Match header. Note that the
         * Etag is returned with any operation that modifies the resource and by a call to {@link
         * BlobURL#getProperties(BlobAccessConditions)}.
         */
        public ETag eTag;
    }
}
