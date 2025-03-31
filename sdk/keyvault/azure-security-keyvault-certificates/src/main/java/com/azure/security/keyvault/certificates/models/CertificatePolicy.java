// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.security.keyvault.certificates.implementation.CertificatePolicyHelper;
import com.azure.security.keyvault.certificates.implementation.models.CertificateAttributes;
import com.azure.security.keyvault.certificates.implementation.models.IssuerParameters;
import com.azure.security.keyvault.certificates.implementation.models.KeyProperties;
import com.azure.security.keyvault.certificates.implementation.models.SecretProperties;
import com.azure.security.keyvault.certificates.implementation.models.X509CertificateProperties;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The Certificate Management policy for the {@link KeyVaultCertificate certificate}.
 */
public final class CertificatePolicy implements JsonSerializable<CertificatePolicy> {
    static {
        CertificatePolicyHelper.setAccessor(new CertificatePolicyHelper.CertificatePolicyAccessor() {
            @Override
            public CertificatePolicy
                createPolicy(com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy impl) {
                return new CertificatePolicy(impl);
            }

            @Override
            public com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy
                getPolicy(CertificatePolicy policy) {
                return policy == null ? null : policy.impl;
            }
        });
    }

    private final com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy impl;

    private List<LifetimeAction> lifetimeActions;

    /**
     * Creates certificate policy.
     * @param issuerName The issuer name to set.
     * @param subject The subject name to set.
     */
    public CertificatePolicy(String issuerName, String subject) {
        this.impl = new com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy()
            .setIssuerParameters(new IssuerParameters().setName(issuerName))
            .setX509CertificateProperties(new X509CertificateProperties().setSubject(subject));
    }

    /**
     * Creates certificate policy.
     * @param issuerName The issuer name to set.
     * @param subjectAlternativeNames The subject alternative names to set.
     */
    public CertificatePolicy(String issuerName, SubjectAlternativeNames subjectAlternativeNames) {
        this.impl = new com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy()
            .setIssuerParameters(new IssuerParameters().setName(issuerName))
            .setX509CertificateProperties(
                new X509CertificateProperties().setSubjectAlternativeNames(subjectAlternativeNames));
    }

    /**
     * Creates certificate policy.
     * @param issuerName The issuer name to set.
     * @param subject The subject name to set.
     * @param subjectAlternativeNames The subject alternative names to set.
     */
    public CertificatePolicy(String issuerName, String subject, SubjectAlternativeNames subjectAlternativeNames) {
        this.impl = new com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy()
            .setIssuerParameters(new IssuerParameters().setName(issuerName))
            .setX509CertificateProperties(new X509CertificateProperties().setSubject(subject)
                .setSubjectAlternativeNames(subjectAlternativeNames));
    }

    private CertificatePolicy(com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy impl) {
        this.impl = impl;
    }

    /**
     * Get the key usage.
     *
     * @return the key usage
     */
    public List<CertificateKeyUsage> getKeyUsage() {
        return impl.getX509CertificateProperties().getKeyUsage();
    }

    /**
     * Set the key usage.
     *
     * @param keyUsage the key usage value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setKeyUsage(CertificateKeyUsage... keyUsage) {
        if (keyUsage == null) {
            impl.getX509CertificateProperties().setKeyUsage(null);
        } else {
            impl.getX509CertificateProperties().setKeyUsage(Arrays.asList(keyUsage));
        }

        return this;
    }

    /**
     * Get the enhanced key usage.
     *
     * @return the enhanced key usage
     */
    public List<String> getEnhancedKeyUsage() {
        return impl.getX509CertificateProperties().getEkus();
    }

    /**
     * Set the enhanced key usage.
     *
     * @param ekus the ekus value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setEnhancedKeyUsage(List<String> ekus) {
        impl.getX509CertificateProperties().setEkus(ekus);

        return this;
    }

    /**
     * Get the exportable.
     *
     * @return the exportable value
     */
    public Boolean isExportable() {
        return impl.getKeyProperties() == null ? null : impl.getKeyProperties().isExportable();
    }

