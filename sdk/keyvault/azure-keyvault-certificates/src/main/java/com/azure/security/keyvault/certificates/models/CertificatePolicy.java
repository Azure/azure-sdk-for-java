// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.security.keyvault.certificates.models.webkey.KeyCurveName;
import com.azure.security.keyvault.certificates.models.webkey.KeyType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Certificate Management policy for the {@link Certificate certificate}.
 */
public final class CertificatePolicy {

    /**
     * The subject name. Should be a valid X509 distinguished Name.
     */
    @JsonProperty(value = "subject")
    private String subjectName;

    /**
     * The subject alternative names.
     */
    @JsonProperty(value = "sans")
    private SubjectAlternativeNames subjectAlternativeNames;

    /**
     * The duration that the ceritifcate is valid in months.
     */
    @JsonProperty(value = "validity_months")
    private Integer validityInMonths;

    /**
     * Actions that will be performed by Key Vault over the lifetime of a
     * certificate.
     */
    @JsonProperty(value = "lifetime_actions")
    private List<LifetimeAction> lifetimeActions;

    /**
     * Name of the referenced issuer object or reserved names; for example,
     * 'Self' or 'Unknown'.
     */
    @JsonProperty(value = "name")
    private String issuerName;

    /**
     * Type of certificate to be requested from the issuer provider.
     */
    @JsonProperty(value = "cty")
    private String issuerCertificateTypeRequest;

    /**
     * Indicates if the certificates generated under this policy should be
     * published to certificate transparency logs.
     */
    @JsonProperty(value = "cert_transparency")
    private Boolean certificateTransparency;

    /**
     * The content type of the secret.
     */
    private SecretContentType secretContentType;

    /**
     * Creation time in UTC.
     */
    private OffsetDateTime created;

    /**
     * Last updated time in UTC.
     */
    private OffsetDateTime updated;

    /**
     * Determines whether the object is enabled.
     */
    private Boolean enabled;

    /**
     *  The key configuration of the key backing the certificate.
     */
    private KeyOptions keyOptions;

    /**
     * Creates certificate policy.
     * @param issuerName The issuer name to set.
     * @param subjectName The subject name to set.
     */
    public CertificatePolicy(String issuerName, String subjectName) {
        this.issuerName = issuerName;
        this.subjectName = subjectName;
    }

    CertificatePolicy() {

    }

    /**
     * Get the the UTC time at which certificate policy was created.
     *
     * @return the created UTC time.
     */
    public OffsetDateTime created() {
        return created;
    }

    /**
     * Get the UTC time at which certificate policy was last updated.
     *
     * @return the last updated UTC time.
     */
    public OffsetDateTime updated() {
        return updated;
    }


    /**
     * Get the enabled status.
     *
     * @return the enabled status
     */
    public Boolean enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled status.
     * @param enabled The enabled status to set.
     * @return The enabled status
     */
    public CertificatePolicy enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the contentType value.
     *
     * @return the contentType value
     */
    public SecretContentType secretContentType() {
        return this.secretContentType;
    }


    /**
     * Set the content type.
     *
     * @param secretContentType the content type value to set
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy secretContentType(SecretContentType secretContentType) {
        this.secretContentType = secretContentType;
        return this;
    }

    /**
     * Set the subject value.
     *
     * @param subjectName the subject value to set
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy subjectName(String subjectName) {
        this.subjectName = subjectName;
        return this;
    }

    /**
     * Get the subjectAlternativeNames.
     *
     * @return the subjectAlternativeNames.
     */
    public SubjectAlternativeNames subjectAlternativeNames() {
        return subjectAlternativeNames;
    }

    /**
     * Set the subjectAlternativeNames.
     *
     * @param subjectAlternativeNames the subjectAlternativeNames to set
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy subjectAlternativeNames(SubjectAlternativeNames subjectAlternativeNames) {
        this.subjectAlternativeNames = subjectAlternativeNames;
        return this;
    }

    /**
     * Set the validityInMonths value.
     *
     * @param validityInMonths the validityInMonths value to set
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy validityInMonths(Integer validityInMonths) {
        this.validityInMonths = validityInMonths;
        return this;
    }

    /**
     * Get the Key Configuration.
     * @return the Key Configuration
     */
    public KeyOptions keyOptions() {
        return keyOptions;
    }

