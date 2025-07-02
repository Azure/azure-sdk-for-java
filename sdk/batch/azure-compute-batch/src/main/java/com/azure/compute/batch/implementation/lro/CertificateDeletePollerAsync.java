package com.azure.compute.batch.implementation.lro;

import com.azure.compute.batch.BatchAsyncClient;
import com.azure.compute.batch.models.BatchCertificate;
import com.azure.compute.batch.models.BatchCertificateState;
import com.azure.core.exception.ResourceNotFoundException;
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
     */
    public Function<PollingContext<BatchCertificate>, Mono<PollResponse<BatchCertificate>>> getActivationOperation() {
        return context -> batchAsyncClient.deleteCertificateWithResponse(thumbprintAlgorithm, thumbprint, options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    /**
     * Poll operation to check if the certificate is still being deleted or is gone.
     */
    public Function<PollingContext<BatchCertificate>, Mono<PollResponse<BatchCertificate>>> getPollOperation() {
        return context -> {
            RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getCertificateWithResponse(thumbprintAlgorithm, thumbprint, pollOptions)
                .map(response -> {
                    BatchCertificate cert = response.getValue().toObject(BatchCertificate.class);
                    LongRunningOperationStatus status = BatchCertificateState.DELETING.equals(cert.getState())
                        ? LongRunningOperationStatus.IN_PROGRESS
                        : LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                    return new PollResponse<>(status, cert);
                })
                .onErrorResume(ResourceNotFoundException.class,
                    ex -> Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null)))
                .onErrorResume(e -> Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null)));
        };
    }

    /**
     * Cancel operation (not supported for certificate deletion).
     */
    public BiFunction<PollingContext<BatchCertificate>, PollResponse<BatchCertificate>, Mono<BatchCertificate>>
        getCancelOperation() {
        return (context, pollResponse) -> Mono.empty();
    }

    /**
     * Final result fetch operation (returns null; not required).
     */
    public Function<PollingContext<BatchCertificate>, Mono<Void>> getFetchResultOperation() {
        return context -> Mono.empty();
    }
}
