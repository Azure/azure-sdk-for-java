/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import org.joda.time.Duration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ApplicationsTests extends GraphRbacManagementTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Test
    @Ignore("Need to login as user to run")
    public void canCRUDApplication() throws Exception {
        Application application = null;
        try {
            application = graphRbacManager.applications().define("anotherapp15")
                    .withSignOnUrl("http://easycreate.azure.com/anotherapp/15")
                    .withIdentifierUrl("http://easycreate.azure.com/anotherapp/15")
                    .definePasswordCredential("passwd")
                        .withPasswordValue("P@ssw0rd")
                        .withDuration(Duration.standardDays(700))
                        .attach()
                    .defineCertificateCredential("cert")
                        .withAsymmetricX509Cert()
                        .withPublicKey(Files.readAllBytes(Paths.get("/Users/jianghlu/Documents/code/certs/myserver.crt")))
                        .withDuration(Duration.standardDays(100))
                        .attach()
                    .create();
            System.out.println(application.id() + " - " + application.appId());

            application.update()
                    .withoutCredential("passwd")
                    .apply();
            System.out.println(application.id() + " - " + application.appId());
        } finally {
            if (application != null) {
                graphRbacManager.applications().deleteById(application.id());
            }
        }
    }

}
