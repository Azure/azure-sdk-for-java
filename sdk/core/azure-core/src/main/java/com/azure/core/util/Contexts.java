// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.implementation.util.HttpUtils;

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
     * Adds the TLS client certificate that the outgoing request's access token is bound to, for mutual TLS (mTLS)
     * proof-of-possession.
     * <p>
     * HTTP clients that support certificate binding, as reported by
     * {@link com.azure.core.http.HttpClient#supportsClientCertificateBinding()}, send the request only over a TLS
     * connection that presents this certificate. The HTTP pipeline fails the request without sending it when its HTTP
     * client doesn't support certificate binding.
     *
     * @param clientCertificateBinding The client certificate binding, or null to clear one added earlier.
     * @return Itself.
     */
    public Contexts setClientCertificateBinding(ClientCertificateBinding clientCertificateBinding) {
        context = context.addData(HttpUtils.AZURE_CLIENT_CERTIFICATE_BINDING, clientCertificateBinding);
        return this;
    }

    /**
     * Retrieves the TLS client certificate that the outgoing request's access token is bound to, for mutual TLS
     * (mTLS) proof-of-possession.
     *
     * @return The client certificate binding, or null if the request doesn't carry one.
     */
    public ClientCertificateBinding getClientCertificateBinding() {
        Object binding = context.getData(HttpUtils.AZURE_CLIENT_CERTIFICATE_BINDING).orElse(null);
        return binding instanceof ClientCertificateBinding ? (ClientCertificateBinding) binding : null;
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
    }
}
