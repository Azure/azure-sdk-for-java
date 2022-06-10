// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization;

import java.time.Duration;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApplicationsTests extends GraphRbacManagementTest {
    @Test
    public void canCRUDApplication() throws Exception {
        String name = generateRandomResourceName("javasdkapp", 20);

        ActiveDirectoryApplication application = null;
        try {
            application =
                authorizationManager
                    .applications()
                    .define(name)
                    .withSignOnUrl("http://easycreate.azure.com/" + name)
                    .definePasswordCredential("passwd")
                    .withPasswordValue("P@ssw0rd")
                    .withDuration(Duration.ofDays(700))
                    .attach()
                    .defineCertificateCredential("cert")
                    .withAsymmetricX509Certificate()
                    .withPublicKey(readAllBytes(this.getClass().getResourceAsStream("/myTest.cer")))
                    .withDuration(Duration.ofDays(100))
                    .attach()
                    .create();
            System.out.println(application.id() + " - " + application.applicationId());
            Assertions.assertNotNull(application.id());
            Assertions.assertNotNull(application.applicationId());
            Assertions.assertEquals(name, application.name());
            Assertions.assertEquals(1, application.certificateCredentials().size());
            Assertions.assertEquals(1, application.passwordCredentials().size());
            Assertions.assertEquals(1, application.replyUrls().size());
            Assertions.assertEquals(1, application.identifierUris().size());
            Assertions.assertEquals("http://easycreate.azure.com/" + name, application.signOnUrl().toString());

            application.update().withoutCredential("passwd").apply();
            System.out.println(application.id() + " - " + application.applicationId());
            Assertions.assertEquals(0, application.passwordCredentials().size());
        } finally {
            if (application != null) {
                authorizationManager.applications().deleteById(application.id());
            }
        }
    }

    @Test
    @DoNotRecord
    public void canListApplications() {
        if (skipInPlayback()) {
            return;
        }

        PagedIterable<ActiveDirectoryApplication> applications = authorizationManager.applications().list();
        Assertions.assertTrue(TestUtilities.getSize(applications) > 0);
    }
}
