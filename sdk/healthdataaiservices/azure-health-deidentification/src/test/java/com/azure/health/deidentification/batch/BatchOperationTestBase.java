// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.batch;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.health.deidentification.DeidentificationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Base class for Deid Services client tests.
 */
public class BatchOperationTestBase extends TestProxyTestBase {
    private static final String FAKE_STORAGE_ACCOUNT_SAS_URI = "https://fake_storage_account_sas_uri.blob.core.windows.net/container-sdk-dev-fakeid";

    protected DeidentificationClientBuilder getDeidServicesClientBuilder() {
        DeidentificationClientBuilder deidentificationClientBuilder = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (interceptorManager.isPlaybackMode()) {
            deidentificationClientBuilder
                .httpClient(interceptorManager.getPlaybackClient())
                .credential(new MockTokenCredential());
        } else if (interceptorManager.isRecordMode()) {
            deidentificationClientBuilder
                .addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build())
                .httpClient(HttpClient.createDefault());
        } else if (interceptorManager.isLiveMode()) {
            deidentificationClientBuilder
                .credential(new DefaultAzureCredentialBuilder().build())
                .httpClient(HttpClient.createDefault());
        }

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.removeSanitizers("AZSDK3493", "AZSDK4001", "AZSDK3430", "AZSDK2003", "AZSDK2030");
        }
        return deidentificationClientBuilder;
    }

    String getJobName() {
        return testResourceNamer.randomName("job", 16);
    }

    /**
     * Get the training data set SAS Url value based on the test running mode.
     *
     * @return the training data set Url
     */
    String getStorageAccountSASUri() {
        return interceptorManager.isPlaybackMode() ? FAKE_STORAGE_ACCOUNT_SAS_URI : Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_SAS_URI");
    }
}