    /**
     * Set the exportable value.
     *
     * @param exportable the exportable value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setExportable(Boolean exportable) {
        if (impl.getKeyProperties() == null) {
            impl.setKeyProperties(new KeyProperties());
        }

        impl.getKeyProperties().setExportable(exportable);
        return this;
    }

    /**
     * Get the key type.
     *
     * @return the key type value
     */
    public CertificateKeyType getKeyType() {
        return impl.getKeyProperties() == null ? null : impl.getKeyProperties().getKeyType();
    }

    /**
     * Get the key type.
     *
     * @param keyType the key type
     * @return the key type
     */
    public CertificatePolicy setKeyType(CertificateKeyType keyType) {
        if (impl.getKeyProperties() == null) {
            impl.setKeyProperties(new KeyProperties());
        }

        impl.getKeyProperties().setKeyType(keyType);
        return this;
    }

    /**
     * Get the key size.
     *
     * @return the key size
     */
    public Integer getKeySize() {
        return impl.getKeyProperties() == null ? null : impl.getKeyProperties().getKeySize();
    }

    /**
     * Get the key reuse status.
     *
     * @return the key reuse status
     */
    public Boolean isKeyReusable() {
        return impl.getKeyProperties() == null ? null : impl.getKeyProperties().isReuseKey();
    }

    /**
     * Set the reuse key value.
     *
     * @param keyReusable the reuseKey value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setKeyReusable(Boolean keyReusable) {
        if (impl.getKeyProperties() == null) {
            impl.setKeyProperties(new KeyProperties());
        }

        impl.getKeyProperties().setReuseKey(keyReusable);
        return this;
    }

    /**
     * Get the key curve.
     *
     * @return the curve value
     */
    public CertificateKeyCurveName getKeyCurveName() {
        return impl.getKeyProperties() == null ? null : impl.getKeyProperties().getCurve();
    }

    /**
     * Get the the UTC time at which certificate policy was created.
     *
     * @return the created UTC time.
     */
    public OffsetDateTime getCreatedOn() {
        return impl.getAttributes() == null ? null : impl.getAttributes().getCreated();
    }

    /**
     * Get the UTC time at which certificate policy was last updated.
     *
     * @return the last updated UTC time.
     */
    public OffsetDateTime getUpdatedOn() {
        return impl.getAttributes() == null ? null : impl.getAttributes().getUpdated();
    }

    /**
     * Get the enabled status.
     *
     * @return the enabled status
     */
    public Boolean isEnabled() {
        return impl.getAttributes() == null ? null : impl.getAttributes().isEnabled();
    }

    /**
     * Set the enabled status.
     * @param enabled The enabled status to set.
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setEnabled(Boolean enabled) {
        if (impl.getAttributes() == null) {
            impl.setAttributes(new CertificateAttributes());
        }

        impl.getAttributes().setEnabled(enabled);
        return this;
    }

    /**
     * Get the content type.
     *
     * @return the content type
     */
    public CertificateContentType getContentType() {
        return impl.getSecretProperties() == null
            ? null
            : CertificateContentType.fromString(impl.getSecretProperties().getContentType());
    }

    /**
     * Set the content type.
     *
     * @param contentType the content type value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setContentType(CertificateContentType contentType) {
        if (impl.getSecretProperties() == null) {
            impl.setSecretProperties(new SecretProperties());
        }

        impl.getSecretProperties().setContentType(Objects.toString(contentType, null));
        return this;
    }

    /**
     * Get the subjectAlternativeNames.
     *
     * @return the subjectAlternativeNames.
     */
    public SubjectAlternativeNames getSubjectAlternativeNames() {
        return impl.getX509CertificateProperties().getSubjectAlternativeNames();
    }

