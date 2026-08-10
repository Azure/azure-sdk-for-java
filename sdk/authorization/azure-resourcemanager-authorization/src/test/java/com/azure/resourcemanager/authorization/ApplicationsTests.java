// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;

public class ApplicationsTests extends GraphRbacManagementTest {
    @Test
    public void canCRUDApplication() throws Exception {
        String name = generateRandomResourceName("javasdkapp", 20);

        ActiveDirectoryApplication application = null;
        try {
            application = authorizationManager.applications()
                .define(name)
                .withSignOnUrl("http://easycreate.azure.com/" + name)
                .definePasswordCredential("passwd")
                .withDuration(Duration.ofDays(700))
                .attach()
                .defineCertificateCredential("cert")
                .withAsymmetricX509Certificate()
                .withPublicKey(replaceCRLF(readAllBytes(this.getClass().getResourceAsStream("/myTest.cer"))))
                .withDuration(Duration.ofDays(100))
                .attach()
                .create();
            System.out.println(application.id() + " - " + application.applicationId());
            Assertions.assertNotNull(application.id());
            Assertions.assertNotNull(application.applicationId());
            Assertions.assertEquals(name, application.name());
            Assertions.assertEquals(1, application.certificateCredentials().size());
            Assertions.assertEquals(1, application.passwordCredentials().size());
            Assertions.assertEquals(0, application.replyUrls().size());
            Assertions.assertEquals(0, application.identifierUris().size());
            Assertions.assertEquals("http://easycreate.azure.com/" + name, application.signOnUrl().toString());

            application.update().withoutCredential("passwd").withoutCredential("cert").apply();
            System.out.println(application.id() + " - " + application.applicationId());
            Assertions.assertEquals(0, application.passwordCredentials().size());
            Assertions.assertEquals(0, application.certificateCredentials().size());
        } finally {
            if (application != null) {
                authorizationManager.applications().deleteById(application.id());
            }
        }
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canAddCertificateCredentialFromX509CertificateDuringUpdate() throws Exception {
        if (skipInPlayback()) {
            return;
        }

        byte[] certificateBytes;
        try (InputStream inputStream = this.getClass().getResourceAsStream("/myTest.cer")) {
            X509Certificate certificate
                = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
            certificateBytes = certificate.getEncoded();
        }

        String encodedCertificate = Base64.getEncoder().encodeToString(certificateBytes);
        Assertions.assertTrue(encodedCertificate.contains("+") || encodedCertificate.contains("/"));

        String name = generateRandomResourceName("javasdkapp", 20);
        ActiveDirectoryApplication application = null;
        try {
            application = authorizationManager.applications()
                .define(name)
                .withSignOnUrl("http://easycreate.azure.com/" + name)
                .create();

            application.update()
                .defineCertificateCredential("cert")
                .withAsymmetricX509Certificate()
                .withPublicKey(certificateBytes)
                .withDuration(Duration.ofDays(100))
                .attach()
                .apply();

            Assertions.assertEquals(1, application.certificateCredentials().size());
        } finally {
            if (application != null) {
                authorizationManager.applications().deleteById(application.id());
            }
        }
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canListApplications() {
        if (skipInPlayback()) {
            return;
        }

        PagedIterable<ActiveDirectoryApplication> applications = authorizationManager.applications().list();
        Assertions.assertTrue(TestUtilities.getSize(applications) > 0);
    }
}
