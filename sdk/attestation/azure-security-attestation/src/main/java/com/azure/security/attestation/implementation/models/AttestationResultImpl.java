// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.implementation.models;

import com.azure.core.util.CoreUtils;
import com.azure.security.attestation.models.AttestationResult;
import com.azure.security.attestation.models.AttestationSigner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** A Microsoft Azure Attestation response token body - the body of a response token issued by MAA. */

public final class AttestationResultImpl implements com.azure.security.attestation.models.AttestationResult {
    /*
     * Unique Identifier for the token
     */
    private String jti;

    /*
     * The Principal who issued the token
     */
    private String iss;

    /*
     * The time at which the token was issued, in the number of seconds since
     * 1970-01-0T00:00:00Z UTC
     */
    private LocalDateTime iat;

    /*
     * The expiration time after which the token is no longer valid, in the
     * number of seconds since 1970-01-0T00:00:00Z UTC
     */
    private LocalDateTime exp;

    /*
     * The not before time before which the token cannot be considered valid,
     * in the number of seconds since 1970-01-0T00:00:00Z UTC
     */
    private LocalDateTime nbf;

    /*
     * The Nonce input to the attestation request, if provided.
     */
    private String nonce;

    /*
     * The Schema version of this structure. Current Value: 1.0
     */
    private String version;

    /*
     * Runtime Claims
     */
    private Object runtimeClaims;

    /*
     * InitTime Claims
     */
    private Object inittimeClaims;

    /*
     * Policy Generated Claims
     */
    private Object policyClaims;

    /*
     * The Attestation type being attested.
     */
    private String verifierType;

    /*
     * The certificate used to sign the policy object, if specified.
     */
    private AttestationSigner policySigner;

    /*
     * The SHA256 hash of the BASE64URL encoded policy text used for
     * attestation
     */
    private byte[] policyHash;

    /*
     * True if the enclave is debuggable, false otherwise
     */
    private Boolean isDebuggable;

    /*
     * The SGX Product ID for the enclave.
     */
    private Float productId;

    /*
     * The HEX encoded SGX MRENCLAVE value for the enclave.
     */
    private String mrEnclave;

    /*
     * The HEX encoded SGX MRSIGNER value for the enclave.
     */
    private String mrSigner;

    /*
     * The SGX SVN value for the enclave.
     */
    private Float svn;

    /*
     * A copy of the RuntimeData specified as an input to the attest call.
     */
    private byte[] enclaveHeldData;

    /*
     * The SGX SVN value for the enclave.
     */
    private Object sgxCollateral;

    /**
     * Gets the unique identifier property: Unique Identifier for the token.
     *
     * @return the jti value.
     */
    @Override public String getUniqueIdentifier() {
        return this.jti;
    }

    /**
     * Get the iss property: The Principal who issued the token.
     *
     * @return the iss value.
     */
    @Override public String getIssuer() {
        return this.iss;
    }

    /**
     * Get the iat property: The time at which the token was issued, in the number of seconds since 1970-01-0T00:00:00Z
     * UTC.
     *
     * @return the iat value.
     */
    @Override public LocalDateTime getIssuedAt() {
        return this.iat;
    }

    /**
     * Get the exp property: The expiration time after which the token is no longer valid, in the number of seconds
     * since 1970-01-0T00:00:00Z UTC.
     *
     * @return the exp value.
     */
    @Override public LocalDateTime getExpiresOn() {
        return this.exp;
    }


    /**
     * Get the nbf property: The not before time before which the token cannot be considered valid, in the number of
     * seconds since 1970-01-0T00:00:00Z UTC.
     *
     * @return the nbf value.
     */
    @Override public LocalDateTime getNotBefore() {
        return this.nbf;
    }

    /**
     * Get the nonce property: The Nonce input to the attestation request, if provided.
     *
     * @return the nonce value.
     */
    @Override public String getNonce() {
        return this.nonce;
    }

    /**
     * Get the version property: The Schema version of this structure. Current Value: 1.0.
     *
     * @return the version value.
     */
    @Override public String getVersion() {
        return this.version;
    }

    /**
     * Get the runtimeClaims property: Runtime Claims.
     *
     * @return the runtimeClaims value.
     */
    @Override public Object getRuntimeClaims() {
        return this.runtimeClaims;
    }

