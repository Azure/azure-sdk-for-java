// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.security.keyvault.certificates.models.webkey.CertificateKeyCurveName;
import com.azure.security.keyvault.certificates.models.webkey.CertificateKeyType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
    private Boolean certificateTransparency;

    /**
     * The content type of the secret.
     */
    private CertificateContentType contentType;

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
    private Boolean reuseKey;

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
     * @param subjectName The subject name to set.
     */
    public CertificatePolicy(String issuerName, String subjectName) {
        this.issuerName = issuerName;
        this.subjectName = subjectName;
    }

    CertificatePolicy() {

    }

    /**
     * Get the keyUsage value.
     *
     * @return the keyUsage value
     */
    public List<CertificateKeyUsage> getKeyUsage() {
        return this.keyUsage;
    }

    /**
     * Set the key usage.
     *
     * @param keyUsage the keyUsage value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setKeyUsage(CertificateKeyUsage... keyUsage) {
        this.keyUsage = Arrays.asList(keyUsage);
        return this;
    }

    /**
     * Get the enhanced key usage value.
     *
     * @return the enhanced key usage value
     */
    public List<String> getEnhancedKeyUsage() {
        return this.enhancedKeyUsage;
    }

    /**
     * Set the enhanced key usage value.
     *
     * @param ekus the ekus value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setEnhancedKeyUsage(List<String> ekus) {
        this.enhancedKeyUsage = ekus;
        return this;
    }

    /**
     * Get the exportable value.
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
     * Get the keyType value.
     *
     * @return the keyType value
     */
    public CertificateKeyType getKeyType() {
        return this.keyType;
    }

    /**
     * Get the keyType value.
     *
     * @param keyType the key type
     * @return the key type
     */
    public CertificatePolicy setKeyType(CertificateKeyType keyType) {
        this.keyType = keyType;
        return this;
    }

    /**
     * Get the keySize value.
     *
     * @return the keySize value
     */
    public Integer getKeySize() {
        return this.keySize;
    }

    /**
     * Get the reuseKey value.
     *
     * @return the reuseKey value
     */
    public Boolean isReuseKey() {
        return this.reuseKey;
    }

    /**
     * Set the reuseKey value.
     *
     * @param reuseKey the reuseKey value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setReuseKey(Boolean reuseKey) {
        this.reuseKey = reuseKey;
        return this;
    }

    /**
     * Get the curve value.
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
    public OffsetDateTime getCreated() {
        return created;
    }

    /**
     * Get the UTC time at which certificate policy was last updated.
     *
     * @return the last updated UTC time.
     */
    public OffsetDateTime getUpdated() {
        return updated;
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
     * Get the contentType value.
     *
     * @return the contentType value
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
        this.subjectName = subjectName;
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
     * Set the validityInMonths value.
     *
     * @param validityInMonths the validityInMonths value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setValidityInMonths(Integer validityInMonths) {
        this.validityInMonths = validityInMonths;
        return this;
    }

    /**
     * Set the keySize value.
     *
     * @param keySize the keySize value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setKeySize(Integer keySize) {
        this.keySize = keySize;
        return this;
    }

    /**
     * Set the curve value.
     *
     * @param keyCurveName the curve value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setKeyCurveName(CertificateKeyCurveName keyCurveName) {
        this.keyCurveName = keyCurveName;
        return this;
    }

    /**
     * Set the name value.
     *
     * @param issuerName the name value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setIssuerName(String issuerName) {
        this.issuerName = issuerName;
        return this;
    }

    /**
     * Set the certificateType to request from the issuer.
     *
     * @param certificateType the certificateType to request from issuer.
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setCertificateType(String certificateType) {
        this.certificateType = certificateType;
        return this;
    }

    /**
     * Set the certificateTransparency value.
     *
     * @param certificateTransparency the certificateTransparency value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setCertificateTransparency(Boolean certificateTransparency) {
        this.certificateTransparency = certificateTransparency;
        return this;
    }

    /**
     * Get the subject value.
     *
     * @return the subject name
     */
    public String getSubjectName() {
        return this.subjectName;
    }

    /**
     * Get the validityInMonths value.
     *
     * @return the validityInMonths value
     */
    public Integer getValidityInMonths() {
        return this.validityInMonths;
    }

    /**
     * Get the  issuer name.
     *
     * @return the Issuer name.
     */
    public String getIssuerName() {
        return issuerName;
    }

    /**
     * Get the certificateType value.
     *
     * @return the certificateType value
     */
    public String getCertificateType() {
        return this.certificateType;
    }

    /**
     * Get the certificateTransparency value.
     *
     * @return the certificateTransparency value
     */
    public Boolean isCertificateTransparency() {
        return this.certificateTransparency;
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


    @JsonProperty("key_props")
    private void unpackKeyProperties(Map<String, Object> keyProps) {

        this.keyType = CertificateKeyType.fromString((String) keyProps.get("kty"));
        this.keySize = (Integer) keyProps.get("key_size");
        this.exportable = (Boolean) keyProps.get("exportable");
        this.reuseKey = (Boolean) keyProps.get("reuseKey");
        this.keyCurveName = keyProps.containsKey("crv") ? CertificateKeyCurveName.fromString((String) keyProps.get("crv")) : null;
    }


    @JsonProperty("x509_props")
    @SuppressWarnings("unchecked")
    private void unpackX509Properties(Map<String, Object> x509Props) {
        validityInMonths = (Integer) x509Props.get("validity_months");
        subjectName = (String) x509Props.get("subject");

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
        this.certificateTransparency = (Boolean) issuerProps.get("cert_transparency");
    }

    @JsonProperty("lifetime_actions")
    @SuppressWarnings("unchecked")
    private void unpackLifeTimeActions(List<Object> lifetimeActions) {
        List<LifetimeAction> actions = new ArrayList<>();

        for (Object action: lifetimeActions) {
            Map<String, Object> map = (Map<String, Object>) action;
            Integer lifetimePercentageTrigger = null;
            Integer daysBeforeExpiryTrigger = null;
            LifetimeActionType actionType = null;
            if (map.containsKey("trigger")) {
                Map<String, Object> trigger = (Map<String, Object>) map.get("trigger");
                lifetimePercentageTrigger = trigger.containsKey("lifetime_percentage") ? (Integer) trigger.get("lifetime_percentage") : null;
                daysBeforeExpiryTrigger = trigger.containsKey("days_before_expiry") ? (Integer) trigger.get("days_before_expiry") : null;
            }

            if (map.containsKey("action")) {
                Map<String, Object> lifetimeAction = (Map<String, Object>) map.get("action");
                actionType = lifetimeAction.containsKey("action_type") ? LifetimeActionType.fromString((String) lifetimeAction.get("action_type")) : null;
            }
            actions.add(new LifetimeAction(actionType).setLifetimePercentage(lifetimePercentageTrigger).setDaysBeforeExpiry(daysBeforeExpiryTrigger));
        }

        this.lifetimeActions = actions;
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
