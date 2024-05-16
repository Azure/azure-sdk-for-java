// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.model;

/**
 * The CertificateBundle REST model.
 */
public class CertificateBundle {

    /**
     * Stores the CER bytes.
     */
    private String cer;

    /**
     * Stores the Key ID.
     */
    private String kid;

    /**
     * Stores the policy.
     */
    private CertificatePolicy policy;

    /**
     * Stores the Secret ID.
     */
    private String sid;

    /**
     * Get the CER string.
     *
     * @return the CER string.
     */
    public String getCer() {
        return cer;
    }

    /**
     * Get the Key ID.
     *
     * @return the Key ID.
     */
    public String getKid() {
        return kid;
    }

    /**
     * Get the policy.
     *
     * @return the policy.
     */
    public CertificatePolicy getPolicy() {
        return policy;
    }

    /**
     * Get the Secret ID.
     *
     * @return the Secret ID.
     */
    public String getSid() {
        return sid;
    }

    /**
     * Set the CER string.
     *
     * @param cer the CER string.
     */
    public void setCer(String cer) {
        this.cer = cer;
    }

    /**
     * Set the Key ID.
     *
     * @param kid the Key ID.
     */
    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * Set the policy.
     *
     * @param policy the policy.
     */
    public void setPolicy(CertificatePolicy policy) {
        this.policy = policy;
    }

    /**
     * Set the Secret ID.
     *
     * @param sid the Secret ID.
     */
    public void setSid(String sid) {
        this.sid = sid;
    }
}