    /**
     * Set the Ec Key Configuration.
     *
     * @param ecKeyOptions the ec key options to set
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy keyOptions(ECKeyOptions ecKeyOptions) {
        this.keyOptions = ecKeyOptions;
        return this;
    }

    /**
     * Set the Rsa Key Configuration.
     *
     * @param rsaKeyOptions the rsa key options to set.
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy keyOptions(RSAKeyOptions rsaKeyOptions) {
        this.keyOptions = rsaKeyOptions;
        return this;
    }

    /**
     * Set the name value.
     *
     * @param issuerName the name value to set
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy issuerName(String issuerName) {
        this.issuerName = issuerName;
        return this;
    }

    /**
     * Set the certificateType to request from the issuer.
     *
     * @param issuerCertificateTypeRequest the certificateType to request from issuer.
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy issuerCertificateTypeRequest(String issuerCertificateTypeRequest) {
        this.issuerCertificateTypeRequest = issuerCertificateTypeRequest;
        return this;
    }

    /**
     * Set the certificateTransparency value.
     *
     * @param certificateTransparency the certificateTransparency value to set
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy certificateTransparency(Boolean certificateTransparency) {
        this.certificateTransparency = certificateTransparency;
        return this;
    }

    /**
     * Get the subject value.
     *
     * @return the subject value
     */
    public String subjectName() {
        return this.subjectName;
    }

    /**
     * Get the validityInMonths value.
     *
     * @return the validityInMonths value
     */
    public Integer validityInMonths() {
        return this.validityInMonths;
    }

    /**
     * Get the  issuer name.
     *
     * @return the Issuer name.
     */
    public String issuerName() {
        return issuerName;
    }

    /**
     * Get the certificateType value.
     *
     * @return the certificateType value
     */
    public String issuerCertificateTypeRequest() {
        return this.issuerCertificateTypeRequest;
    }

    /**
     * Get the certificateTransparency value.
     *
     * @return the certificateTransparency value
     */
    public Boolean certificateTransparency() {
        return this.certificateTransparency;
    }

    /**
     * Set the lifetime actions
     * @param actions the lifetime actions to set.
     * @return the certificate policy object itself.
     */
    public CertificatePolicy lifetimeActions(LifetimeAction... actions) {
        this.lifetimeActions = Arrays.asList(actions);
        return this;
    }

    /**
     * Get the lifetime actions
     * @return the lifetime actions
     */
    public List<LifetimeAction> lifetimeActions() {
        return this.lifetimeActions;
    }


    @JsonProperty("key_props")
    private void unpackKeyProperties(Map<String, Object> keyProps) {

        String keyType = (String) keyProps.get("kty");
        Integer keySize = (Integer) keyProps.get("key_size");
        Boolean exportable = (Boolean) keyProps.get("exportable");
        Boolean reuseKey = (Boolean) keyProps.get("reuseKey");
        KeyCurveName curve = (KeyCurveName) keyProps.get("crv");

        if (keyOptions == null) {
            keyOptions = new KeyOptions();
        }

        keyOptions
            .exportable(exportable)
            .reuseKey(reuseKey);

        switch (keyType) {
            case "RSA":
                keyOptions
                    .keySize(keySize)
                    .keyType(KeyType.RSA);
                break;

            case "RSA-HSM" :
                keyOptions
                    .keySize(keySize)
                    .keyType(KeyType.RSA_HSM);
                break;

            case "EC" :
                keyOptions
                    .curve(curve)
                    .keyType(KeyType.EC)
                    .curve(curve);
                break;

            case "EC-HSM" :
                keyOptions
                    .curve(curve)
                    .keyType(KeyType.EC_HSM);
                break;
            default:
                // should not reach here
                break;
        }
    }


    @JsonProperty("x509_props")
    @SuppressWarnings("unchecked")
    private void unpackX509Properties(Map<String, Object> x509Props) {
        validityInMonths = (Integer) x509Props.get("validity_months");
        subjectName = (String) x509Props.get("subject");

        if (keyOptions == null) {
            keyOptions = new KeyOptions();
        }

        keyOptions
            .enhancedKeyUsage((List<String>) x509Props.get("ekus"))
            .keyUsage((List<KeyUsageType>) x509Props.get("key_usage"));
    }

    @JsonProperty("secret_props")
    private void unpackSecretProperties(Map<String, Object> secretProps) {
        this.secretContentType = SecretContentType.fromString((String) secretProps.get("contentType"));
    }

    @JsonProperty("issuer")
    private void unpackIssuerProperties(Map<String, Object> issuerProps) {
        this.issuerName = (String) issuerProps.get("name");
        this.issuerCertificateTypeRequest = (String) issuerProps.get("cty");
        this.certificateTransparency = (Boolean) issuerProps.get("cert_transparency");

    }


    @JsonProperty("attributes")
    private void unpackAttributes(Map<String, Object> attributes) {
        this.enabled = (Boolean) attributes.get("enabled");
        this.created = epochToOffsetDateTime(attributes.get("created"));
        this.updated = epochToOffsetDateTime(attributes.get("updated"));
    }

    private OffsetDateTime epochToOffsetDateTime(Object epochValue) {
        if (epochValue != null) {
            Instant instant = Instant.ofEpochMilli(((Number) epochValue).longValue() * 1000L);
            return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        return null;
    }
}
