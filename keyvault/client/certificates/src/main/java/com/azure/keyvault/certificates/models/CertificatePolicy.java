package com.azure.keyvault.certificates.models;

import com.azure.keyvault.certificates.models.webkey.JsonWebKeyCurveName;
import com.azure.keyvault.certificates.models.webkey.JsonWebKeyType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CertificatePolicy extends CertificateBase{

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
     * The subject alternative emails.
     */
    private List<String> subjectAlternativeEmails;

    /**
     * The subject alternative user principal names.
     */
    private List<String> subjectAlternativeUPNs;

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
    private String secretContentType;

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
     *  The RSA key configuration of the key backing the certificate.
     */
    private RSAKeyConfiguration rsaKeyConfiguration;


    /**
     *  The EC key configuration of the key backing the certificate.
     */
    private ECKeyConfiguration ecKeyConfiguration;


    public CertificatePolicy(String issuerName, String subjectName){
        this.issuerName = issuerName;
        this.subjectName = subjectName;
    }

    public CertificatePolicy() {

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
     * @return The enabled value
     */
    public CertificateBase enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
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
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy secretContentType(String secretContentType) {
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
     * Set the subjectAlternativeNames value.
     *
     * @param subjectAlternativeDomainNames the subjectAlternativeNames value to set
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy subjectAlternativeDomainNames(String ... subjectAlternativeDomainNames) {
        this.subjectAlternativeDomainNames = Arrays.asList(subjectAlternativeDomainNames);
        return this;
    }

    /**
     * Set the Subject alternative emails.
     *
     * @param subjectAlternativeDomainNames the subjectAlternativeNames value to set
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy subjectAlternativeEmails(String ... subjectAlternativeDomainNames) {
        this.subjectAlternativeDomainNames = Arrays.asList(subjectAlternativeDomainNames);
        return this;
    }

    /**
     * Set the Subject alternative user principal names.
     *
     * @param subjectAlternativeUPNs The Subject alternative user principal names value to set.
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy subjectAlternativeUPNs(String ... subjectAlternativeUPNs) {
        this.subjectAlternativeUPNs = Arrays.asList(subjectAlternativeUPNs);
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
     * Set the Rsa Key Configuration.
     * @param rsaKeyConfiguration The Rsa key configuration to set.
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy rsaKeyConfiguration(RSAKeyConfiguration rsaKeyConfiguration) {
        this.rsaKeyConfiguration = rsaKeyConfiguration;
        return this;
    }

    /**
     * Set the Ec Key Configuration.
     * @param ecKeyConfiguration The Ec key configuration to set.
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy ecKeyConfiguration(ECKeyConfiguration ecKeyConfiguration) {
        this.ecKeyConfiguration = ecKeyConfiguration;
        return this;
    }

    /**
     * Get the Ec Key Configuration.
     * @return the EcKeyConfiguration.
     */
    public ECKeyConfiguration ecKeyConfiguration() {
        return this.ecKeyConfiguration;
    }

    /**
     * Get the Rsa Key Configuration.
     * @return the EcKeyConfiguration.
     */
    public RSAKeyConfiguration rsaKeyConfiguration() {
        return this.rsaKeyConfiguration;
    }

    /**
     * Set the name value.
     *
     * @param issuerName the name value to set
     * @return the CertificatePolicy object itself.
     */
    public CertificatePolicy issuerName(String issuerName) {
        this.issuerName =issuerName ;
        return this;
    }

    /**
     * Set the certificateType value.
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
     * Get the subject alternative domain names.
     *
     * @return the subject alternative domain names.
     */
    public List<String> subjectAlternativeDomainNames() {
        return this.subjectAlternativeDomainNames;
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
     * Get the certificateTransparency value.
     *
     * @return the certificateTransparency value
     */
    public Boolean certificateTransparency() {
        return this.certificateTransparency;
    }

    public CertificatePolicy addLifetimeAction(LifetimeAction action){
        this.lifetimeActions.add(action);
        return this;
    }

    public List<LifetimeAction> lifetimeActions(){
        return this.lifetimeActions;
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
        this.enabled = (Boolean) attributes.get("enabled");
        switch (keyType){
            case "RSA":
                this.rsaKeyConfiguration = new RSAKeyConfiguration()
                                .keySize(keySize)
                                .exportable(exportable)
                                .reuseKey(reuseKey)
                                .enhancedKeyUsage((List<String>) x509Props.get("ekus"))
                                .keyUsage((List<KeyUsageType>) x509Props.get("key_usage"));
                break;

            case "RSA-HSM" :
                this.rsaKeyConfiguration = new RSAKeyConfiguration()
                    .keySize(keySize)
                    .exportable(exportable)
                    .hsm(true)
                    .reuseKey(reuseKey)
                    .enhancedKeyUsage((List<String>) x509Props.get("ekus"))
                    .keyUsage((List<KeyUsageType>) x509Props.get("key_usage"));
                break;

            case "EC" :
                this.ecKeyConfiguration = new ECKeyConfiguration()
                        .curve(curve)
                        .exportable(exportable)
                        .reuseKey(reuseKey)
                        .enhancedKeyUsage((List<String>) x509Props.get("ekus"))
                        .keyUsage((List<KeyUsageType>) x509Props.get("key_usage"))
                        .curve(curve);
                break;

            case "EC-HSM" :
                this.ecKeyConfiguration = new ECKeyConfiguration()
                        .curve(curve)
                        .hsm(true)
                        .exportable(exportable)
                        .reuseKey(reuseKey)
                        .enhancedKeyUsage((List<String>) x509Props.get("ekus"))
                        .keyUsage((List<KeyUsageType>) x509Props.get("key_usage"))
                        .curve(curve);
                break;
          }

        this.secretContentType = (String) secretProps.get("contentType");
        this.validityInMonths = (Integer) x509Props.get("validity_months");
        this.subjectName = (String) x509Props.get("subject");
    }
}