    /**
     * Get the inittimeClaims property: Inittime Claims.
     *
     * @return the inittimeClaims value.
     */
    @Override public Object getInittimeClaims() {
        return this.inittimeClaims;
    }

    /**
     * Get the policyClaims property: Policy Generated Claims.
     *
     * @return the policyClaims value.
     */
    @Override public Object getPolicyClaims() {
        return this.policyClaims;
    }

    /**
     * Get the verifierType property: The Attestation type being attested.
     *
     * @return the verifierType value.
     */
    @Override public String getVerifierType() {
        return this.verifierType;
    }

    /**
     * Get the policySigner property: The certificate used to sign the policy object, if specified.
     *
     * @return the policySigner value.
     */
    @Override public AttestationSigner getPolicySigner() {
        return this.policySigner;
    }

    /**
     * Get the policyHash property: The SHA256 hash of the BASE64URL encoded policy text used for attestation.
     *
     * @return the policyHash value.
     */
    @Override public byte[] getPolicyHash() {
        return CoreUtils.clone(this.policyHash);
    }

    /**
     * Get the isDebuggable property: True if the enclave is debuggable, false otherwise.
     *
     * @return the isDebuggable value.
     */
    @Override public Boolean isDebuggable() {
        return this.isDebuggable;
    }

    /**
     * Get the productId property: The SGX Product ID for the enclave.
     *
     * @return the productId value.
     */
    @Override public Float getProductId() {
        return this.productId;
    }

    /**
     * Get the mrEnclave property: The HEX encoded SGX MRENCLAVE value for the enclave.
     *
     * @return the mrEnclave value.
     */
    @Override public String getMrEnclave() {
        return this.mrEnclave;
    }

    /**
     * Get the mrSigner property: The HEX encoded SGX MRSIGNER value for the enclave.
     *
     * @return the mrSigner value.
     */
    @Override public String getMrSigner() {
        return this.mrSigner;
    }

    /**
     * Get the svn property: The SGX SVN value for the enclave.
     *
     * @return the svn value.
     */
    @Override public Float getSvn() {
        return this.svn;
    }

    /**
     * Get the enclaveHeldData property: A copy of the RuntimeData specified as an input to the attest call.
     *
     * @return the enclaveHeldData value.
     */
    @Override public byte[] getEnclaveHeldData() {
        return CoreUtils.clone(this.enclaveHeldData);
    }

    /**
     * Get the sgxCollateral property: The SGX SVN value for the enclave.
     *
     * @return the sgxCollateral value.
     */
    @Override public Object getSgxCollateral() {
        return this.sgxCollateral;
    }


    /**
     * Return a public attestation result from the generated result.
     * @param generated - Generated result.
     * @return Public result.
     */
    public static AttestationResult fromGeneratedAttestationResult(com.azure.security.attestation.implementation.models.AttestationResult generated) {
        AttestationResultImpl result = new AttestationResultImpl();
        // MAA Claims:
        result.enclaveHeldData = generated.getEnclaveHeldData();
        result.inittimeClaims = generated.getInittimeClaims();
        result.runtimeClaims = generated.getRuntimeClaims();
        result.policyClaims = generated.getPolicyClaims();
        result.verifierType = generated.getVerifierType();
        result.nonce = generated.getNonce();
        result.version = generated.getVersion();

        // RFC 7519 Claims
        result.iss = generated.getIss();
        result.jti = generated.getJti();

        // RFC 7515 Claims
        result.exp = LocalDateTime.ofEpochSecond(generated.getExp().longValue(), 0, ZoneOffset.UTC);
        result.iat = LocalDateTime.ofEpochSecond(generated.getIat().longValue(), 0, ZoneOffset.UTC);
        result.nbf  = LocalDateTime.ofEpochSecond(generated.getNbf().longValue(), 0, ZoneOffset.UTC);

        // SGX properties.
        result.mrEnclave = generated.getMrEnclave();
        result.mrSigner = generated.getMrSigner();
        result.isDebuggable = generated.isDebuggable();
        result.policyHash = generated.getPolicyHash();
        if (generated.getPolicySigner() != null) {
            result.policySigner = AttestationSignerImpl.fromJsonWebKey(generated.getPolicySigner());
        }
        result.sgxCollateral = generated.getSgxCollateral();
        result.svn = generated.getSvn();
        result.productId = generated.getProductId();

        return result;
    }
}
