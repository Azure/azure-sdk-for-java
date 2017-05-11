/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.google.common.io.BaseEncoding;
import org.joda.time.Duration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
    public void canCreateApplication() throws Exception {
        Application application = graphRbacManager.applications().define("anotherapp8")
                .withSignOnUrl("http://easycreate.azure.com/anotherapp/8")
                .withIdentifierUrl("http://easycreate.azure.com/anotherapp/8")
                .defineKey("passwd")
                    .withPassword("P@ssw0rd")
                    .withDuration(Duration.standardDays(700))
                    .attach()
                .defineKey("cert")
                    .withCertificate(BaseEncoding.base64().encode(Files.readAllBytes(Paths.get("/Users/jianghlu/Documents/code/certs/myserver.crt"))))
                    .withType(CertificateType.ASYMMETRIC_X509_CERT)
                    .withDuration(Duration.standardDays(100))
                    .attach()
                .create();
        System.out.println(application.id() + " - " + application.appId());
    }

}
