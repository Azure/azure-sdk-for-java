// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Singleton helper class that tracks OkHttp HttpResponses and closes them once they no longer have any strong
 * references.
 */
public final class OkHttpResponseCloser {
    private static final ClientLogger LOGGER = new ClientLogger(OkHttpResponseCloser.class);

    private static volatile ReferenceQueue<HttpResponse> referenceQueue = new ReferenceQueue<>();
    private static volatile Queue<WeakReference<HttpResponse>> references = new ConcurrentLinkedQueue<>();

    /**
     * Tracks a OkHttp HttpResponse and closes it once it no long has any strong references.
     * <p>
     * Before the new response is tracked all previously tracked responses are checked for being queued.
     * <p>
     * After the new response is tracked the queued responses are closed.
     *
     * @param httpResponse The HttpResponse to track.
     */
    public static synchronized void trackResponse(HttpResponse httpResponse) {
        // Purge references that have been queued.
        references.removeIf(WeakReference::isEnqueued);

        // Add the new reference.
        references.add(new WeakReference<>(httpResponse, referenceQueue));

        // Poll the queue until no new references are returned.
        while (true) {
            if (referenceQueue.poll() == null) {
                return;
            }

            try {
                referenceQueue.remove().get().close();
            } catch (InterruptedException ex) {
                LOGGER.warning("Exception caught while closing response body. {}", ex);
                return;
            }
        }
    }

    private OkHttpResponseCloser() {
        // Private constructor.
    }
}
