// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.batch;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.health.deidentification.DeidentificationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.ArrayList;
import java.util.List;

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
            List<TestProxySanitizer> customSanitizer = new ArrayList<>();
            customSanitizer.add(new TestProxySanitizer("$..location", "^(?!.*FAKE_STORAGE_ACCOUNT).*", FAKE_STORAGE_ACCOUNT_SAS_URI, TestProxySanitizerType.BODY_KEY));
            interceptorManager.addSanitizers(customSanitizer);
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
     * Retrieves the storage account location URL based on the global configuration settings.
     * <p>
     * This method constructs the URL using the storage account name and storage container name
     * from the global configuration. The URL is in the following format:
     * {@code https://<STORAGE_ACCOUNT_NAME>.blob.core.windows.net/<STORAGE_CONTAINER_NAME>}
     * </p>
     *
     * @return The storage account location URL as a {@code String}.
     */
    String getStorageAccountLocation() {
        if (interceptorManager.isPlaybackMode()) {
            return FAKE_STORAGE_ACCOUNT_SAS_URI;
        }
        return "https://" + Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_NAME") + ".blob.core.windows.net/" + Configuration.getGlobalConfiguration().get("STORAGE_CONTAINER_NAME");
    }
}
