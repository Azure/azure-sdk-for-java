package com.azure.keyvault.certificates.models;

import java.util.List;

public class MergeCertificateConfig {
    private String certificateName;
    private List<byte[]> x509Certificates;

    public MergeCertificateConfig(String certificateName, List<byte[]> x509Certificates){
        this.certificateName = certificateName;
        this.x509Certificates = x509Certificates;
    }
}
