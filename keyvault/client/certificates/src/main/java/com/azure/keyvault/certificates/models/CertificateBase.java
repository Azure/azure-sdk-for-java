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
     * The certificate id.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    @JsonProperty(value = "tags")
    private Map<String, String> tags;

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
     * Get the notBefore UTC time.
     *
     * @return the notBefore UTC time.
     */
    public OffsetDateTime notBefore() {
        return notBefore;
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

    /**
     * Set the enabled value.
     * @param enabled The enabled value to set.
     * @return
     */
    public CertificateBase enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     *  Get the X509 Thumbprint of the certificate.
     * @return the x509Thumbprint.
     */
    public byte[] x509Thumbprint() {
        return this.x509Thumbprint.decodedBytes();
    }




















    //private boolean propertiesEnabled;








//    /**
//     * Get the subject value.
//     *
//     * @return the subject value
//     */
//    public String subjectName() {
//        return this.subjectName;
//    }
//
//    /**
//     * Set the subject value.
//     *
//     * @param subjectName the subject value to set
//     * @return the X509CertificateProperties object itself.
//     */
//    public CertificateBase subjectName(String subjectName) {
//        this.subjectName = subjectName;
//        return this;
//    }
//
//    /**
//     * Set the subjectAlternativeNames value.
//     *
//     * @param subjectAlternativeDomainNames the subjectAlternativeNames value to set
//     * @return the X509CertificateProperties object itself.
//     */
//    public CertificateBase subjectAlternativeDomainNames(String ... subjectAlternativeDomainNames) {
//        this.subjectAlternativeDomainNames = Arrays.asList(subjectAlternativeDomainNames);
//        return this;
//    }

//    /**
//     * Get the validityInMonths value.
//     *
//     * @return the validityInMonths value
//     */
//    public Integer validityInMonths() {
//        return this.validityInMonths;
//    }
//
//    /**
//     * Set the validityInMonths value.
//     *
//     * @param validityInMonths the validityInMonths value to set
//     * @return the X509CertificateProperties object itself.
//     */
//    public CertificateBase validityInMonths(Integer validityInMonths) {
//        this.validityInMonths = validityInMonths;
//        return this;
 //   }

//    public CertificateBase rsaKeyConfiguration(RSAKeyConfiguration rsaKeyConfiguration) {
//        this.keyConfig = rsaKeyConfiguration;
//        return this;
//    }
//
//    public CertificateBase ecKeyConfiguration(ECKeyConfiguration ecKeyConfiguration) {
//        this.keyConfig = ecKeyConfiguration;
//        return this;
//    }



//    /**
//     * Get the keyId identifier.
//     *
//     * @return the keyId identifier.
//     */
//    public String keyId() {
//        return this.keyId;
//    }




//    /**
//     * Set the name value.
//     *
//     * @param issuerName the name value to set
//     * @return the IssuerParameters object itself.
//     */
//    public CertificateBase issuerName(String issuerName) {
//        this.issuerName = issuerName;
//        return this;
//    }
//
//    /**
//     * Get the name value.
//     *
//     * @return the Issuer name.
//     */
//    public String issuerName() {
//        return issuerName;
//    }
//
//    /**
//     * Get the certificateType value.
//     *
//     * @return the certificateType value
//     */
//    public String issuerCertificateTypeRequest() {
//        return this.issuerCertificateTypeRequest;
//    }
//
//    /**
//     * Set the certificateType value.
//     *
//     * @param issuerCertificateTypeRequest the certificateType to request from issuer.
//     * @return the IssuerParameters object itself.
//     */
//    public CertificateBase issuerCertificateTypeRequest(String issuerCertificateTypeRequest) {
//        this.issuerCertificateTypeRequest = issuerCertificateTypeRequest;
//        return this;
//    }
//
//    /**
//     * Get the certificateTransparency value.
//     *
//     * @return the certificateTransparency value
//     */
//    public Boolean certificateTransparency() {
//        return this.certificateTransparency;
//    }
//
//    /**
//     * Set the certificateTransparency value.
//     *
//     * @param certificateTransparency the certificateTransparency value to set
//     * @return the IssuerParameters object itself.
//     */
//    public CertificateBase certificateTransparency(Boolean certificateTransparency) {
//        this.certificateTransparency = certificateTransparency;
//        return this;
//    }


    @JsonProperty("attributes")
    private void unpackBaseAttributes(Map<String, Object> attributes) {
        this.enabled = (Boolean) attributes.get("enabled");
        this.notBefore =  epochToOffsetDateTime(attributes.get("nbf"));
        this.expires =  epochToOffsetDateTime(attributes.get("exp"));
        this.created = epochToOffsetDateTime(attributes.get("created"));
        this.updated = epochToOffsetDateTime(attributes.get("updated"));
        this.recoveryLevel = (String) attributes.get("recoveryLevel");
       // this.keyId = (String) lazyValueSelection(attributes.get("keyId"), this.keyId);
        this.tags = (Map<String, String>) lazyValueSelection(attributes.get("tags"), this.tags);
        unpackId((String) attributes.get("id"));
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

