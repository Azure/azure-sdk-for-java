// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.certificates.models;

import com.azure.core.implementation.Base64Url;
import com.azure.keyvault.certificates.models.webkey.JsonWebKeyCurveName;
import com.azure.keyvault.certificates.models.webkey.JsonWebKeyType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CertificateBase {

    /**
     * Determines whether the object is enabled.
     */
    private Boolean enabled;

    /**
     * Not before date in UTC.
     */
    private OffsetDateTime notBefore;

    /**
     * The secret version.
     */
    String version;

    /**
     * Expiry date in UTC.
     */
    private OffsetDateTime expires;

    /**
     * Creation time in UTC.
     */
    private OffsetDateTime created;

    /**
     * Last updated time in UTC.
     */
    private OffsetDateTime updated;

    /**
     * Reflects the deletion recovery level currently in effect for certificates in
     * the current vault. If it contains 'Purgeable', the certificate can be
     * permanently deleted by a privileged user; otherwise, only the system can
     * purge the certificate, at the end of the retention interval. Possible values
     * include: 'Purgeable', 'Recoverable+Purgeable', 'Recoverable',
     * 'Recoverable+ProtectedSubscription'.
     */
    private String recoveryLevel;

    /**
     * The Certificate name.
     */
    String name;

    /**
     *  The configuration of key backing the certificate.
     */
    private KeyConfiguration keyConfig;

    /**
     * The certificate id.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /**
     * The key id.
     */
    @JsonProperty(value = "kid", access = JsonProperty.Access.WRITE_ONLY)
    private String keyId;

    /**
     * The content type of the secret.
     */
    private String secretContentType;

    private boolean propertiesEnabled;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    @JsonProperty(value = "tags")
    private Map<String, String> tags;

    /**
     * True if the certificate's lifetime is managed by key vault. If this is a key
     * backing a certificate, then managed will be true.
     */
    @JsonProperty(value = "managed", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean managed;

    /**
     * The subject name. Should be a valid X509 distinguished Name.
     */
    @JsonProperty(value = "subject")
    private String subjectName;

    /**
     * The subject alternative domain names.
     */
    private List<String> subjectAlternativeDomainNames;

    /**
     * The duration that the ceritifcate is valid in months.
     */
    @JsonProperty(value = "validity_months")
    private Integer validityInMonths;

    /**
     * Actions that will be performed by Key Vault over the lifetime of a
     * certificate.
     */
   // @JsonProperty(value = "lifetime_actions")
   // private List<LifetimeAction> lifetimeActions;

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
     * Thumbprint of the certificate. Read Only
     */
    @JsonProperty(value = "x5t", access = JsonProperty.Access.WRITE_ONLY)
    private Base64Url x509Thumbprint;


    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Get the contentType value.
     *
     * @return the contentType value
     */
    public String secretContentType() {
        return this.secretContentType;
    }

    /**
     * Set the contentType value.
     *
     * @param secretContentType the contentType value to set
     * @return the SecretProperties object itself.
     */
    public CertificateBase secretContentType(String secretContentType) {
        this.secretContentType = secretContentType;
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
     * Set the subject value.
     *
     * @param subjectName the subject value to set
     * @return the X509CertificateProperties object itself.
     */
    public CertificateBase subjectName(String subjectName) {
        this.subjectName = subjectName;
        return this;
    }

    /**
     * Set the subjectAlternativeNames value.
     *
     * @param subjectAlternativeDomainNames the subjectAlternativeNames value to set
     * @return the X509CertificateProperties object itself.
     */
    public CertificateBase subjectAlternativeDomainNames(String ... subjectAlternativeDomainNames) {
        this.subjectAlternativeDomainNames = Arrays.asList(subjectAlternativeDomainNames);
        return this;
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
     * Set the validityInMonths value.
     *
     * @param validityInMonths the validityInMonths value to set
     * @return the X509CertificateProperties object itself.
     */
    public CertificateBase validityInMonths(Integer validityInMonths) {
        this.validityInMonths = validityInMonths;
        return this;
    }

    public CertificateBase rsaKeyConfiguration(RSAKeyConfiguration rsaKeyConfiguration) {
        this.keyConfig = rsaKeyConfiguration;
        return this;
    }

    public CertificateBase ecKeyConfiguration(ECKeyConfiguration ecKeyConfiguration) {
        this.keyConfig = ecKeyConfiguration;
        return this;
    }

    /**
     * Get the notBefore UTC time.
     *
     * @return the notBefore UTC time.
     */
    public OffsetDateTime notBefore() {
        return notBefore;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set
     * @return the SecretBase object itself.
     */
    public CertificateBase notBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore;
        return this;
    }

    /**
     * Get the Secret Expiry time in UTC.
     *
     * @return the expires UTC time.
     */
    public OffsetDateTime expires() {
        if (this.expires == null) {
            return null;
        }
        return this.expires;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expires The expiry time to set for the secret.
     * @return the SecretBase object itself.
     */
    public CertificateBase expires(OffsetDateTime expires) {
        this.expires = expires;
        return this;
    }

    /**
     * Get the the UTC time at which secret was created.
     *
     * @return the created UTC time.
     */
    public OffsetDateTime created() {
        return created;
    }

    /**
     * Get the UTC time at which secret was last updated.
     *
     * @return the last updated UTC time.
     */
    public OffsetDateTime updated() {
        return updated;
    }


    /**
     * Get the tags associated with the secret.
     *
     * @return the value of the tags.
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags to be associated with the secret.
     *
     * @param tags The tags to set
     * @return the SecretBase object itself.
     */
    public CertificateBase tags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the keyId identifier.
     *
     * @return the keyId identifier.
     */
    public String keyId() {
        return this.keyId;
    }

    /**
     * Get the managed value.
     *
     * @return the managed value
     */
    public Boolean managed() {
        return this.managed;
    }

    /**
     * Get the version of the secret.
     *
     * @return the version of the secret.
     */
    public String version() {
        return this.version;
    }

    /**
     * Get the secret name.
     *
     * @return the name of the secret.
     */
    public String name() {
        return this.name;
    }

    /**
     * Get the recovery level of the secret.

     * @return the recoveryLevel of the secret.
     */
    public String recoveryLevel() {
        return recoveryLevel;
    }

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean enabled() {
        return this.enabled;
    }

    public KeyConfiguration keyConfiguration(){
        return this.keyConfig;
    }

    /**
     * Set the name value.
     *
     * @param issuerName the name value to set
     * @return the IssuerParameters object itself.
     */
    public CertificateBase issuerName(String issuerName) {
        this.issuerName = issuerName;
        return this;
    }

    /**
     * Get the name value.
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
     * Set the certificateType value.
     *
     * @param issuerCertificateTypeRequest the certificateType to request from issuer.
     * @return the IssuerParameters object itself.
     */
    public CertificateBase issuerCertificateTypeRequest(String issuerCertificateTypeRequest) {
        this.issuerCertificateTypeRequest = issuerCertificateTypeRequest;
        return this;
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
     * Set the certificateTransparency value.
     *
     * @param certificateTransparency the certificateTransparency value to set
     * @return the IssuerParameters object itself.
     */
    public CertificateBase certificateTransparency(Boolean certificateTransparency) {
        this.certificateTransparency = certificateTransparency;
        return this;
    }

    public void propertiesEnabled(Boolean propertiesEnabled){
        this.propertiesEnabled = propertiesEnabled;
    }

    public Boolean propertiesEnabled(){
        return this.propertiesEnabled;
    }

    @JsonProperty("attributes")
    private void unpackAttributes(Map<String, Object> attributes) {
        this.enabled = (Boolean) attributes.get("enabled");
        this.notBefore =  epochToOffsetDateTime(attributes.get("nbf"));
        this.expires =  epochToOffsetDateTime(attributes.get("exp"));
        this.created = epochToOffsetDateTime(attributes.get("created"));
        this.updated = epochToOffsetDateTime(attributes.get("updated"));
        this.recoveryLevel = (String) attributes.get("recoveryLevel");
        this.keyId = (String) lazyValueSelection(attributes.get("keyId"), this.keyId);
        this.tags = (Map<String, String>) lazyValueSelection(attributes.get("tags"), this.tags);
        this.managed = (Boolean) lazyValueSelection(attributes.get("managed"), this.managed);
        unpackId((String) attributes.get("id"));
    }


    @JsonProperty("policy")
    private void unpackPolicy(Map<String, Object> policy) {

        Map<String, Object> keyProps = (Map<String, Object>) policy.get("key_props");
        String keyType = (String)keyProps.get("kty");
        Integer keySize = (Integer) keyProps.get("key_size");
        Boolean exportable = (Boolean) keyProps.get("exportable");
        Boolean reuseKey = (Boolean) keyProps.get("reuseKey");
        JsonWebKeyCurveName curve = (JsonWebKeyCurveName) keyProps.get("crv");


        Map<String, Object> x509Props = (Map<String, Object>) policy.get("x509_props");
        Map<String, Object> secretProps = (Map<String, Object>) policy.get("secret_props");
        Map<String, Object> issuerProps = (Map<String, Object>) policy.get("issuer");
        Map<String, Object> attributes = (Map<String, Object>) policy.get("attributes");




        this.issuerName = (String) issuerProps.get("name");
        this.issuerCertificateTypeRequest = (String) issuerProps.get("cty");
        this.certificateTransparency = (Boolean) issuerProps.get("cert_transparency");
        this.propertiesEnabled = (Boolean) attributes.get("enabled");
        KeyConfiguration keyConfig = null;
        switch (keyType){
            case "RSA":
                keyConfig = new RSAKeyConfiguration(JsonWebKeyType.RSA)
                                .keySize(keySize)
                                .exportable(exportable)
                                .reuseKey(reuseKey)
                                .enhancedKeyUsage((List<String>) x509Props.get("ekus"))
                                .keyUsage((List<KeyUsageType>) x509Props.get("key_usage"));
                break;

            case "RSA-HSM" :
                keyConfig = new RSAKeyConfiguration(JsonWebKeyType.RSA_HSM)
                    .keySize(keySize)
                    .exportable(exportable)
                    .reuseKey(reuseKey)
                    .enhancedKeyUsage((List<String>) x509Props.get("ekus"))
                    .keyUsage((List<KeyUsageType>) x509Props.get("key_usage"));
                break;

            case "EC" :
                keyConfig = new RSAKeyConfiguration(JsonWebKeyType.EC)
                        .keySize(keySize)
                        .exportable(exportable)
                        .reuseKey(reuseKey)
                        .enhancedKeyUsage((List<String>) x509Props.get("ekus"))
                        .keyUsage((List<KeyUsageType>) x509Props.get("key_usage"))
                        .curve(curve);
                break;

            case "EC-HSM" :
                keyConfig = new RSAKeyConfiguration(JsonWebKeyType.EC_HSM)
                        .keySize(keySize)
                        .exportable(exportable)
                        .reuseKey(reuseKey)
                        .enhancedKeyUsage((List<String>) x509Props.get("ekus"))
                        .keyUsage((List<KeyUsageType>) x509Props.get("key_usage"))
                        .curve(curve);
                break;
        }

        this.keyConfig = keyConfig;
        this.secretContentType = (String) secretProps.get("contentType");
        this.validityInMonths = (Integer) x509Props.get("validity_months");
        this.subjectName = (String) x509Props.get("subject");
    }


    private OffsetDateTime epochToOffsetDateTime(Object epochValue) {
        if (epochValue != null) {
            Instant instant = Instant.ofEpochMilli(((Number) epochValue).longValue() * 1000L);
            return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        return null;
    }

    @JsonProperty(value = "id")
    private void unpackId(String id) {
        if (id != null && id.length() > 0) {
            this.id = id;
            try {
                URL url = new URL(id);
                String[] tokens = url.getPath().split("/");
                this.name = (tokens.length >= 3 ? tokens[2] : null);
                this.version = (tokens.length >= 4 ? tokens[3] : null);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private Object lazyValueSelection(Object input1, Object input2) {
        if (input1 == null) {
            return input2;
        }
        return input1;
    }

}

