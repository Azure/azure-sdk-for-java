// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;

import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

import static java.net.http.HttpRequest.BodyPublishers.fromPublisher;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpRequest.BodyPublishers.ofByteArray;
import static java.net.http.HttpRequest.BodyPublishers.ofInputStream;
import static java.net.http.HttpRequest.BodyPublishers.ofString;

final class BodyPublisherUtils {
    private static final ClientLogger LOGGER = new ClientLogger(BodyPublisherUtils.class);

    private BodyPublisherUtils() {
    }

    /**
     * Creates BodyPublisher depending on underlying body content type.
     * If progress reporter is not null, configures it to track request body upload.
     *
     * @param request {@link com.azure.core.http.HttpRequest} instance
     * @progressReporter optional progress reporter.
     * @return the request BodyPublisher
     */
    public static HttpRequest.BodyPublisher toBodyPublisher(com.azure.core.http.HttpRequest request, ProgressReporter progressReporter) {
        BinaryData body = request.getBodyAsBinaryData();
        if (body == null) {
            return noBody();
        }

        HttpRequest.BodyPublisher publisher = null;
        BinaryDataContent bodyContent = BinaryDataHelper.getContent(body);
        if (bodyContent instanceof ByteArrayContent) {
            publisher = ofByteArray(bodyContent.toBytes());
        } else if (bodyContent instanceof StringContent || bodyContent instanceof SerializableContent) {
            publisher = ofString(bodyContent.toString());
        } else if (bodyContent instanceof FileContent || bodyContent instanceof InputStreamContent) {
            publisher = ofInputStream(bodyContent::toStream);
            Long contentLength = bodyContent.getLength();
            if (contentLength != null) {
                publisher = fromPublisher(publisher, contentLength);
            }
        } else {
            final String contentLength = request.getHeaders().getValue("content-length");
            publisher = toBodyPublisher(request.getBody(), contentLength);
        }

        return getPublisherWithReporter(publisher, progressReporter);
    }


    /**
     * Create BodyPublisher from the given java.nio.ByteBuffer publisher.
     *
     * @param bbPublisher stream of java.nio.ByteBuffer representing request content
     * @return the request BodyPublisher
     */
    private static HttpRequest.BodyPublisher toBodyPublisher(Flux<ByteBuffer> bbPublisher, String contentLength) {
        if (bbPublisher == null) {
            return noBody();
        }
        final Flow.Publisher<ByteBuffer> bbFlowPublisher = JdkFlowAdapter.publisherToFlowPublisher(bbPublisher);
        if (CoreUtils.isNullOrEmpty(contentLength)) {
            return fromPublisher(bbFlowPublisher);
        } else {
            long contentLengthLong = Long.parseLong(contentLength);
            if (contentLengthLong < 1) {
                return noBody();
            } else {
                return fromPublisher(bbFlowPublisher, contentLengthLong);
            }
        }
    }

    private static HttpRequest.BodyPublisher getPublisherWithReporter(HttpRequest.BodyPublisher downstream, ProgressReporter progressReporter) {
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
