/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;

/**
 * A certificate that can be installed on compute nodes and can be used to
 * authenticate operations on the machine.
 */
public class Certificate {
    /**
     * The X.509 thumbprint of the certificate. This is a sequence of up to 40
     * hex digits (it may include spaces but these are removed).
     */
    private String thumbprint;

    /**
     * The algorithm used to derive the thumbprint. This must be sha1.
     */
    private String thumbprintAlgorithm;

    /**
     * The URL of the certificate.
     */
    private String url;

    /**
     * The current state of the certificate. Possible values include:
     * 'active', 'deleting', 'deletefailed'.
     */
    private CertificateState state;

    /**
     * The time at which the certificate entered its current state.
     */
    private DateTime stateTransitionTime;

    /**
     * The previous state of the certificate. This property is not set if the
     * certificate is in its initial Active state. Possible values include:
     * 'active', 'deleting', 'deletefailed'.
     */
    private CertificateState previousState;

    /**
     * The time at which the certificate entered its previous state. This
     * property is not set if the certificate is in its initial Active state.
     */
    private DateTime previousStateTransitionTime;

    /**
     * The public part of the certificate as a base-64 encoded .cer file.
     */
    private String publicData;

    /**
     * The error that occurred on the last attempt to delete this certificate.
     * This property is set only if the certificate is in the deletefailed
     * state.
     */
    private DeleteCertificateError deleteCertificateError;

    /**
     * Get the thumbprint value.
     *
     * @return the thumbprint value
     */
    public String thumbprint() {
        return this.thumbprint;
    }

    /**
     * Set the thumbprint value.
     *
     * @param thumbprint the thumbprint value to set
     * @return the Certificate object itself.
     */
    public Certificate withThumbprint(String thumbprint) {
        this.thumbprint = thumbprint;
        return this;
    }

    /**
     * Get the thumbprintAlgorithm value.
     *
     * @return the thumbprintAlgorithm value
     */
    public String thumbprintAlgorithm() {
        return this.thumbprintAlgorithm;
    }

    /**
     * Set the thumbprintAlgorithm value.
     *
     * @param thumbprintAlgorithm the thumbprintAlgorithm value to set
     * @return the Certificate object itself.
     */
    public Certificate withThumbprintAlgorithm(String thumbprintAlgorithm) {
        this.thumbprintAlgorithm = thumbprintAlgorithm;
        return this;
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String url() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param url the url value to set
     * @return the Certificate object itself.
     */
    public Certificate withUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public CertificateState state() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     * @return the Certificate object itself.
     */
    public Certificate withState(CertificateState state) {
        this.state = state;
        return this;
    }

    /**
     * Get the stateTransitionTime value.
     *
     * @return the stateTransitionTime value
     */
    public DateTime stateTransitionTime() {
        return this.stateTransitionTime;
    }

    /**
     * Set the stateTransitionTime value.
     *
     * @param stateTransitionTime the stateTransitionTime value to set
     * @return the Certificate object itself.
     */
    public Certificate withStateTransitionTime(DateTime stateTransitionTime) {
        this.stateTransitionTime = stateTransitionTime;
        return this;
    }

    /**
     * Get the previousState value.
     *
     * @return the previousState value
     */
    public CertificateState previousState() {
        return this.previousState;
    }

    /**
     * Set the previousState value.
     *
     * @param previousState the previousState value to set
     * @return the Certificate object itself.
     */
    public Certificate withPreviousState(CertificateState previousState) {
        this.previousState = previousState;
        return this;
    }

    /**
     * Get the previousStateTransitionTime value.
     *
     * @return the previousStateTransitionTime value
     */
    public DateTime previousStateTransitionTime() {
        return this.previousStateTransitionTime;
    }

    /**
     * Set the previousStateTransitionTime value.
     *
     * @param previousStateTransitionTime the previousStateTransitionTime value to set
     * @return the Certificate object itself.
     */
    public Certificate withPreviousStateTransitionTime(DateTime previousStateTransitionTime) {
        this.previousStateTransitionTime = previousStateTransitionTime;
        return this;
    }

    /**
     * Get the publicData value.
     *
     * @return the publicData value
     */
    public String publicData() {
        return this.publicData;
    }

    /**
     * Set the publicData value.
     *
     * @param publicData the publicData value to set
     * @return the Certificate object itself.
     */
    public Certificate withPublicData(String publicData) {
        this.publicData = publicData;
        return this;
    }

    /**
     * Get the deleteCertificateError value.
     *
     * @return the deleteCertificateError value
     */
    public DeleteCertificateError deleteCertificateError() {
        return this.deleteCertificateError;
    }

    /**
     * Set the deleteCertificateError value.
     *
     * @param deleteCertificateError the deleteCertificateError value to set
     * @return the Certificate object itself.
     */
    public Certificate withDeleteCertificateError(DeleteCertificateError deleteCertificateError) {
        this.deleteCertificateError = deleteCertificateError;
        return this;
    }

}
