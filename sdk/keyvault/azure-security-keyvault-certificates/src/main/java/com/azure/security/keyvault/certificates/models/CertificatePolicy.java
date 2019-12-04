// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Certificate Management policy for the {@link KeyVaultCertificate certificate}.
 */
public final class CertificatePolicy {

    /**
     * The subject name. Should be a valid X509 distinguished Name.
     */
    @JsonProperty(value = "subject")
    private String subject;

    /**
     * The subject alternative names.
     */
    @JsonProperty(value = "sans")
    private SubjectAlternativeNames subjectAlternativeNames;

    /**
     * The duration that the certificate is valid in months.
     */
    @JsonProperty(value = "validity_months")
    private Integer validityInMonths;

    /**
     * Actions that will be performed by Key Vault over the lifetime of a
     * certificate.
     */
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
    private String certificateType;

    /**
     * Indicates if the certificates generated under this policy should be
     * published to certificate transparency logs.
     */
    @JsonProperty(value = "cert_transparency")
    private Boolean certificateTransparent;

    /**
     * The content type of the secret.
     */
    private CertificateContentType contentType;

    /**
     * Creation time in UTC.
     */
    private OffsetDateTime createdOn;

    /**
     * Last updated time in UTC.
     */
    private OffsetDateTime updatedOn;

    /**
     * Determines whether the object is enabled.
     */
    private Boolean enabled;

    /**
     * Indicates if the private key can be exported.
     */
    @JsonProperty(value = "exportable")
    private Boolean exportable;

    /**
     * The type of key pair to be used for the certificate. Possible values
     * include: 'EC', 'EC-HSM', 'RSA', 'RSA-HSM', 'oct'.
     */
    @JsonProperty(value = "kty")
    private CertificateKeyType keyType;

    /**
     * The key size in bits. For example: 2048, 3072, or 4096 for RSA.
     */
    @JsonProperty(value = "key_size")
    private Integer keySize;

    /**
     * Indicates if the same key pair will be used on certificate renewal.
     */
    @JsonProperty(value = "reuse_key")
    private Boolean keyReusable;

    /**
     * Elliptic curve name. For valid values, see KeyCurveName. Possible
     * values include: 'P-256', 'P-384', 'P-521', 'P-256K'.
     */
    @JsonProperty(value = "crv")
    private CertificateKeyCurveName keyCurveName;

    /**
     * List of key usages.
     */
    @JsonProperty(value = "key_usage")
    private List<CertificateKeyUsage> keyUsage;

    /**
     * The enhanced key usage.
     */
    @JsonProperty(value = "ekus")
    private List<String> enhancedKeyUsage;

    /**
     * Creates certificate policy.
     * @param issuerName The issuer name to set.
     * @param subject The subject name to set.
     */
    public CertificatePolicy(String issuerName, String subject) {
        this.issuerName = issuerName;
        this.subject = subject;
    }

    /**
     * Creates certificate policy.
     * @param issuerName The issuer name to set.
     * @param subjectAlternativeNames The subject alternative names to set.
     */
    public CertificatePolicy(String issuerName, SubjectAlternativeNames subjectAlternativeNames) {
        this.issuerName = issuerName;
        this.subjectAlternativeNames = subjectAlternativeNames;
    }

    /**
     * Creates certificate policy.
     * @param issuerName The issuer name to set.
     * @param subject The subject name to set.
     * @param subjectAlternativeNames The subject alternative names to set.
     */
    public CertificatePolicy(String issuerName, String subject, SubjectAlternativeNames subjectAlternativeNames) {
        this.issuerName = issuerName;
        this.subject = subject;
        this.subjectAlternativeNames = subjectAlternativeNames;
    }


    CertificatePolicy() {

    }

    /**
     * Get the key usage.
     *
     * @return the key usage
     */
    public List<CertificateKeyUsage> getKeyUsage() {
        return this.keyUsage;
    }

    /**
     * Set the key usage.
     *
     * @param keyUsage the key usage value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setKeyUsage(CertificateKeyUsage... keyUsage) {
        this.keyUsage = Arrays.asList(keyUsage);
        return this;
    }

    /**
     * Get the enhanced key usage.
     *
     * @return the enhanced key usage
     */
    public List<String> getEnhancedKeyUsage() {
        return this.enhancedKeyUsage;
    }

    /**
     * Set the enhanced key usage.
     *
     * @param ekus the ekus value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setEnhancedKeyUsage(List<String> ekus) {
        this.enhancedKeyUsage = ekus;
        return this;
    }

    /**
     * Get the exportable.
     *
     * @return the exportable value
     */
    public Boolean isExportable() {
        return this.exportable;
    }

