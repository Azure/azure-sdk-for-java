package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction.X509CertTokenVerificationKey;

public class X509CertTokenVerificationKeyTests {
    
    private final URL certFileName = getClass().getResource("/certificate/server.crt");
    
    @Test
    public void EmptyX509Certificate() {
        // Arrange
        X509CertTokenVerificationKey certKey = new X509CertTokenVerificationKey();
        
        // Act
        X509Certificate cert = certKey.getX509Certificate();
        
        // Assert
        assertNull(cert);
    }

    @Test
    public void X509CertificateInConstructorShouldExists() throws CertificateException, IOException {
        // Arrange
        InputStream certFile = certFileName.openStream();
        BufferedInputStream certInStream = new BufferedInputStream(certFile);
        X509Certificate cert = (X509Certificate)
                CertificateFactory.getInstance("X.509").generateCertificate(certInStream);
        
        // Act
        X509CertTokenVerificationKey certKey = new X509CertTokenVerificationKey(cert);
        X509Certificate resultCert = certKey.getX509Certificate();
        
        // Assert
        assertNotNull(resultCert);
        assertEquals(cert, resultCert);
    }
    
    @Test
    public void GetterSetterX509Certificate() throws CertificateException, IOException {
        // Arrange
        InputStream certFile = certFileName.openStream();
        BufferedInputStream certInStream = new BufferedInputStream(certFile);
        X509Certificate cert = (X509Certificate)
                CertificateFactory.getInstance("X.509").generateCertificate(certInStream);
        
        // Act
        X509CertTokenVerificationKey certKey = new X509CertTokenVerificationKey();
        certKey.setX509Certificate(cert);
        X509Certificate resultCert = certKey.getX509Certificate();
        
        // Assert
        assertNotNull(resultCert);
        assertEquals(cert, resultCert);
    }
    
    @Test
    public void RawBodyOfX509CertificateShouldMatch() throws CertificateException, IOException {
        // Arrange
        InputStream certFile = certFileName.openStream();
        BufferedInputStream certInStream = new BufferedInputStream(certFile);
        X509Certificate cert = (X509Certificate)
                CertificateFactory.getInstance("X.509").generateCertificate(certInStream);
        X509CertTokenVerificationKey certKey = new X509CertTokenVerificationKey(cert);
        
        // Act        
        byte[] rawBody = certKey.getRawBody();
        X509CertTokenVerificationKey secondCertKey = new X509CertTokenVerificationKey();
        secondCertKey.setRawBody(rawBody);
        X509Certificate resultCert = secondCertKey.getX509Certificate();
        
        // Assert
        assertNotNull(resultCert);
        assertEquals(cert, resultCert);
    }
    
    @Test
    public void InvalidRawBodyOfX509CertificateShouldNullRawBody() throws CertificateException, IOException {
        // Arrange
        X509CertTokenVerificationKey certKey = new X509CertTokenVerificationKey();
        
        // Act
        certKey.setRawBody("invalid rawbody".getBytes());
        byte[] resultRawBody = certKey.getRawBody();
        
        // Assert
        assertNull(resultRawBody);
    }
    
    
}
