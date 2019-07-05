package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "X509CertTokenVerificationKey")
public class X509CertTokenVerificationKey extends AsymmetricTokenVerificationKey {

    @XmlTransient
    private X509Certificate x509Certificate;
    
    public X509CertTokenVerificationKey() {
    }

    public X509CertTokenVerificationKey(X509Certificate x509Certificate) throws CertificateEncodingException {
        this.setX509Certificate(x509Certificate);
        super.setRawBody(x509Certificate.getEncoded());
    }

    /**
     * @return the x509Certificate
     */
    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }

    /**
     * @param x509Certificate the x509Certificate to set
     */
    public void setX509Certificate(X509Certificate x509Certificate) {
        this.x509Certificate = x509Certificate;
    }
    
    @Override
    public void setRawBody(byte[] rawBody) {
        super.setRawBody(rawBody);
        ByteArrayInputStream input = new ByteArrayInputStream(rawBody);
        try {
            this.x509Certificate = (X509Certificate)
                    CertificateFactory.getInstance("X.509").generateCertificate(input);
        } catch (CertificateException e) {
            super.setRawBody(null);
        }
         
    }
    
    
}
