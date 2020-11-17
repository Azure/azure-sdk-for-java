// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.samples;


import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.appplatform.samples.ManageSpringCloud;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class AppPlatformLiveOnlyTests extends SamplesTestBase {

    @Test
    @DoNotRecord
    public void testSpringCloud() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageSpringCloud.runSample(azureResourceManager, clientIdFromFile()));
    }
}
