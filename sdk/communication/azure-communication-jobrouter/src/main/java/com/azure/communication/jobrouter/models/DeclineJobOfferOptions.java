// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import java.time.OffsetDateTime;

/**
 * Request options to decline a job offer.
 */
public final class DeclineJobOfferOptions {

    /**
     * id of worker declining the job offer.
     */
    private final String workerId;

    /**
     * id of the offer to decline.
     */
    private final String offerId;

    /**
     * If the RetryOfferAt is not provided, then this job will not be offered again to the worker who declined this job
     * unless the worker is de-registered and re-registered.  If a RetryOfferAt time is provided, then the job will be
     * re-matched to eligible workers at the retry time in UTC.  The worker that declined the job will also be eligible
     * for the job at that time.
     */
    private OffsetDateTime retryOfferAt;

    /**
     * Constructor for DeclineJobOfferOptions
     * @param workerId id of worker declining the job offer.
     * @param offerId id of the offer to decline.
     */
    public DeclineJobOfferOptions(String workerId, String offerId) {
        this.workerId = workerId;
        this.offerId = offerId;
    }

    /**
     * Get the retryOfferAt property: If the RetryOfferAt is not provided, then this job will not be offered again to
     * the worker who declined this job unless the worker is de-registered and re-registered. If a RetryOfferAt time is
     * provided, then the job will be re-matched to eligible workers at the retry time in UTC. The worker that declined
     * the job will also be eligible for the job at that time.
     *
     * @return the retryOfferAt value.
     */
    public OffsetDateTime getRetryOfferAt() {
        return this.retryOfferAt;
    }

    /**
     * Set the retryOfferAt property: If the RetryOfferAt is not provided, then this job will not be offered again to
     * the worker who declined this job unless the worker is de-registered and re-registered. If a RetryOfferAt time is
     * provided, then the job will be re-matched to eligible workers at the retry time in UTC. The worker that declined
     * the job will also be eligible for the job at that time.
     *
     * @param retryOfferAt the retryOfferAt value to set.
     * @return the DeclineJobOfferOptions object itself.
     */
    public DeclineJobOfferOptions setRetryOfferAt(OffsetDateTime retryOfferAt) {
        this.retryOfferAt = retryOfferAt;
        return this;
    }

    /**
     * Gets workerId.
     * @return workerId the id of the worker.
     */
    public String getWorkerId() {
        return this.workerId;
    }

    /**
     * Get offerId.
     * @return offerId
     */
    public String getOfferId() {
        return this.offerId;
    }
}
