package com.azure.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Certificate extends CertificateBase {

    /**
     * CER contents of x509 certificate.
     */
    @JsonProperty(value = "cer")
    private byte[] cer;

    /**
     * The key id.
     */
    @JsonProperty(value = "kid", access = JsonProperty.Access.WRITE_ONLY)
    private String keyId;

    /**
     * The secret id.
     */
    @JsonProperty(value = "sid", access = JsonProperty.Access.WRITE_ONLY)
    private String secretId;

    private CertificatePolicy certificatePolicy;


    public Certificate(String name){
        super.name = name;
    }

    public Certificate() {

    }

    public String keyId(){
        return this.keyId;
    }

    public String secretId(){
        return this.secretId;
    }

    public byte[] cer(){
        return cer;
    }

    public CertificatePolicy certificatePolicy(){
        return this.certificatePolicy;
    }

    public Certificate certificatePolicy(CertificatePolicy certificatePolicy){
        this.certificatePolicy = certificatePolicy;
        return this;
    }

}
