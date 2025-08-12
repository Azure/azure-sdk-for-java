// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchCertificate;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Async poller class used by {@code beginDeleteCertificate} to implement polling logic
 * for deleting a {@link BatchCertificate}. Returns {@link BatchCertificate} during polling
 * and {@code null} upon successful deletion.
 */
public final class CertificateDeletePollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String thumbprintAlgorithm;
    private final String thumbprint;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new {@link CertificateDeletePollerAsync}.
     *
     * @param batchAsyncClient The {@link BatchAsyncClient} used to interact with the Batch service.
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint. (e.g., "sha1")
     * @param thumbprint The thumbprint of the certificate to delete.
     * @param options Optional request options for service calls.
     */
    public CertificateDeletePollerAsync(BatchAsyncClient batchAsyncClient, String thumbprintAlgorithm,
        String thumbprint, RequestOptions options) {
        this.batchAsyncClient = batchAsyncClient;
        this.thumbprintAlgorithm = thumbprintAlgorithm;
        this.thumbprint = thumbprint;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation to start the delete.
     *
     * @return A function that initiates the delete request and returns a {@link PollResponse}
     * with {@link LongRunningOperationStatus#IN_PROGRESS} status.
     */
    public Function<PollingContext<BatchCertificate>, Mono<PollResponse<BatchCertificate>>> getActivationOperation() {
        return context -> batchAsyncClient.deleteCertificateWithResponse(thumbprintAlgorithm, thumbprint, options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    /**
     * Poll operation to check if the certificate is still being deleted or is gone.
     *
     * @return A function that polls the certificate state and returns a {@link PollResponse}
     * with the current {@link LongRunningOperationStatus}.
     */
    public Function<PollingContext<BatchCertificate>, Mono<PollResponse<BatchCertificate>>> getPollOperation() {
        return context -> {
            RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getCertificateWithResponse(thumbprintAlgorithm, thumbprint, pollOptions)
                .map(response -> {
                    BatchCertificate cert = response.getValue().toObject(BatchCertificate.class);
                    return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, cert);
                })
                .onErrorResume(HttpResponseException.class,
                    ex -> ex.getResponse() != null && ex.getResponse().getStatusCode() == 404
                        ? Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null))
                        : Mono.error(ex));
        };
    }

    /**
     * Cancel operation (not supported for certificate deletion).
     *
     * @return A function that always returns an empty {@link Mono}, indicating cancellation is unsupported.
     */
    public BiFunction<PollingContext<BatchCertificate>, PollResponse<BatchCertificate>, Mono<BatchCertificate>>
        getCancelOperation() {
        return (context, pollResponse) -> Mono.empty();
    }

    /**
     * Final result fetch operation (returns null; not required).
     *
     * @return A function that returns an empty {@link Mono}, indicating no final fetch is required.
     */
    public Function<PollingContext<BatchCertificate>, Mono<Void>> getFetchResultOperation() {
        return context -> Mono.empty();
    }
}
