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
     * Get or sets the X.509 thumbprint of the certificate. This is a sequence
     * of up to 40 hex digits (it may include spaces but these are removed).
     */
    private String thumbprint;

    /**
     * Gets or sets the algorithm used to derive the thumbprint. This must be
     * sha1.
     */
    private String thumbprintAlgorithm;

    /**
     * Gets or sets the URL of the certificate.
     */
    private String url;

    /**
     * Gets or sets the current state of the certificate. Possible values
     * include: 'active', 'deleting', 'deletefailed'.
     */
    private CertificateState state;

    /**
     * Gets or sets the time at which the certificate entered its current
     * state.
     */
    private DateTime stateTransitionTime;

    /**
     * Gets or sets the previous state of the certificate. This property is
     * not set if the certificate is in its initial Active state. Possible
     * values include: 'active', 'deleting', 'deletefailed'.
     */
    private CertificateState previousState;

    /**
     * Gets or sets the time at which the certificate entered its previous
     * state.  This property is not set if the certificate is in its initial
     * Active state.
     */
    private DateTime previousStateTransitionTime;

    /**
     * Gets or sets the public part of the certificate as a base-64 encoded
     * .cer file.
     */
    private String publicData;

    /**
     * Gets or sets the error that occurred on the last attempt to delete this
     * certificate.  This property is set only if the certificate is in the
     * deletefailed state.
     */
    private DeleteCertificateError deleteCertificateError;

    /**
     * Get the thumbprint value.
     *
     * @return the thumbprint value
     */
    public String getThumbprint() {
        return this.thumbprint;
    }

    /**
     * Set the thumbprint value.
     *
     * @param thumbprint the thumbprint value to set
     */
    public void setThumbprint(String thumbprint) {
        this.thumbprint = thumbprint;
    }

    /**
     * Get the thumbprintAlgorithm value.
     *
     * @return the thumbprintAlgorithm value
     */
    public String getThumbprintAlgorithm() {
        return this.thumbprintAlgorithm;
    }

    /**
     * Set the thumbprintAlgorithm value.
     *
     * @param thumbprintAlgorithm the thumbprintAlgorithm value to set
     */
    public void setThumbprintAlgorithm(String thumbprintAlgorithm) {
        this.thumbprintAlgorithm = thumbprintAlgorithm;
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param url the url value to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public CertificateState getState() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     */
    public void setState(CertificateState state) {
        this.state = state;
    }

    /**
     * Get the stateTransitionTime value.
     *
     * @return the stateTransitionTime value
     */
    public DateTime getStateTransitionTime() {
        return this.stateTransitionTime;
    }

    /**
     * Set the stateTransitionTime value.
     *
     * @param stateTransitionTime the stateTransitionTime value to set
     */
    public void setStateTransitionTime(DateTime stateTransitionTime) {
        this.stateTransitionTime = stateTransitionTime;
    }

    /**
     * Get the previousState value.
     *
     * @return the previousState value
     */
    public CertificateState getPreviousState() {
        return this.previousState;
    }

    /**
     * Set the previousState value.
     *
     * @param previousState the previousState value to set
     */
    public void setPreviousState(CertificateState previousState) {
        this.previousState = previousState;
    }

    /**
     * Get the previousStateTransitionTime value.
     *
     * @return the previousStateTransitionTime value
     */
    public DateTime getPreviousStateTransitionTime() {
        return this.previousStateTransitionTime;
    }

    /**
     * Set the previousStateTransitionTime value.
     *
     * @param previousStateTransitionTime the previousStateTransitionTime value to set
     */
    public void setPreviousStateTransitionTime(DateTime previousStateTransitionTime) {
        this.previousStateTransitionTime = previousStateTransitionTime;
    }

    /**
     * Get the publicData value.
     *
     * @return the publicData value
     */
    public String getPublicData() {
        return this.publicData;
    }

    /**
     * Set the publicData value.
     *
     * @param publicData the publicData value to set
     */
    public void setPublicData(String publicData) {
        this.publicData = publicData;
    }

    /**
     * Get the deleteCertificateError value.
     *
     * @return the deleteCertificateError value
     */
    public DeleteCertificateError getDeleteCertificateError() {
        return this.deleteCertificateError;
    }

    /**
     * Set the deleteCertificateError value.
     *
     * @param deleteCertificateError the deleteCertificateError value to set
     */
    public void setDeleteCertificateError(DeleteCertificateError deleteCertificateError) {
        this.deleteCertificateError = deleteCertificateError;
    }

}
