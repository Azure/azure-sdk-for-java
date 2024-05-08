// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressReporter;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;

import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

import static java.net.http.HttpRequest.BodyPublishers.fromPublisher;
import static java.net.http.HttpRequest.BodyPublishers.noBody;

/**
 * Utility class for BodyPublisher.
 */
public final class BodyPublisherUtils {
    private BodyPublisherUtils() {
    }

    /**
     * Creates BodyPublisher depending on underlying body content type.
     * If progress reporter is not null, configures it to track request body upload.
     *
     * @param request {@link com.azure.core.http.HttpRequest} instance
     * @param writeTimeout write timeout
     * @param progressReporter optional progress reporter.
     * @return the request BodyPublisher
     */
    public static HttpRequest.BodyPublisher toBodyPublisher(com.azure.core.http.HttpRequest request,
        Duration writeTimeout, ProgressReporter progressReporter) {
        BinaryData body = request.getBodyAsBinaryData();
        if (body == null) {
            return noBody();
        }

        HttpRequest.BodyPublisher publisher;
        BinaryDataContent bodyContent = BinaryDataHelper.getContent(body);
        Flux<ByteBuffer> fluxBody;
        if (bodyContent instanceof ByteArrayContent
            || bodyContent instanceof StringContent
            || bodyContent instanceof SerializableContent) {
            // String and serializable content also uses ofByteArray as ofString is just a wrapper for this,
            // so we might as well own the handling.
            byte[] bytes = bodyContent.toBytes();
            fluxBody = Flux.defer(() -> {
                AtomicInteger position = new AtomicInteger(0);

                // This is used over the built-in JDK HttpClient method to send a byte array body as that performs a
                // deep duplication of data, whereas this creates read-only ByteBuffers over the byte array content.
                return Flux.generate(sink -> {
                    int remaining = bytes.length - position.get();
                    if (remaining == 0) {
                        sink.complete();
                    } else {
                        int chunkSize = Math.min(remaining, 8192);
                        sink.next(ByteBuffer.wrap(bytes, position.getAndAdd(chunkSize), chunkSize));
                    }
                });
            });
        } else {
            if (bodyContent instanceof FileContent || bodyContent instanceof InputStreamContent) {
                fluxBody = FluxUtil.toFluxByteBuffer(bodyContent.toStream());
            } else {
                fluxBody = request.getBody();
            }
        }

        // Only apply the timeout if it exists and is positive.
        if (writeTimeout != null && !writeTimeout.isNegative() && !writeTimeout.isZero()) {
            fluxBody = fluxBody.timeout(writeTimeout);
        }

        // Further investigation into the factory methods in 'RequestBodyPublishers' shows that they buffer internally.
        // Using those methods don't net us much in terms of performance and makes the write timeout story more
        // difficult to implement.
        //
        // Use 'fromPublisher' to create a BodyPublisher from the Flux<ByteBuffer> that has a timeout applied if
        // elements aren't request fast enough.
        publisher = toBodyPublisherWithLength(fromPublisher(JdkFlowAdapter.publisherToFlowPublisher(fluxBody)),
            request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));

        return getPublisherWithReporter(publisher, progressReporter);
    }

    /**
     * Creates BodyPublisher with content length
     *
     * @param publisher BodyPublisher representing request content that's not aware of content length
     * @return the request BodyPublisher
     */
    private static HttpRequest.BodyPublisher toBodyPublisherWithLength(HttpRequest.BodyPublisher publisher,
        String contentLength) {
        if (CoreUtils.isNullOrEmpty(contentLength)) {
            return publisher;
        } else {
            long contentLengthLong = Long.parseLong(contentLength);
            if (contentLengthLong < 1) {
                return noBody();
            } else {
                return fromPublisher(publisher, contentLengthLong);
            }
        }
    }

    private static HttpRequest.BodyPublisher getPublisherWithReporter(HttpRequest.BodyPublisher downstream,
        ProgressReporter progressReporter) {
        return progressReporter == null ? downstream : new CountingPublisher(downstream, progressReporter);
    }

    private static class CountingPublisher implements HttpRequest.BodyPublisher {

        private final HttpRequest.BodyPublisher downstream;
        private final ProgressReporter progressReporter;

        CountingPublisher(HttpRequest.BodyPublisher downstream, ProgressReporter progressReporter) {
            this.downstream = downstream;
            this.progressReporter = progressReporter;
        }

        @Override
        public long contentLength() {
            return downstream.contentLength();
        }

        @Override
        public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
            downstream.subscribe(new CountingSubscriber(subscriber, progressReporter));
        }
    }

    private static class CountingSubscriber implements Flow.Subscriber<ByteBuffer> {
        private final Flow.Subscriber<? super ByteBuffer> downstream;
        private final ProgressReporter progressReporter;

        CountingSubscriber(Flow.Subscriber<? super ByteBuffer> downstream, ProgressReporter progressReporter) {
            this.downstream = downstream;
            this.progressReporter = progressReporter;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            downstream.onSubscribe(subscription);
        }

        @Override
        public void onNext(ByteBuffer item) {
            progressReporter.reportProgress(item.remaining());
            downstream.onNext(item);
        }

        @Override
        public void onError(Throwable throwable) {
            downstream.onError(throwable);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}
