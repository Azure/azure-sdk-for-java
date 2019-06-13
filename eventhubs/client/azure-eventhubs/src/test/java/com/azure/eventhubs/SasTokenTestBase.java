// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.ApiTestBase;
import com.azure.eventhubs.implementation.SharedAccessSignatureTokenProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Locale;

public class SasTokenTestBase extends ApiTestBase {


    private static CredentialInfo originalCredentialInfo = getCredentialInfo();

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @BeforeClass
    public static void initialize() {
        final ServiceLogger logger = new ServiceLogger(SasTokenTestBase.class);

        CredentialInfo anotherCredentialInfo = new CredentialInfo();

        String sasToken;
        try {
            SharedAccessSignatureTokenProvider tokenProvider = new SharedAccessSignatureTokenProvider(
                originalCredentialInfo.sharedAccessKeyName(), originalCredentialInfo.sharedAccessKey());
            try {
                sasToken = tokenProvider.getToken(
                    String.format(Locale.US, "amqp://%s/%s",
                        originalCredentialInfo.endpoint().getHost(), originalCredentialInfo.eventHubPath()),
                    Duration.ofDays(1));
            } catch (UnsupportedEncodingException e) {
                logger.asError().log(String.format("Unable to generate SAS Token: %s", e));
            }
        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeyException e) {
            logger.asError().log(String.format("Unable to construct a SAS token provider: %s", e));
        }

        //TODO: make sure there is SAS token support for track 2 preview 1
//        anotherCredentialInfo.sharedAccessSignature(sasToken);
        ApiTestBase.getEventHubClientBuilder().credentials(anotherCredentialInfo);
    }

    @AfterClass
    public static void cleanup() {
       if (originalCredentialInfo != null) {
           ApiTestBase.getEventHubClientBuilder().credentials(originalCredentialInfo);
       }
    }

}
