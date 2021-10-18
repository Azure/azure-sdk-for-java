// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.util.function.BiConsumer;

/**
 * Set the options used to validate an attestation token.
 *
 * <p>
 * For each {@link AttestationToken} object, there are several elements which can be validated:
 *</p>
 * <ul>
 *     <li>The token signature (if it is signed)</li>
 *     <li>The token expiration time (if it has an expiration time)</li>
 *     <li>The token 'not before' time (if it has a not before time)</li>
 *     <li>The issuer of the token</li>
 *     <li>Any customer provided validations.</li>
 * </ul>
 * <p>
 *     The AttestationTokenValidationOptions API allows customers to control various elements of the token validation.
 *     It also provides a mechanism for customers to provide their own validations to the validations performed
 *     by the client.
 * </p>
 *
 */

@Fluent
public class AttestationTokenValidationOptions {
    private boolean validateToken;
    private String expectedIssuer;
    private boolean validateExpiresOn;
    private boolean validateNotBefore;
    private Duration validationSlack;
    private BiConsumer<AttestationToken, AttestationSigner> validationCallback;

    /**
     * Creates a new instance of the AttestationTokenValidationOptions with default settings.
     */
    public AttestationTokenValidationOptions() {
        validateToken = true;
        validateExpiresOn = true;
        validateNotBefore = true;
        validationSlack = Duration.ZERO;
        validationCallback = null;
    }

    /**
     * Sets whether the token is to be validated at all. If the validateToken parameter is set to false,
     * then no validations will be performed (default: true)
     * @param validateToken - indicates whether or not the token should be validated.
     * @return this AttestationTokenValidationOptions object.
     */
    public AttestationTokenValidationOptions setValidateToken(boolean validateToken) {
        this.validateToken = validateToken;
        return this;
    }

    /**
     * Returns if the returned attestation token should be validated at all.
     * @return a boolean indicating if the attestation token should be validated.
     */
    public boolean getValidateToken() {
        return validateToken;
    }

    /**
     * Sets the expected issuer of the token. When the token is validated, if this is set,
     * the attestation API verifies that the issuer of the token matches the expected issuer (default: null)
     * @param expectedIssuer - indicates the expected issuer of the attestation token.
     * @return this AttestationTokenValidationOptions object.
     */
    public AttestationTokenValidationOptions setExpectedIssuer(String expectedIssuer) {
        this.expectedIssuer = expectedIssuer;
        return this;
    }

    /**
     * Returns the expected issuer of the attestation token.
     * @return the expected issuer of the attestation token.
     */
    public String getExpectedIssuer() {
        return expectedIssuer;
    }

    /**
     * Sets a validation callback to allow the developer to provide additional validations beyond the basic
     * validations performed by the attestation client.
     * <p>
     *     If the developer validation fails, the callback is expected to throw an exception which indicates
     *     the reason for the failure.
     * </p>
     *
     * @param callback - Customer provided callback which can perform additional validations beyond
     *                 the default validations.
     * @return this AttestationTokenValidationOptions object.
     */
    public AttestationTokenValidationOptions setValidationCallback(BiConsumer<AttestationToken, AttestationSigner> callback) {
        this.validationCallback = callback;
        return this;
    }

    /**
     * Returns the token validation callback.
     * @return the token validation callback if set.
     */
    public BiConsumer<AttestationToken, AttestationSigner> getValidationCallback() {
        return this.validationCallback;
    }

    /**
     * Enable or Disable expiration time validation.
     * @param validateExpiresOn - sets whether the expiration time should be validated.
     * @return this AttestationTokenValidationOptions object.
     */
    public AttestationTokenValidationOptions setValidateExpiresOn(boolean validateExpiresOn) {
        this.validateExpiresOn = validateExpiresOn;
        return this;
    }

    /**
     * Returns whether expiration time should be validated.
     * @return the current state of the ExpiresOn validation.
     */
    public boolean getValidateExpiresOn() {
        return validateExpiresOn;
    }

    /**
     * Enable or Disable NotBefore validation.
     * @param validateNotBefore - sets whether the NotBefore time should be validated.
     * @return this AttestationTokenValidationOptions object.
     */
    public AttestationTokenValidationOptions setValidateNotBefore(boolean validateNotBefore) {
        this.validateNotBefore = validateNotBefore;
        return this;
    }
    /**
     * Returns whether expiration time should be validated.
     * @return the current state of the ExpiresOn validation.
     */
    public boolean getValidateNotBefore() {
        return validateNotBefore;
    }

    /**
     * Sets the validation slack allowed when measuring times.
     * @param slack - sets the allowable amount of slack.
     * @return this AttestationTokenValidationOptions object.
     */
    public AttestationTokenValidationOptions setValidationSlack(Duration slack) {
        this.validationSlack = slack;
        return this;
    }

    /**
     * Returns the allowable slack for token time validations
     * @return the allowable slack for token time validations.
     */
    public Duration getValidationSlack() {
        return validationSlack;
    }
}