    /**
     * Set the subjectAlternativeNames.
     *
     * @param subjectAlternativeNames the subjectAlternativeNames to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setSubjectAlternativeNames(SubjectAlternativeNames subjectAlternativeNames) {
        impl.getX509CertificateProperties().setSubjectAlternativeNames(subjectAlternativeNames);
        return this;
    }

    /**
     * Set the subject Name.
     *
     * @param subject the subject Name to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setSubject(final String subject) {
        impl.getX509CertificateProperties().setSubject(subject);
        return this;
    }

    /**
     * Set the validity in months.
     *
     * @param validityInMonths the validityInMonths value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setValidityInMonths(Integer validityInMonths) {
        impl.getX509CertificateProperties().setValidityInMonths(validityInMonths);
        return this;
    }

    /**
     * Set the key size.
     *
     * @param keySize the key size value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setKeySize(Integer keySize) {
        if (impl.getKeyProperties() == null) {
            impl.setKeyProperties(new KeyProperties());
        }

        impl.getKeyProperties().setKeySize(keySize);
        return this;
    }

    /**
     * Set the key curve.
     *
     * @param keyCurveName the key curve value to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setKeyCurveName(CertificateKeyCurveName keyCurveName) {
        if (impl.getKeyProperties() == null) {
            impl.setKeyProperties(new KeyProperties());
        }

        impl.getKeyProperties().setCurve(keyCurveName);
        return this;
    }

    /**
     * Set the certificate type to request from the issuer.
     *
     * @param certificateType the certificateType to request from issuer.
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setCertificateType(String certificateType) {
        impl.getIssuerParameters().setCertificateType(certificateType);
        return this;
    }

    /**
     * Set the certificate transparency status.
     *
     * @param certificateTransparent the certificateTransparency status to set
     * @return the updated CertificatePolicy object itself.
     */
    public CertificatePolicy setCertificateTransparent(Boolean certificateTransparent) {
        impl.getIssuerParameters().setCertificateTransparency(certificateTransparent);
        return this;
    }

    /**
     * Get the subject.
     *
     * @return the subject
     */
    public String getSubject() {
        return impl.getX509CertificateProperties().getSubject();
    }

    /**
     * Get the validity in months.
     *
     * @return the validity in months
     */
    public Integer getValidityInMonths() {
        return impl.getX509CertificateProperties().getValidityInMonths();
    }

    /**
     * Get the  issuer name.
     *
     * @return the issuer name.
     */
    public String getIssuerName() {
        return impl.getIssuerParameters().getName();
    }

    /**
     * Get the certificate type.
     *
     * @return the certificate type
     */
    public String getCertificateType() {
        return impl.getIssuerParameters().getCertificateType();
    }

    /**
     * Get the certificate transparency status.
     *
     * @return the certificate transparency status
     */
    public Boolean isCertificateTransparent() {
        return impl.getIssuerParameters().isCertificateTransparency();
    }

    /**
     * Set the lifetime actions
     * @param actions the lifetime actions to set.
     * @return the updated certificate policy object itself.
     */
    public CertificatePolicy setLifetimeActions(LifetimeAction... actions) {
        if (actions == null) {
            impl.setLifetimeActions(null);
            lifetimeActions = null;
        } else {
            lifetimeActions = Arrays.asList(actions);
            List<com.azure.security.keyvault.certificates.implementation.models.LifetimeAction> implActions
                = new ArrayList<>(actions.length);
            for (LifetimeAction action : actions) {
                implActions.add(action.getImpl());
            }
            impl.setLifetimeActions(implActions);
        }

        return this;
    }

    /**
     * Get the lifetime actions
     * @return the lifetime actions
     */
    public List<LifetimeAction> getLifetimeActions() {
        if (lifetimeActions == null && impl.getLifetimeActions() != null) {
            lifetimeActions = new ArrayList<>(impl.getLifetimeActions().size());
            for (com.azure.security.keyvault.certificates.implementation.models.LifetimeAction implAction : impl
                .getLifetimeActions()) {
                lifetimeActions.add(new LifetimeAction(implAction));
            }
        }

        return lifetimeActions;
    }

    /**
     * Get the default certificate policy.
     * @return the default certificate policy.
     */
    public static CertificatePolicy getDefault() {
        return new CertificatePolicy("Self", "CN=DefaultPolicy");
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return impl.toJson(jsonWriter);
    }

    /**
     * Reads a JSON stream into a {@link CertificatePolicy}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link CertificatePolicy} that the JSON stream represented, may return null.
     * @throws IOException If a {@link CertificatePolicy} fails to be read from the {@code jsonReader}.
     */
    public static CertificatePolicy fromJson(JsonReader jsonReader) throws IOException {
        return new CertificatePolicy(
            com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy.fromJson(jsonReader));
    }
}
