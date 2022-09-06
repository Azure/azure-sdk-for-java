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

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

import static java.net.http.HttpRequest.BodyPublishers.fromPublisher;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpRequest.BodyPublishers.ofByteArray;
import static java.net.http.HttpRequest.BodyPublishers.ofFile;
import static java.net.http.HttpRequest.BodyPublishers.ofInputStream;
import static java.net.http.HttpRequest.BodyPublishers.ofString;

class BodyPublisherUtils {

    private BodyPublisherUtils() {
    }

    private static final ClientLogger LOGGER = new ClientLogger(BodyPublisherUtils.class);

    /**
     * Create BodyPublisher from the given java.nio.ByteBuffer publisher.
     *
     * @param request {@link com.azure.core.http.HttpRequest} instance
     * @progressReporter optional progress reporter for request upload.
     * @return the request BodyPublisher
     */
    public static HttpRequest.BodyPublisher toBodyPublisher(com.azure.core.http.HttpRequest request, ProgressReporter progressReporter) {
        BinaryData body = request.getBodyAsBinaryData();
        if (body != null) {
            BinaryDataContent bodyContent = BinaryDataHelper.getContent(body);
            if (bodyContent instanceof ByteArrayContent) {
                return getPublisherWithReporter(ofByteArray(bodyContent.toBytes()), progressReporter);
            } else if (bodyContent instanceof StringContent
                || bodyContent instanceof SerializableContent) {
                return getPublisherWithReporter(ofString(bodyContent.toString()), progressReporter);
            } else if (bodyContent instanceof FileContent) {
                try {
                    return getPublisherWithReporter(ofFile(((FileContent) bodyContent).getFile()), progressReporter);
                } catch (FileNotFoundException e) {
                    throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
                }
            } else if (bodyContent instanceof InputStreamContent) {
                return getPublisherWithReporter(ofInputStream(bodyContent::toStream), progressReporter);
            } else {
                final String contentLength = request.getHeaders().getValue("content-length");
                return getPublisherWithReporter(toBodyPublisher(request.getBody(), contentLength), progressReporter);
            }
        } else {
            return noBody();
        }
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
        public CountingPublisher(HttpRequest.BodyPublisher downstream, ProgressReporter progressReporter) {
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
    private static class CountingSubscriber<T extends ByteBuffer> implements Flow.Subscriber<T> {
        private final Flow.Subscriber<T> downstream;
        private final ProgressReporter progressReporter;
        public CountingSubscriber(Flow.Subscriber<T> downstream, ProgressReporter progressReporter) {
            this.downstream = downstream;
            this.progressReporter = progressReporter;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            downstream.onSubscribe(subscription);
        }

        @Override
        public void onNext(T item) {
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
