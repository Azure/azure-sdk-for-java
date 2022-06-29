// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.Objects;

/**
 * A utility type that can be used to add and retrieve instances commonly used in {@link Context}.
 */
public final class Contexts {

    private Context context;

    private Contexts(Context context) {
        this.context = Objects.requireNonNull(context, "'context' must not be null");
    }

    /**
     * Creates {@link Contexts} from empty {@link Context}.
     * @return The {@link Contexts} instance.
     */
    public static Contexts empty() {
        return with(Context.NONE);
    }

    /**
     * Creates {@link Contexts} from supplied {@link Context}.
     * @param context Existing {@link Context}. Must not be null.
     * @return The {@link Contexts} instance.
     * @throws NullPointerException If {@code context} is null.
     */
    public static Contexts with(Context context) {
        return new Contexts(context);
    }

    /**
     * Adds request's {@link ProgressReporter} instance to the {@link Context}.
     * @param progressReporter The {@link ProgressReporter} instance.
     * @return Itself.
     */
    public Contexts setHttpRequestProgressReporter(ProgressReporter progressReporter) {
        context = context.addData(Keys.HTTP_REQUEST_PROGRESS_REPORTER, progressReporter);
        return this;
    }

    /**
     * Retrieves request's {@link ProgressReporter} from the {@link Context}.
     * @return The {@link ProgressReporter}.
     */
    public ProgressReporter getHttpRequestProgressReporter() {
        return (ProgressReporter) context.getData(Keys.HTTP_REQUEST_PROGRESS_REPORTER).orElse(null);
    }

    /**
     * Configure whether Sync Rest Proxy should be used or not for the Sync APIs.
     * @param restProxySyncProxyEnable The flag indicating if sync rest proxy should be enabled or not.
     * @return Itself.
     */
    public Contexts setRestProxySyncProxyEnable(boolean restProxySyncProxyEnable) {
        context = context.addData(Keys.HTTP_REST_PROXY_SYNC_PROXY_ENABLE, restProxySyncProxyEnable);
        return this;
    }

    /**
     * Retrieves the {@link Boolean} flag from the {@link Context} indicating if Sync Rest Proxy is enabled or not.
     * @return The {@link Boolean} flag indicating whether sync rest proxy is enabled or not.
     */
    public boolean isRestProxySyncProxyEnabled() {
        return (boolean) context.getData(Keys.HTTP_REST_PROXY_SYNC_PROXY_ENABLE).orElse(false);
    }

    /**
     * Returns a version of the {@link Context} reflecting mutations.
     * @return The version of the {@link Context} reflecting mutations.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Defines {@link Context} keys commonly used in Azure SDKs.
     */
    private static final class Keys {
        private Keys() {
        }

        /**
         * A {@link Context} key for the outgoing request's {@link ProgressReporter}.
         */
        public static final String HTTP_REQUEST_PROGRESS_REPORTER = "com.azure.core.http.request.progress.reporter";
        public static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";
    }
}
