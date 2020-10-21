// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

/**
 * The KeyVault certificate.
 */
class KeyVaultCertificate extends X509Certificate {

    /**
     * Stores the delegate.
     */
    private final X509Certificate delegate;

    /**
     * Constructor.
     *
     * @param delegate the delegate.
     */
    KeyVaultCertificate(X509Certificate delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * @see X509Certificate#checkValidity()
     */
    @Override
    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        delegate.checkValidity();
    }

    /**
     * @see X509Certificate#checkValidity(java.util.Date)
     */
    @Override
    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        delegate.checkValidity(date);
    }

    /**
     * @see X509Certificate#getBasicConstraints()
     */
    @Override
    public int getBasicConstraints() {
        return delegate.getBasicConstraints();
    }

    /**
     * @see X509Certificate#getCriticalExtensionOIDs()
     */
    @Override
    public Set<String> getCriticalExtensionOIDs() {
        return delegate.getCriticalExtensionOIDs();
    }

    /**
     * @see X509Certificate#getEncoded()
     */
    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        return delegate.getEncoded();
    }

    /**
     * @see X509Certificate#getExtensionValue(java.lang.String)
     */
    @Override
    public byte[] getExtensionValue(String oid) {
        return delegate.getExtensionValue(oid);
    }

    /**
     * @see X509Certificate#getIssuerDN()
     */
    @Override
    public Principal getIssuerDN() {
        return delegate.getIssuerDN();
    }

    /**
     * @see X509Certificate#getIssuerUniqueID()
     */
    @Override
    public boolean[] getIssuerUniqueID() {
        return delegate.getIssuerUniqueID();
    }

    /**
     * @see X509Certificate#getKeyUsage()
     */
    @Override
    public boolean[] getKeyUsage() {
        return delegate.getKeyUsage();
    }

    /**
     * @see X509Certificate#getNonCriticalExtensionOIDs()
     */
    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
        return delegate.getNonCriticalExtensionOIDs();
    }

    /**
     * @see X509Certificate#getNotAfter()
     */
    @Override
    public Date getNotAfter() {
        return delegate.getNotAfter();
    }

    /**
     * @see X509Certificate#getNotBefore()
     */
    @Override
    public Date getNotBefore() {
        return delegate.getNotBefore();
    }

    /**
     * @see X509Certificate#getPublicKey()
     */
    @Override
    public PublicKey getPublicKey() {
        return delegate.getPublicKey();
    }

    /**
     * @see X509Certificate#getSerialNumber()
     */
    @Override
    public BigInteger getSerialNumber() {
        return delegate.getSerialNumber();
    }

    /**
     * @see X509Certificate#getSigAlgName()
     */
    @Override
    public String getSigAlgName() {
        return delegate.getSigAlgName();
    }

    /**
     * @see X509Certificate#getSigAlgOID()
     */
    @Override
    public String getSigAlgOID() {
        return delegate.getSigAlgOID();
    }

    /**
     * @see X509Certificate#getSigAlgParams()
     */
    @Override
    public byte[] getSigAlgParams() {
        return delegate.getSigAlgParams();
    }

    /**
     * @see X509Certificate#getSignature()
     */
    @Override
    public byte[] getSignature() {
        return delegate.getSignature();
    }

    /**
     * @see X509Certificate#getSubjectDN()
     */
    @Override
    public Principal getSubjectDN() {
        return delegate.getSubjectDN();
    }

    /**
     * @see X509Certificate#getSubjectUniqueID()
     */
    @Override
    public boolean[] getSubjectUniqueID() {
        return delegate.getSubjectUniqueID();
    }

    /**
     * @see X509Certificate#getTBSCertificate()
     */
    @Override
    public byte[] getTBSCertificate() throws CertificateEncodingException {
        return delegate.getTBSCertificate();
    }

    /**
     * @see X509Certificate#getVersion()
     */
    @Override
    public int getVersion() {
        return delegate.getVersion();
    }

    /**
     * @see X509Certificate#hasUnsupportedCriticalExtension()
     */
    @Override
    public boolean hasUnsupportedCriticalExtension() {
        return delegate.hasUnsupportedCriticalExtension();
    }

    /**
     * @see X509Certificate#toString()
     */
    @Override
    public String toString() {
        return delegate.toString();
    }

    /**
     * @see X509Certificate#verify(java.security.PublicKey)
     */
    @Override
    public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException,
        NoSuchProviderException, SignatureException {
        delegate.verify(key);
    }

    /**
     * @see X509Certificate#verify(java.security.PublicKey, java.security.Provider)
     */
    @Override
    public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException, SignatureException {
        delegate.verify(key, sigProvider);
    }
}