    /**
     * Set the exportable value.
     *
     * @param exportable the exportable value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setExportable(Boolean exportable) {
        this.exportable = exportable;
        return this;
    }

    /**
     * Get the key type.
     *
     * @return the key type value
     */
    public CertificateKeyType getKeyType() {
        return this.keyType;
    }

    /**
     * Get the key type.
     *
     * @param keyType the key type
     * @return the key type
     */
    public CertificatePolicy setKeyType(CertificateKeyType keyType) {
        this.keyType = keyType;
        return this;
    }

    /**
     * Get the key size.
     *
     * @return the key size
     */
    public Integer getKeySize() {
        return this.keySize;
    }

    /**
     * Get the key reuse status.
     *
     * @return the key reuse status
     */
    public Boolean isKeyReusable() {
        return this.keyReusable;
    }

    /**
     * Set the reuse key value.
     *
     * @param keyReusable the reuseKey value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setKeyReusable(Boolean keyReusable) {
        this.keyReusable = keyReusable;
        return this;
    }

    /**
     * Get the key curve.
     *
     * @return the curve value
     */
    public CertificateKeyCurveName getKeyCurveName() {
        return this.keyCurveName;
    }

    /**
     * Get the the UTC time at which certificate policy was created.
     *
     * @return the created UTC time.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    /**
     * Get the UTC time at which certificate policy was last updated.
     *
     * @return the last updated UTC time.
     */
    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }


    /**
     * Get the enabled status.
     *
     * @return the enabled status
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set the enabled status.
     * @param enabled The enabled status to set.
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the content type.
     *
     * @return the content type
     */
    public CertificateContentType getContentType() {
        return this.contentType;
    }


    /**
     * Set the content type.
     *
     * @param contentType the content type value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setContentType(CertificateContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set the subject value.
     *
     * @param subjectName the subject value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy getSubjectName(String subjectName) {
        this.subject = subjectName;
        return this;
    }

    /**
     * Get the subjectAlternativeNames.
     *
     * @return the subjectAlternativeNames.
     */
    public SubjectAlternativeNames getSubjectAlternativeNames() {
        return subjectAlternativeNames;
    }

    /**
     * Set the subjectAlternativeNames.
     *
     * @param subjectAlternativeNames the subjectAlternativeNames to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setSubjectAlternativeNames(SubjectAlternativeNames subjectAlternativeNames) {
        this.subjectAlternativeNames = subjectAlternativeNames;
        return this;
    }

    /**
     * Set the validity in months.
     *
     * @param validityInMonths the validityInMonths value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setValidityInMonths(Integer validityInMonths) {
        this.validityInMonths = validityInMonths;
        return this;
    }

    /**
     * Set the key size.
     *
     * @param keySize the key size value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setKeySize(Integer keySize) {
        this.keySize = keySize;
        return this;
    }

    /**
     * Set the key curve.
     *
     * @param keyCurveName the key curve value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setKeyCurveName(CertificateKeyCurveName keyCurveName) {
        this.keyCurveName = keyCurveName;
        return this;
    }

    /**
     * Set the issuer name.
     *
     * @param issuerName the issuer name to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setIssuerName(String issuerName) {
        this.issuerName = issuerName;
        return this;
    }

    /**
     * Set the certificate type to request from the issuer.
     *
     * @param certificateType the certificateType to request from issuer.
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setCertificateType(String certificateType) {
        this.certificateType = certificateType;
        return this;
    }

    /**
     * Set the certificate transparency status.
     *
     * @param certificateTransparent the certificateTransparency status to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setCertificateTransparent(Boolean certificateTransparent) {
        this.certificateTransparent = certificateTransparent;
        return this;
    }

    /**
     * Get the subject.
     *
     * @return the subject
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Get the validity in months.
     *
     * @return the validity in months
     */
    public Integer getValidityInMonths() {
        return this.validityInMonths;
    }

    /**
     * Get the  issuer name.
     *
     * @return the issuer name.
     */
    public String getIssuerName() {
        return issuerName;
    }

    /**
     * Get the certificate type.
     *
     * @return the certificate type
     */
    public String getCertificateType() {
        return this.certificateType;
    }

    /**
     * Get the certificate transparency status.
     *
     * @return the certificate transparency status
     */
    public Boolean isCertificateTransparent() {
        return this.certificateTransparent;
    }

    /**
     * Set the lifetime actions
     * @param actions the lifetime actions to set.
     * @return the updated certificate policy object itself.
     */
    public CertificatePolicy setLifetimeActions(LifetimeAction... actions) {
        this.lifetimeActions = Arrays.asList(actions);
        return this;
    }

    /**
     * Get the lifetime actions
     * @return the lifetime actions
     */
    public List<LifetimeAction> getLifetimeActions() {
        return this.lifetimeActions;
    }

    /**
     * Get the default certificate policy.
     * @return the default certificate policy.
     */
    public static CertificatePolicy getDefault() {
        return  new CertificatePolicy("Self", "CN=DefaultPolicy");
    }


    @JsonProperty("key_props")
    private void unpackKeyProperties(Map<String, Object> keyProps) {

        this.keyType = CertificateKeyType.fromString((String) keyProps.get("kty"));
        this.keySize = (Integer) keyProps.get("key_size");
        this.exportable = (Boolean) keyProps.get("exportable");
        this.keyReusable = (Boolean) keyProps.get("reuse_key");
        this.keyCurveName = keyProps.containsKey("crv") ? CertificateKeyCurveName.fromString((String) keyProps.get("crv")) : null;
    }


    @JsonProperty("x509_props")
    @SuppressWarnings("unchecked")
    private void unpackX509Properties(Map<String, Object> x509Props) {
        validityInMonths = (Integer) x509Props.get("validity_months");
        subject = (String) x509Props.get("subject");

        this.enhancedKeyUsage = (x509Props.containsKey("ekus") ? parseEnhancedKeyUsage((List<Object>) x509Props.get("ekus")) : null);
        this.keyUsage = (x509Props.containsKey("key_usage") ? parseKeyUsage((List<Object>) x509Props.get("key_usage")) : null);
    }

    @SuppressWarnings("unchecked")
    private List<CertificateKeyUsage> parseKeyUsage(List<Object> keyUsages) {
        List<CertificateKeyUsage> output = new ArrayList<>();

        for (Object keyUsage : keyUsages) {
            CertificateKeyUsage type = CertificateKeyUsage.fromString((String) keyUsage);
            output.add(type);
        }
        return output;
    }

    @SuppressWarnings("unchecked")
    private List<String> parseEnhancedKeyUsage(List<Object> keyUsages) {
        List<String> output = new ArrayList<>();

        for (Object keyUsage : keyUsages) {
            output.add((String) keyUsage);
        }
        return output;
    }

    @JsonProperty("secret_props")
    private void unpackSecretProperties(Map<String, Object> secretProps) {
        this.contentType = secretProps.containsKey("contentType") ? CertificateContentType.fromString((String) secretProps.get("contentType")) : null;
    }

    @JsonProperty("issuer")
    private void unpackIssuerProperties(Map<String, Object> issuerProps) {
        this.issuerName = (String) issuerProps.get("name");
        this.certificateType = (String) issuerProps.get("cty");
        this.certificateTransparent = (Boolean) issuerProps.get("cert_transparency");
    }

    @JsonProperty("lifetime_actions")
    @SuppressWarnings("unchecked")
    private void unpackLifeTimeActions(List<Object> lifetimeActions) {
        List<LifetimeAction> actions = new ArrayList<>();

        for (Object action: lifetimeActions) {
            Map<String, Object> map = (Map<String, Object>) action;
            Integer lifetimePercentageTrigger = null;
            Integer daysBeforeExpiryTrigger = null;
            CertificatePolicyAction actionType = null;
            if (map.containsKey("trigger")) {
                Map<String, Object> trigger = (Map<String, Object>) map.get("trigger");
                lifetimePercentageTrigger = trigger.containsKey("lifetime_percentage") ? (Integer) trigger.get("lifetime_percentage") : null;
                daysBeforeExpiryTrigger = trigger.containsKey("days_before_expiry") ? (Integer) trigger.get("days_before_expiry") : null;
            }

            if (map.containsKey("action")) {
                Map<String, Object> lifetimeAction = (Map<String, Object>) map.get("action");
                actionType = lifetimeAction.containsKey("action_type") ? CertificatePolicyAction.fromString((String) lifetimeAction.get("action_type")) : null;
            }
            actions.add(new LifetimeAction(actionType).setLifetimePercentage(lifetimePercentageTrigger).setDaysBeforeExpiry(daysBeforeExpiryTrigger));
        }

        this.lifetimeActions = actions;
    }


    @JsonProperty("attributes")
    private void unpackAttributes(Map<String, Object> attributes) {
        this.enabled = (Boolean) attributes.get("enabled");
        this.createdOn = epochToOffsetDateTime(attributes.get("created"));
        this.updatedOn = epochToOffsetDateTime(attributes.get("updated"));
    }

    private OffsetDateTime epochToOffsetDateTime(Object epochValue) {
        if (epochValue != null) {
            Instant instant = Instant.ofEpochMilli(((Number) epochValue).longValue() * 1000L);
            return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        return null;
    }
}
