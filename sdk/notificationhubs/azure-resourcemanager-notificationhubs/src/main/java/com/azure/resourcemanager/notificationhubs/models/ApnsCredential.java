// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.notificationhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.notificationhubs.fluent.models.ApnsCredentialProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Description of a NotificationHub ApnsCredential.
 */
@Fluent
public final class ApnsCredential {
    /*
     * Description of a NotificationHub ApnsCredential.
     */
    @JsonProperty(value = "properties", required = true)
    private ApnsCredentialProperties innerProperties = new ApnsCredentialProperties();

    /**
     * Creates an instance of ApnsCredential class.
     */
    public ApnsCredential() {
    }

    /**
     * Get the innerProperties property: Description of a NotificationHub ApnsCredential.
     * 
     * @return the innerProperties value.
     */
    private ApnsCredentialProperties innerProperties() {
        return this.innerProperties;
    }

    /**
     * Get the apnsCertificate property: Gets or sets the APNS certificate.
     * 
     * @return the apnsCertificate value.
     */
    public String apnsCertificate() {
        return this.innerProperties() == null ? null : this.innerProperties().apnsCertificate();
    }

    /**
     * Set the apnsCertificate property: Gets or sets the APNS certificate.
     * 
     * @param apnsCertificate the apnsCertificate value to set.
     * @return the ApnsCredential object itself.
     */
    public ApnsCredential withApnsCertificate(String apnsCertificate) {
        if (this.innerProperties() == null) {
            this.innerProperties = new ApnsCredentialProperties();
        }
        this.innerProperties().withApnsCertificate(apnsCertificate);
        return this;
    }

    /**
     * Get the certificateKey property: Gets or sets the certificate key.
     * 
     * @return the certificateKey value.
     */
    public String certificateKey() {
        return this.innerProperties() == null ? null : this.innerProperties().certificateKey();
    }

    /**
     * Set the certificateKey property: Gets or sets the certificate key.
     * 
     * @param certificateKey the certificateKey value to set.
     * @return the ApnsCredential object itself.
     */
    public ApnsCredential withCertificateKey(String certificateKey) {
        if (this.innerProperties() == null) {
            this.innerProperties = new ApnsCredentialProperties();
        }
        this.innerProperties().withCertificateKey(certificateKey);
        return this;
    }

    /**
     * Get the endpoint property: Gets or sets the endpoint of this credential.
     * 
     * @return the endpoint value.
     */
    public String endpoint() {
        return this.innerProperties() == null ? null : this.innerProperties().endpoint();
    }

    /**
     * Set the endpoint property: Gets or sets the endpoint of this credential.
     * 
     * @param endpoint the endpoint value to set.
     * @return the ApnsCredential object itself.
     */
    public ApnsCredential withEndpoint(String endpoint) {
        if (this.innerProperties() == null) {
            this.innerProperties = new ApnsCredentialProperties();
        }
        this.innerProperties().withEndpoint(endpoint);
        return this;
    }

    /**
     * Get the thumbprint property: Gets or sets the APNS certificate Thumbprint.
     * 
     * @return the thumbprint value.
     */
    public String thumbprint() {
        return this.innerProperties() == null ? null : this.innerProperties().thumbprint();
    }

    /**
     * Set the thumbprint property: Gets or sets the APNS certificate Thumbprint.
     * 
     * @param thumbprint the thumbprint value to set.
     * @return the ApnsCredential object itself.
     */
    public ApnsCredential withThumbprint(String thumbprint) {
        if (this.innerProperties() == null) {
            this.innerProperties = new ApnsCredentialProperties();
        }
        this.innerProperties().withThumbprint(thumbprint);
        return this;
    }

    /**
     * Get the keyId property: Gets or sets a 10-character key identifier (kid) key, obtained from
     * your developer account.
     * 
     * @return the keyId value.
     */
    public String keyId() {
        return this.innerProperties() == null ? null : this.innerProperties().keyId();
    }

    /**
     * Set the keyId property: Gets or sets a 10-character key identifier (kid) key, obtained from
     * your developer account.
     * 
     * @param keyId the keyId value to set.
     * @return the ApnsCredential object itself.
     */
    public ApnsCredential withKeyId(String keyId) {
        if (this.innerProperties() == null) {
            this.innerProperties = new ApnsCredentialProperties();
        }
        this.innerProperties().withKeyId(keyId);
        return this;
    }

    /**
     * Get the appName property: Gets or sets the name of the application.
     * 
     * @return the appName value.
     */
    public String appName() {
        return this.innerProperties() == null ? null : this.innerProperties().appName();
    }

    /**
     * Set the appName property: Gets or sets the name of the application.
     * 
     * @param appName the appName value to set.
     * @return the ApnsCredential object itself.
     */
    public ApnsCredential withAppName(String appName) {
        if (this.innerProperties() == null) {
            this.innerProperties = new ApnsCredentialProperties();
        }
        this.innerProperties().withAppName(appName);
        return this;
    }

    /**
     * Get the appId property: Gets or sets the issuer (iss) registered claim key, whose value is
     * your 10-character Team ID, obtained from your developer account.
     * 
     * @return the appId value.
     */
    public String appId() {
        return this.innerProperties() == null ? null : this.innerProperties().appId();
    }

    /**
     * Set the appId property: Gets or sets the issuer (iss) registered claim key, whose value is
     * your 10-character Team ID, obtained from your developer account.
     * 
     * @param appId the appId value to set.
     * @return the ApnsCredential object itself.
     */
    public ApnsCredential withAppId(String appId) {
        if (this.innerProperties() == null) {
            this.innerProperties = new ApnsCredentialProperties();
        }
        this.innerProperties().withAppId(appId);
        return this;
    }

    /**
     * Get the token property: Gets or sets provider Authentication Token, obtained through your
     * developer account.
     * 
     * @return the token value.
     */
    public String token() {
        return this.innerProperties() == null ? null : this.innerProperties().token();
    }

    /**
     * Set the token property: Gets or sets provider Authentication Token, obtained through your
     * developer account.
     * 
     * @param token the token value to set.
     * @return the ApnsCredential object itself.
     */
    public ApnsCredential withToken(String token) {
        if (this.innerProperties() == null) {
            this.innerProperties = new ApnsCredentialProperties();
        }
        this.innerProperties().withToken(token);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (innerProperties() == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Missing required property innerProperties in model ApnsCredential"));
        } else {
            innerProperties().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(ApnsCredential.class);
}
