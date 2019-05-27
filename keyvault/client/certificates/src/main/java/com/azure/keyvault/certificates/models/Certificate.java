package com.azure.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Certificate extends CertificateBase{
    /**
     * CER contents of x509 certificate.
     */
    @JsonProperty(value = "cer")
    private byte[] cer;

    /**
     * The secret id.
     */
    @JsonProperty(value = "sid", access = JsonProperty.Access.WRITE_ONLY)
    private String secretId;


    public Certificate(String name){
        super.name = name;
    }

    public Certificate() {

    }

    /**
     * The key id.
     */
    @JsonProperty(value = "kid", access = JsonProperty.Access.WRITE_ONLY)
    private String keyId;


    public String keyId(){
        return this.keyId;
    }

    public String secretId(){
        return this.secretId;
    }

    public byte[] cer(){
        return cer;
    }

    /**
     * Set the contentType value.
     *
     * @param secretContentType the contentType value to set
     * @return the SecretProperties object itself.
     */
    public Certificate secretContentType(String secretContentType) {
        super.secretContentType(secretContentType);
        return this;
    }

    /**
     * Set the subject value.
     *
     * @param subjectName the subject value to set
     * @return the X509CertificateProperties object itself.
     */
    public Certificate subjectName(String subjectName) {
        super.subjectName(subjectName);
        return this;
    }

    /**
     * Set the subjectAlternativeNames value.
     *
     * @param subjectAlternativeDomainNames the subjectAlternativeNames value to set
     * @return the X509CertificateProperties object itself.
     */
    public Certificate subjectAlternativeDomainNames(String ... subjectAlternativeDomainNames) {
        super.subjectAlternativeDomainNames(subjectAlternativeDomainNames);
        return this;
    }

    /**
     * Set the validityInMonths value.
     *
     * @param validityInMonths the validityInMonths value to set
     * @return the X509CertificateProperties object itself.
     */
    public Certificate validityInMonths(Integer validityInMonths) {
        super.validityInMonths(validityInMonths);
        return this;
    }

    public Certificate rsaKeyConfiguration(RSAKeyConfiguration rsaKeyConfiguration) {
        super.rsaKeyConfiguration(rsaKeyConfiguration);
        return this;
    }

    public Certificate ecKeyConfiguration(ECKeyConfiguration ecKeyConfiguration) {
        super.ecKeyConfiguration(ecKeyConfiguration);
        return this;
    }

    /**
     * Set the name value.
     *
     * @param issuerName the name value to set
     * @return the IssuerParameters object itself.
     */
    @Override
    public Certificate issuerName(String issuerName) {
        super.issuerName(issuerName);
        return this;
    }

    /**
     * Set the certificateType value.
     *
     * @param issuerCertificateTypeRequest the certificateType to request from issuer.
     * @return the IssuerParameters object itself.
     */
    @Override
    public Certificate issuerCertificateTypeRequest(String issuerCertificateTypeRequest) {
        super.issuerCertificateTypeRequest(issuerCertificateTypeRequest);
        return this;
    }

    /**
     * Set the certificateTransparency value.
     *
     * @param certificateTransparency the certificateTransparency value to set
     * @return the IssuerParameters object itself.
     */
    @Override
    public Certificate certificateTransparency(Boolean certificateTransparency) {
        super.certificateTransparency(certificateTransparency);
        return this;
    }

}
